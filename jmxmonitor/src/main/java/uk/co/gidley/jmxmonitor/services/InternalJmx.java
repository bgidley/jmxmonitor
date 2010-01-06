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
import org.apache.commons.configuration.Configuration;
import org.apache.tapestry5.ioc.annotations.EagerLoad;
import org.apache.tapestry5.ioc.services.RegistryShutdownHub;
import org.apache.tapestry5.ioc.services.RegistryShutdownListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Holds the internal JMX configuration Created by IntelliJ IDEA. User: ben Date: Jan 5, 2010 Time: 10:08:37 AM
 */
@EagerLoad
public class InternalJmx implements RegistryShutdownListener {
	private static final Logger logger = LoggerFactory.getLogger(InternalJmx.class);
	private final String PROPERTY_PREFIX;
	private JMXConnectorServer jmxConnectorServer;
	private Registry registry;

	public InternalJmx(MainConfiguration configuration, RegistryShutdownHub registryShutdownHub) throws InitialisationException {
		//TODO make this a symbol
		this.PROPERTY_PREFIX = Manager.PROPERTY_PREFIX;
		registryShutdownHub.addRegistryShutdownListener(this);
		start(configuration.getConfiguration());


	}

	public void start(Configuration configuration) throws InitialisationException {

		startRmiRegistry(configuration);
		startJmxConnector(configuration);
	}

	private void startRmiRegistry(Configuration configuration) throws InitialisationException {
		try {
			int port = configuration.getInt(PROPERTY_PREFIX + "localRmiPort");
			logger.debug("Creating RMI Registry on {}", port);
			registry = LocateRegistry.createRegistry(port);
		} catch (RemoteException e) {
			logger.error("{}", e);
			throw new InitialisationException(e);
		}
	}

	private void startJmxConnector(Configuration configuration) throws InitialisationException {
		try {
			MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
			String url = configuration.getString(PROPERTY_PREFIX + "localJmx", null);
			if (url != null) {
				logger.debug("Initialising local JMX server on URL {}", url);
				JMXServiceURL address = new JMXServiceURL(url);
				jmxConnectorServer = JMXConnectorServerFactory.newJMXConnectorServer(
						address, null,
						mBeanServer);

				ObjectName connectorServerName = ObjectName
						.getInstance("connectors:protocol=rmi");
				mBeanServer.registerMBean(jmxConnectorServer, connectorServerName);
				jmxConnectorServer.start();
			}
		} catch (IOException e) {
			logger.error("{}", e);
			throw new InitialisationException(e);
		} catch (MalformedObjectNameException e) {
			logger.error("{}", e);
			throw new InitialisationException(e);
		} catch (InstanceAlreadyExistsException e) {
			logger.error("{}", e);
			throw new InitialisationException(e);
		} catch (MBeanRegistrationException e) {
			logger.error("{}", e);
			throw new InitialisationException(e);
		} catch (NotCompliantMBeanException e) {
			logger.error("{}", e);
			throw new InitialisationException(e);
		}
	}

	@Override
	public void registryDidShutdown() {
		if (jmxConnectorServer != null) {
			logger.debug("Shutting down JMX");
			try {
				jmxConnectorServer.stop();
			} catch (IOException e) {
				logger.error("{}", e);
			}
		}
		if (registry != null) {
			logger.debug("Shutting down RMI");
			try {
				UnicastRemoteObject.unexportObject(this.registry, true);
			} catch (NoSuchObjectException e) {
				logger.error("{}", e);
			}
		}

	}
}
