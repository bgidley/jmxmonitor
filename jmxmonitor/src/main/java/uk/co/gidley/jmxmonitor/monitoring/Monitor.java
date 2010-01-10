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

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

/**
 * A monitor retrieves data from a remote source
 * <p/>
 * Monitors are initialised by their monitoring group. On initialisation they are passed details of what to monitor and
 * the mbeanconnection.
 * <p/>
 * Monitors don't need to recover from failed connections. They should throw a reading failed exception. Once this has
 * occurred the controlling process will not run that monitor again. It will disgard it and initialise a replacement.
 */
public interface Monitor {

	/**
	 * The name of the monitor This is defined in the scope of the monitor group
	 * @return
	 */
	public String getName();

	/**
	 * The reading is typically a base type or a String. Other items can be used but you need to be sure EL can handle
	 * them
	 *
	 * @return
	 */
	public Object getReading() throws ReadingFailedException;
}
