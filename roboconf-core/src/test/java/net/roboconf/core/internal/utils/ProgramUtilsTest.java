/**
 * Copyright 2014 Linagora, Université Joseph Fourier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.roboconf.core.internal.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Logger;

import org.junit.Test;

/**
 * @author Vincent Zurczak - Linagora
 */
public class ProgramUtilsTest {

	@Test
	public void testChangeDirectory() throws Exception {

		// This should work on any OS
		ProgramUtils.executeCommand(
				Logger.getLogger( getClass().getName()),
				Arrays.asList( "help" ),
				new HashMap<String,String> ());
	}
}
