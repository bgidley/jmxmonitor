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

package uk.co.gidley.jmxmonitor;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.tapestry5.ioc.IOCUtilities;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gidley.jmxmonitor.services.InitialisationException;
import uk.co.gidley.jmxmonitor.services.JmxMonitorModule;
import uk.co.gidley.jmxmonitor.services.MainConfiguration;
import uk.co.gidley.jmxmonitor.services.ThreadManager;

/**
 * Start the registry and pass control to the manager service.
 * <p/>
 * This is seperate from the command line version to facilitate testing
 */
public class RegistryManager {
	private static final Logger logger = LoggerFactory.getLogger(RegistryManager.class);

	private String configurationFile;

	public boolean isReadyToRun() {
		return readyToRun;
	}

	private boolean readyToRun = false;

	public Registry getRegistry() {
		return registry;
	}

	private Registry registry;

	public RegistryManager(String configurationFile) {
		this.configurationFile = configurationFile;
	}

	public void invoke() throws InitialisationException {
		RegistryBuilder registryBuilder = new RegistryBuilder();
		IOCUtilities.addDefaultModules(registryBuilder);
		registryBuilder.add(JmxMonitorModule.class);
		registry = registryBuilder.build();
		parseMainConfiguration(configurationFile, registry);
		registry.performRegistryStartup();
		ThreadManager threadManager = registry.getService(ThreadManager.class);
		try {
			readyToRun = true;
			threadManager.initialise();
		}
		finally {
			registry.shutdown();
		}
	}

	private static void parseMainConfiguration(String configurationFile,
			Registry registry) throws InitialisationException {
		MainConfiguration mainConfiguration = registry.getService(MainConfiguration.class);
		// Read configuration file
		CompositeConfiguration config = new CompositeConfiguration();
		config.setThrowExceptionOnMissing(true);
		try {
			config.addConfiguration(new PropertiesConfiguration(configurationFile));
		} catch (ConfigurationException e) {
			logger.error("{}", e);
			throw new InitialisationException(e);
		}
		mainConfiguration.setConfiguration(config);
	}
}
