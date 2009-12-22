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

package uk.co.gidley.jmxmonitor;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by IntelliJ IDEA. User: ben Date: Dec 22, 2009 Time: 7:48:44 PM
 */
public class JmxMonitorTest {
	private ByteArrayOutputStream outputStream;
	private PrintStream out;
	private ByteArrayOutputStream errorStream;
	private PrintStream error;

	@BeforeMethod
	public void setupMonitorConsole() {
		outputStream = new ByteArrayOutputStream();
		out = System.out;
		System.setOut(new PrintStream(outputStream));
	}

	@Test
	public void testJmxMonitorMissingArg() {

		JmxMonitor.main(new String[] { "jmxmonitor", "-c" });
		assertThat(outputStream.toString(), containsString("usage: jmxMonitor\n" +
				" -c <arg>   Configuration Path"));

	}

	@Test
	public void testJmxMonitorMissingOption() {

		JmxMonitor.main(new String[] { "jmxmonitor" });
		assertThat(outputStream.toString(), containsString("usage: jmxMonitor\n" +
				" -c <arg>   Configuration Path"));

	}

	@Test
	public void testJmxMonitorSpareOption() {

		JmxMonitor.main(new String[] { "jmxmonitor", "-c", "out.txt", "-d" });
		assertThat(outputStream.toString(), containsString("usage: jmxMonitor\n" +
				" -c <arg>   Configuration Path"));

	}

	@Test
	public void testJmxMonitorValidOption() {

		JmxMonitor.main(new String[] { "jmxmonitor", "-c", "out.txt" });
		String output = outputStream.toString();
		assertThat(output, not(containsString("usage: jmxMonitor\n" +
				" -c <arg>   Configuration Path")));
		assertThat(output, containsString("ConfigurationFile is out.txt"));
	}

	@AfterMethod
	public void tearDownMonitorConsole() {
		System.setOut(out);
	}
}
