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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gidley.jmxmonitor.services.Monitor;
import uk.co.gidley.jmxmonitor.services.ReadingFailedException;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

/**
 * Created by IntelliJ IDEA. User: ben Date: Dec 23, 2009 Time: 7:04:11 PM
 */
public class SimpleJmxMonitor implements Monitor {

	private static final Logger logger = LoggerFactory.getLogger(SimpleJmxMonitor.class);

	private String name;
	private MBeanServerConnection mbeanServerConnection;
	private ObjectName objectName;
	private String attributeName;


	@Override
	public void initialise(String name, ObjectName objectName, String attributeName, MBeanServerConnection jmxConnection) {
		this.name = name;
		this.mbeanServerConnection = jmxConnection;
		this.objectName = objectName;
		this.attributeName = attributeName;

	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Object getReading() throws ReadingFailedException {
		try {
			return mbeanServerConnection.getAttribute(objectName, attributeName);
		} catch (Exception e) {
			logger.error("Error reading {}. Exception was {}", name, e);
			throw new ReadingFailedException(e);
		}
	}
}
