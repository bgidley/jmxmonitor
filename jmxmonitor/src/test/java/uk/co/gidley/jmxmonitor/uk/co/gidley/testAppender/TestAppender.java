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

package uk.co.gidley.jmxmonitor.uk.co.gidley.testAppender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import java.util.ArrayList;
import java.util.List;

/**
 * This appender simply stores events in a big old array
 * This allows a test to assert a log event has been raised.
 *
 * Created by IntelliJ IDEA. User: ben Date: Jan 7, 2010 Time: 4:46:55 PM
 */
public class TestAppender extends AppenderBase {
	private static List<ILoggingEvent> events = new ArrayList<ILoggingEvent>(20000);

	@Override
	protected void append(Object eventObject) {
		events.add((ILoggingEvent) eventObject);
	}

	public static List<ILoggingEvent> getEvents() {
		return events;
	}

	public static void reset(){
		events.clear();
	}
}
