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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A JMX monitor that takes readings based on a discriminator Is that really the best classname I could think of Created
 * by IntelliJ IDEA. User: ben Date: Jan 8, 2010 Time: 5:11:22 PM
 */
public class DiscriminatingJmxMonitor implements Monitor {

	private static final Logger logger = LoggerFactory.getLogger(DiscriminatingJmxMonitor.class);

	private String name;
	private MBeanServerConnection mbeanServerConnection;
	private String discriminator;
	private ObjectName objectName;

	private String attributeName;

	public String getName() {
		return name;
	}

	@Override
	public Object getReading() throws ReadingFailedException {
		try {
			Set<ObjectInstance> beans = mbeanServerConnection.queryMBeans(objectName, null);

			Map<String, Object> results = new HashMap<String, Object>();
			for (ObjectInstance bean : beans) {
				String discriminatorValue = mbeanServerConnection.getAttribute(bean.getObjectName(),
						this.discriminator).toString();
				results.put(discriminatorValue,
						mbeanServerConnection.getAttribute(bean.getObjectName(), attributeName));
			}
			return results;
		} catch (Exception e) {
			logger.error("Error reading {}. Exception was {}", name, e);
			throw new ReadingFailedException(e);
		}

	}

	/**
	 * @param name		  The name of this monitor
	 * @param objectName	The objectName pattern to resolve 1 or N objects
	 * @param attributeName The attribute to monitor
	 * @param discriminator The attribute to discriminate results by
	 */
	public DiscriminatingJmxMonitor(String name, ObjectName objectName, String attributeName, String discriminator,
			MBeanServerConnection jmxConnection) {

		this.name = name;
		this.mbeanServerConnection = jmxConnection;
		this.objectName = objectName;
		this.attributeName = attributeName;
		this.discriminator = discriminator;

	}
}
