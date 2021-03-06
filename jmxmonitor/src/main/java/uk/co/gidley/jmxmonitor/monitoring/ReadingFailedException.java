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

import org.apache.commons.lang.exception.NestableException;

/**
 * Thrown when a reading failed. This should wrap any exception thrown
 */
public class ReadingFailedException extends NestableException {
	public ReadingFailedException() {
		super();
	}

	public ReadingFailedException(String msg) {
		super(msg);
	}

	public ReadingFailedException(Throwable cause) {
		super(cause);
	}

	public ReadingFailedException(String msg, Throwable cause) {
		super(msg, cause);
	}

	@Override
	 public String toString() {
		return "Failed to read monitor";
	}
}
