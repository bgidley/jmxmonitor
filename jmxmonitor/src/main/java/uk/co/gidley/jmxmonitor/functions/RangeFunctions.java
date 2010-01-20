/*
 * Copyright 2010 Ben Gidley
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

package uk.co.gidley.jmxmonitor.functions;

/**
 * Library of extra functions for scripting. These are just to make it easier to do things in the scripts
 * <p/>
 * This library is themed around checking ranges
 * <p/>
 * Created by IntelliJ IDEA. User: ben Date: Jan 19, 2010 Time: 1:08:02 PM
 */
public class RangeFunctions {

	/**
	 * Returns true IFF reading is greater than value
	 *
	 * @param reading
	 * @param value
	 * @return
	 */
	public static boolean isAbove(long reading, long value) {
		return reading > value;
	}

	/**
	 * Returns true IFF reading is less than value
	 *
	 * @param reading
	 * @param value
	 * @return
	 */
	public static boolean isBelow(long reading, long value) {
		return reading < value;
	}

	/**
	 * Returns true IFF reading is between floor and ceiling
	 *
	 * @param reading
	 * @param floor
	 * @param ceiling
	 * @return
	 */
	public static boolean isBetween(long reading, long floor, long ceiling) {
		return reading < ceiling && reading > floor;
	}
}
