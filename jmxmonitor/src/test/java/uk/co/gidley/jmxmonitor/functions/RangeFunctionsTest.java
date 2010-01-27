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

import org.testng.annotations.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by IntelliJ IDEA. User: ben Date: Jan 19, 2010 Time: 1:13:54 PM
 */
public class RangeFunctionsTest {

	@Test
	public void testBetween(){
		assertThat(RangeFunctions.isBetween(2,1,3), is(true));
		assertThat(RangeFunctions.isBetween(1,1,3), is(false));
		assertThat(RangeFunctions.isBetween(3,1,3), is(false));
	}

	@Test
	public void testBelow(){
		assertThat(RangeFunctions.isBelow(1,2), is(true));
		assertThat(RangeFunctions.isBelow(2,2), is(false));
	}

	@Test
	public void testAbove(){
		assertThat(RangeFunctions.isAbove(2,2), is(false));
		assertThat(RangeFunctions.isAbove(3,2), is(true));
	}

}
