/*
 * Copyright 2009 Ben Gidley
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package uk.co.gidley.jmxmonitor.services;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * The manager service is responsible for managing the main thread loop and controlling the monitors
 */
public class Manager {
	private static final Logger logger = LoggerFactory.getLogger(Manager.class);
	public static final String SHUTDOWN_MONITOR_THREAD = "ShutdownMonitor";
	private Boolean running = true;
	private Map<String, MonitoringGroupHolder> monitoringGroups = new HashMap<String, MonitoringGroupHolder>();
	private ThreadGroup threadGroup = new ThreadGroup("MonitoringGroups");

	public void initialise(String configurationFile) throws InitialisationException {

		try {
			// Read configuration file
			CompositeConfiguration config = new CompositeConfiguration();
			config.addConfiguration(new PropertiesConfiguration(configurationFile));
			// Start shutdown socket service
			ShutdownRunner shutdownRunner = new ShutdownRunner(config);
			Thread shutdownThread = new Thread(shutdownRunner, SHUTDOWN_MONITOR_THREAD);
			shutdownThread.start();

			// Configure Monitoring Group instances
			List<String> monitoringGroupNames = config.getList("jmxmonitor.groups");

			for (String groupName : monitoringGroupNames) {
				logger.debug("Started initialising {}", groupName);
				initialiseMonitoringGroup(groupName, config);
				logger.debug("Completed initialising {}", groupName);
			}


			// Start threads to begin monitoring
			logger.info("Configuration complete starting all monitors");
			for (String groupName : monitoringGroups.keySet()) {
				Thread thread = monitoringGroups.get(groupName).getThread();
				thread.start();
			}

			// Continue to monitor for failures or stop message. On failure stop group, restart it if possible
			while (running) {
				for (String groupName : monitoringGroups.keySet()) {
					MonitoringGroup monitoringGroup = monitoringGroups.get(groupName).getMonitoringGroup();
					if (!monitoringGroup.isAlive()){
						restartMonitoringGroup(groupName, config);
					}
				}

				// Stop if the shutdown thread has triggered. 
				if (!shutdownThread.isAlive()) {
					running = false;
				}

				Thread.sleep(5000);
			}


		} catch (ConfigurationException e) {
			logger.error("{}", e);
			throw new InitialisationException(e);
		} catch (NoSuchElementException e) {
			logger.error("{}", e);
			throw new InitialisationException(e);
		} catch (IOException e) {
			logger.error("{}", e);
			throw new InitialisationException(e);
		} catch (InterruptedException e) {
			logger.error("{}", e);
			throw new RuntimeException(e);
		}

	}

	private void restartMonitoringGroup(String groupName,
			CompositeConfiguration config) throws InterruptedException, InitialisationException {
		logger.warn("Monitoring Group is dead: {}. Restarting", groupName);

		// Tidy up
		Thread oldThread = monitoringGroups.get(groupName).getThread();
		if (oldThread.isAlive()){
			// Problem try to interrupt. This should force an exist
			oldThread.interrupt();
			oldThread.join(5000);
			if (oldThread.isAlive()){
				logger.error("Unable to stop monitor thread {}", groupName);
				throw new RuntimeException("Unable to stop monitor thread " + groupName);
			}
		}
		monitoringGroups.remove(groupName);

		// Restart
		initialiseMonitoringGroup(groupName, config);
		Thread restartThread = monitoringGroups.get(groupName).getThread();
		restartThread.start();
	}

	private void initialiseMonitoringGroup(String groupName,
			CompositeConfiguration config) throws InitialisationException {
		String monitorFile = config.getString(groupName + ".monitorConfiguration");
		String expressionFile = config.getString(groupName + ".expressionConfiguration");
		Long interval = config.getLong(groupName + ".interval");

		File monitor = findConfigurationFile(monitorFile);
		File expression = findConfigurationFile(expressionFile);

		MonitoringGroup monitoringGroup = new MonitoringGroup();
		monitoringGroup.initialise(groupName, monitor, expression, interval);
		Thread thread = new Thread(threadGroup, monitoringGroup, groupName);
		monitoringGroups.put(groupName, new MonitoringGroupHolder(monitoringGroup, thread));
	}

	private File findConfigurationFile(String configurationFile) throws InitialisationException {
		File configuration = new File(configurationFile);
		if (configuration.exists()) {
			throw new InitialisationException(
					"Referenced Configuration File not found:" + configuration.getAbsolutePath());
		}
		return configuration;
	}

	private class ShutdownRunner implements Runnable {
		private ServerSocketChannel serverSocketChannel;
		private ByteBuffer dbuf = ByteBuffer.allocateDirect(1024);
		private Charset charset = Charset.forName("US-ASCII");
		private CharsetDecoder decoder = charset.newDecoder();
		private String stopKey;


		ShutdownRunner(CompositeConfiguration config) throws IOException {
			int stopPort = config.getInt("jmxmonitor.stopport");
			stopKey = config.getString("jmxmonitor.stopkey");
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.socket().bind(new InetSocketAddress("127.0.0.1", stopPort));
		}

		@Override
		public void run() {
			logger.info("Stop listener thread working");

			try {

				while (running) {
					SocketChannel socketChannel = serverSocketChannel.accept();
					dbuf.clear();
					socketChannel.read(dbuf);
					dbuf.flip();
					CharBuffer cb = decoder.decode(dbuf);
					if (cb.toString().equals(stopKey)) {
						running = false;
						serverSocketChannel.close();
					}
				}
			} catch (IOException e) {
				logger.error("{}", e);
				throw new RuntimeException(e);
			}

		}
	}

	/**
	 * Used to hold the monitoring group
	 */
	private class MonitoringGroupHolder{
		private MonitoringGroup monitoringGroup;
		private Thread thread;

		private MonitoringGroupHolder(MonitoringGroup monitoringGroup, Thread thread) {
			this.monitoringGroup = monitoringGroup;
			this.thread = thread;
		}

		public MonitoringGroup getMonitoringGroup() {
			return monitoringGroup;
		}

		public void setMonitoringGroup(MonitoringGroup monitoringGroup) {
			this.monitoringGroup = monitoringGroup;
		}

		public Thread getThread() {
			return thread;
		}

		public void setThread(Thread thread) {
			this.thread = thread;
		}
	}

}
