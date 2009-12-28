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

import java.io.File;
import java.util.Date;

/**
 * A monitoring group combines a set of monitors and expressions to output data to a logger
 */
public class MonitoringGroup implements Runnable {

	private boolean stopping = false;
	private int interval;
	private long lastRun;

	/**
	 * Called to initialise the monitoring group. The group should use this to construct expensive objects, validate
	 * configuration and prepare to run.
	 *
	 * @param monitorsConfiguration
	 * @param expressionsConfiguration
	 * @param intervalInMilliseconds
	 */
	public void initialise(File monitorsConfiguration, File expressionsConfiguration, int intervalInMilliseconds) {
		interval = intervalInMilliseconds;
	}

	/**
	 * The group should stop at the next opportunity. At most within 5 seconds.
	 */
	public void stop() {
		stopping = true;
	}


	/**
	 * The group should respond true within 5 seconds if it is still functioning.
	 */
	public boolean isAlive() {
		return false;
	}


	/**
	 * Start monitoring (as a thread) return when stopped
	 */
	@Override
	public void run() {
		try {
			while (!stopping) {

				long currentRun = new Date().getTime();

				if (lastRun + interval > currentRun ){
					// Run Monitors

					// Run and output expressions
					lastRun = currentRun;
				}
				Thread.sleep(4000);
			}
		} catch (InterruptedException e) {
			//
		} finally {
			// Tidy up all monitors / expressions IF possible
		}

	}
}
