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

package uk.co.gidley.jmxmonitor.monitoring;

import ch.qos.logback.classic.BasicConfigurator;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import uk.co.gidley.jmxmonitor.RegistryManager;
import uk.co.gidley.jmxmonitor.services.InitialisationException;
import uk.co.gidley.jmxmonitor.services.ThreadManager;
import uk.co.gidley.jmxmonitor.uk.co.gidley.testAppender.TestAppender;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by IntelliJ IDEA. User: ben Date: Jan 7, 2010 Time: 4:18:00 PM
 */
public abstract class BaseMonitoringTest {
	private static final Logger logger = LoggerFactory.getLogger(BaseMonitoringTest.class);
	private RegistryManager registryManager;
	private Thread jmxMonitorThread;

	@BeforeMethod
	public void setup() throws InitialisationException, JoranException, InterruptedException, MalformedObjectNameException, MBeanRegistrationException, InstanceAlreadyExistsException, NotCompliantMBeanException {
		registryManager = new RegistryManager(getConfiguration());

		registerTestMBeans();
		jmxMonitorThread = new Thread(new JmxRunner(), "JmxRunner");
		// Give the registry a chance to start
		jmxMonitorThread.start();
		while (!registryManager.isReadyToRun()) {
			Thread.sleep(100);
		}
		// Reconfigure Logback to use the in-memory appender
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		JoranConfigurator configurator = new JoranConfigurator();
		configurator.setContext(lc);
		lc.reset();
		configurator.doConfigure("src/test/resources/logback-inMemory.xml");
		StatusPrinter.printInCaseOfErrorsOrWarnings(lc);

		// Wait for at least 3 events to be logged
		// THIS NUMBER SHOULD BE INCREMENTED FOR NUMBER OF OUTPUTS EXPECTED FOR THE SELECTED CONFIG
		while (TestAppender.getEvents().size() < waitForEvents()) {
			Thread.sleep(100);
		}
	}

	public abstract String getConfiguration();

	public abstract int waitForEvents();

	public abstract void registerTestMBeans() throws MalformedObjectNameException, MBeanRegistrationException, InstanceAlreadyExistsException, NotCompliantMBeanException;

	@AfterMethod
	public void tearDown() {
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		BasicConfigurator.configure(lc);
		TestAppender.reset();
		try {
			ThreadManager threadManager = registryManager.getRegistry().getService(ThreadManager.class);
			threadManager.stop();
		} catch (IllegalStateException e) {
			//noop
		}
		try {
			// Wait for jmxmonitor to stop
			jmxMonitorThread.join();
		} catch (InterruptedException e) {
			logger.error("{}", e);
			throw new RuntimeException(e);
		}
	}

	private class JmxRunner implements Runnable {
		@Override
		public void run() {
			try {
				registryManager.invoke();
			} catch (InitialisationException e) {
				logger.error("{}", e);
				throw new RuntimeException(e);
			}
		}
	}
}
