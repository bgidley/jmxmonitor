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
import org.testng.annotations.Test;
import uk.co.gidley.jmxmonitor.uk.co.gidley.testAppender.TestAppender;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by IntelliJ IDEA. User: ben Date: Jan 8, 2010 Time: 4:16:06 PM
 */
public class SimpleMBeanTest extends BaseMonitoringTest {

	@Test
	public void testMemoryMonitoring() {

		List<ILoggingEvent> events = TestAppender.getEvents();
		// Find "Heap Used" message
		boolean heapUsedFound = false;
		for (ILoggingEvent event : events) {
			if (event.getFormattedMessage().startsWith("Heap Used")) {
				heapUsedFound = true;
			}
		}
		assertThat(heapUsedFound, equalTo(true));
	}

	@Override
	public String getConfiguration() {
		return "src/test/resources/monitoring/simpleMBeanConfiguration.properties";
	}

	@Override
	public int waitForEvents() {
		return 1;
	}

	@Override
	public void registerTestMBeans() {
		//noop
	}
}
