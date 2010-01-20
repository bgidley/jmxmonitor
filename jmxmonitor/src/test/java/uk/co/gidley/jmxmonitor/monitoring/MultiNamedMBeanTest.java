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

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import uk.co.gidley.jmxmonitor.uk.co.gidley.testAppender.TestAppender;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by IntelliJ IDEA. User: ben Date: Jan 8, 2010 Time: 4:38:11 PM
 */
public class MultiNamedMBeanTest extends BaseMonitoringTest {
	private ObjectInstance ping1;
	private ObjectInstance ping2;

	/**
	 * This test tests handling MBeans like C3PO's they are called things like com.mchange.v2.c3p0:type=PooledDataSource[2rvxty863z22nb16dadw4|62a34b91]
	 * AND the bit in [] changes every JVM restart.
	 * <p/>
	 * You therefore have to use a wildcard in the objectname to find them.
	 */
	@Test
	public void TestMultiNamedMBean() throws InterruptedException {

		List<ILoggingEvent> events = TestAppender.getEvents();
		// Find "Heap Used" message
		boolean ping1Found = false;
		boolean ping2Found = false;

		for (ILoggingEvent event : events) {
			if (event.getFormattedMessage().startsWith("Ping 1")) {
				ping1Found = true;
			}
			if (event.getFormattedMessage().startsWith("Ping 2")) {
				ping2Found = true;
			}
		}

		assertThat(ping1Found, equalTo(true));
		assertThat(ping2Found, equalTo(true));
	}

	@Override
	public String getConfiguration() {
		return "src/test/resources/monitoring/MultiNamedMBeanConfiguration.properties";
	}

	@Override
	public int waitForEvents() {
		return 2;
	}

	@Override
	public void registerTestMBeans() throws MalformedObjectNameException, MBeanRegistrationException, InstanceAlreadyExistsException, NotCompliantMBeanException {
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

		ObjectName bean1 = new ObjectName("uk.co.gidley.jmxmonitor:type=Bean1[guff]");
		ObjectName bean2 = new ObjectName("uk.co.gidley.jmxmonitor:type=Bean1[guff2]");
		ping1 = mBeanServer.registerMBean(new Ping("1"), bean1);
		ping2 = mBeanServer.registerMBean(new Ping("2"), bean2);
	}
	
	public interface PingMBean {

		public String getDiscriminator();

		public void setDiscriminator(String discriminator);

		public Boolean getPing();
	}

	public class Ping implements PingMBean {
		private Ping(String discriminator) {
			this.discriminator = discriminator;
		}

		@Override
		public String getDiscriminator() {
			return discriminator;
		}

		@Override
		public void setDiscriminator(String discriminator) {
			this.discriminator = discriminator;
		}

		private String discriminator;

		@Override
		public Boolean getPing() {
			return true;
		}
	}
}
