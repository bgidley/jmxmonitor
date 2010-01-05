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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.tapestry5.ioc.IOCUtilities;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gidley.jmxmonitor.services.InitialisationException;
import uk.co.gidley.jmxmonitor.services.JmxMonitorModule;
import uk.co.gidley.jmxmonitor.services.Manager;

import java.io.File;

/**
 * Created by IntelliJ IDEA. User: ben Date: Dec 22, 2009 Time: 7:24:30 PM
 */
public class JmxMonitor {

	private static final Logger logger = LoggerFactory.getLogger(JmxMonitor.class);

	public static void main(String[] args) {
		System.out.println("Starting JMX Monitor");

		Options options = new Options();
		Option configurationFileOption = new Option("c", true, "Configuration Path");
		configurationFileOption.setRequired(true);
		configurationFileOption.setArgs(1);
		options.addOption(configurationFileOption);

		CommandLineParser parser = new PosixParser();
		try {
			CommandLine cmd = parser.parse(options, args);
			String configurationFile = cmd.getOptionValue("c");
			System.out.println("ConfigurationFile is " + configurationFile);
			File file = new File(configurationFile);
			if (file.exists() && file.canRead()) {
				RegistryBuilder registryBuilder = new RegistryBuilder();
				IOCUtilities.addDefaultModules(registryBuilder);
				registryBuilder.add(JmxMonitorModule.class);
				Registry registry = registryBuilder.build();
				registry.performRegistryStartup();
				Manager manager = registry.getService(Manager.class);
				try {
					manager.initialise(configurationFile);
				}
				finally {
					registry.shutdown();
				}
			} else {
				logger.error("Unable to read configuration exiting {}", file);
			}


		} catch (ParseException e) {
			logger.error("Exception occured {}", e);
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("jmxMonitor", options);
		} catch (InitialisationException e) {
			logger.error("{}", e);
			throw new RuntimeException(e);
		}

		System.out.println("Exiting JMX Monitor");
	}
}
