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

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.tapestry5.ioc.services.RegistryShutdownHub;
import org.testng.annotations.Test;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.ManagementFactory;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * Created by IntelliJ IDEA. User: ben Date: Jan 7, 2010 Time: 3:30:57 PM
 */
public class InternalJmxTest {


	@Test
	public void testStartupShutdownNoop() throws MalformedObjectNameException, InitialisationException, ConfigurationException {

		MainConfiguration mainConfiguration = mock(MainConfiguration.class);
		RegistryShutdownHub registryShutdownHub = mock(RegistryShutdownHub.class);

		Configuration configuration = new PropertiesConfiguration("src/test/resources/noopConfiguration.properties");
		when(mainConfiguration.getConfiguration()).thenReturn(configuration);


		InternalJmx internalJmx = new InternalJmx(mainConfiguration, registryShutdownHub);

		verify(registryShutdownHub).addRegistryShutdownListener(internalJmx);

		// Internal JMX should now be not running

		try {
			ManagementFactory.getPlatformMBeanServer().getObjectInstance(internalJmx.getConnectorServerName());
			assertThat("Exception Shourt have thrown", false);
		} catch (InstanceNotFoundException e) {

		}

		// Fire shutdown anyhow
		internalJmx.registryDidShutdown();


	}

	@Test
	public void testStartupShutdown() throws MalformedObjectNameException, InitialisationException, ConfigurationException {

		MainConfiguration mainConfiguration = mock(MainConfiguration.class);
		RegistryShutdownHub registryShutdownHub = mock(RegistryShutdownHub.class);

		Configuration configuration = new PropertiesConfiguration(
				"src/test/resources/jmxLocalMonitoringTestConfiguration.properties");
		when(mainConfiguration.getConfiguration()).thenReturn(configuration);


		InternalJmx internalJmx = new InternalJmx(mainConfiguration, registryShutdownHub);

		verify(registryShutdownHub).addRegistryShutdownListener(internalJmx);

		// Internal JMX should now be not running

		try {
			ManagementFactory.getPlatformMBeanServer().getObjectInstance(internalJmx.getConnectorServerName());
		} catch (InstanceNotFoundException e) {
			assertThat("Exception should not have thrown", false);
		}

		try {
			JMXServiceURL serviceUrl = new JMXServiceURL(configuration.getString("jmxmonitor.localJmx"));
			JMXConnector jmxc = JMXConnectorFactory.connect(serviceUrl, null);
			MBeanServerConnection mBeanServerConnection = jmxc.getMBeanServerConnection();
			assertThat(mBeanServerConnection, notNullValue());
		} catch (IOException e) {
			assertThat("Exception should not have thrown", false);
		}


		// Fire shutdown anyhow
		internalJmx.registryDidShutdown();

		// Check JMX actually closed
		try {
			ManagementFactory.getPlatformMBeanServer().getObjectInstance(internalJmx.getConnectorServerName());
			assertThat("Exception should have thrown", false);
		} catch (InstanceNotFoundException e) {

		}

		try {
			JMXServiceURL serviceUrl = new JMXServiceURL(configuration.getString("jmxmonitor.localJmx"));
			JMXConnector jmxc = JMXConnectorFactory.connect(serviceUrl, null);
			MBeanServerConnection mBeanServerConnection = jmxc.getMBeanServerConnection();
			assertThat("Exception should have thrown", false);
		} catch (IOException e) {
			
		}


	}
}
