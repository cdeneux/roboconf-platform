/**
 * Copyright 2015-2016 Linagora, Université Joseph Fourier, Floralis
 *
 * The present code is developed in the scope of the joint LINAGORA -
 * Université Joseph Fourier - Floralis research program and is designated
 * as a "Result" pursuant to the terms and conditions of the LINAGORA
 * - Université Joseph Fourier - Floralis research program. Each copyright
 * holder of Results enumerated here above fully & independently holds complete
 * ownership of the complete Intellectual Property rights applicable to the whole
 * of said Results, and may freely exploit it in any manner which does not infringe
 * the moral rights of the other copyright holders.
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

package net.roboconf.target.api;

import java.util.Map;

import org.junit.Assert;
import net.roboconf.core.internal.tests.TestUtils;
import net.roboconf.core.model.beans.Instance;
import net.roboconf.core.model.beans.Instance.InstanceStatus;
import net.roboconf.target.api.internal.TestAbstractThreadedTargetHandler;

import org.junit.Test;

/**
 * @author Vincent Zurczak - Linagora
 */
public class AbstractThreadedTargetHandlerTest {

	@Test
	public void testNormalExecution() throws Exception {

		final TestAbstractThreadedTargetHandler th = new TestAbstractThreadedTargetHandler( false );
		Map<?,?> configurators = TestUtils.getInternalField( th, "machineIdToConfigurators", Map.class );

		// Schedule period is 1000 by default in AbstractThreadedTargetHandler.
		// And it starts immediately. Here is a summary...
		//
		// 0-1000 => configure is invoked once.
		// 1001-2000 => configure has been invoked twice.
		// 2001-3000 => configure has been invoked three times.

		Instance scopedInstance = new Instance( "test" );
		try {
			Assert.assertEquals( 0, configurators.size());
			Assert.assertEquals( 0, th.getCpt());

			// For test purpose, we call configure before start (to reduce thread sleep).
			th.configureMachine( null, null, "machine-id", null, null, scopedInstance );
			th.start();

			Assert.assertEquals( 1, configurators.size());
			Assert.assertEquals( 0, th.getCpt());
			Thread.sleep( 500 );

			Assert.assertEquals( 1, configurators.size());
			Assert.assertEquals( 1, th.getCpt());
			Thread.sleep( 1000 );

			Assert.assertEquals( 1, configurators.size());
			Assert.assertEquals( 2, th.getCpt());
			Thread.sleep( 1000 );

			Assert.assertEquals( 3, th.getCpt());
			Assert.assertEquals( 0, configurators.size());

		} finally {
			th.stop();
			Assert.assertEquals( InstanceStatus.NOT_DEPLOYED, scopedInstance.getStatus());
		}
	}


	@Test
	public void test_withExceptionInConfigure() throws Exception {

		final TestAbstractThreadedTargetHandler th = new TestAbstractThreadedTargetHandler( true );
		Map<?,?> configurators = TestUtils.getInternalField( th, "machineIdToConfigurators", Map.class );

		// Schedule period is 1000 by default in AbstractThreadedTargetHandler.
		// And it starts immediately. Here is a summary...
		//
		// 0-1000 => configure is invoked once.
		// 1001-2000 => configure has been invoked twice.
		// 2001-3000 => configure has been invoked three times and resulted in an exception.

		Instance scopedInstance = new Instance( "test" );
		try {
			Assert.assertEquals( 0, configurators.size());
			Assert.assertEquals( 0, th.getCpt());

			// For test purpose, we call configure before start (to reduce thread sleep).
			th.configureMachine( null, null, "machine-id", null, null, scopedInstance );
			th.start();

			Assert.assertEquals( 0, th.getCpt());
			Assert.assertEquals( 1, configurators.size());
			Thread.sleep( 500 );

			Assert.assertEquals( 1, th.getCpt());
			Assert.assertEquals( 1, configurators.size());
			Thread.sleep( 1000 );

			Assert.assertEquals( 1, th.getCpt());
			Assert.assertEquals( 0, configurators.size());

		} finally {
			th.stop();
			Assert.assertEquals( InstanceStatus.NOT_DEPLOYED, scopedInstance.getStatus());
		}
	}


	@Test
	public void test_withExceptionInConfigure_instanceIsDeploying() throws Exception {

		// The same test than before, except the instance is being deployed.
		// When the exception is thrown, the instance's state should be changed to PROBLEM.

		final TestAbstractThreadedTargetHandler th = new TestAbstractThreadedTargetHandler( true );
		Map<?,?> configurators = TestUtils.getInternalField( th, "machineIdToConfigurators", Map.class );

		Instance scopedInstance = new Instance( "test" ).status( InstanceStatus.DEPLOYING );
		try {
			Assert.assertEquals( 0, configurators.size());
			Assert.assertEquals( 0, th.getCpt());

			// For test purpose, we call configure before start (to reduce thread sleep).
			th.configureMachine( null, null, "machine-id", null, null, scopedInstance );
			th.start();

			Assert.assertEquals( 0, th.getCpt());
			Assert.assertEquals( 1, configurators.size());
			Thread.sleep( 500 );

			Assert.assertEquals( 1, th.getCpt());
			Assert.assertEquals( 1, configurators.size());
			Thread.sleep( 1000 );

			Assert.assertEquals( 1, th.getCpt());
			Assert.assertEquals( 0, configurators.size());

		} finally {
			th.stop();
			Assert.assertEquals( InstanceStatus.PROBLEM, scopedInstance.getStatus());
		}
	}


	@Test
	public void cancelMachineId() {
		final TestAbstractThreadedTargetHandler th = new TestAbstractThreadedTargetHandler( false );
		th.cancelMachineConfigurator( null );
		th.cancelMachineConfigurator( "whatever" );
	}


	@Test
	public void testNormalExecution_withCancellation() throws Exception {

		final TestAbstractThreadedTargetHandler th = new TestAbstractThreadedTargetHandler( false );
		Map<?,?> configurators = TestUtils.getInternalField( th, "machineIdToConfigurators", Map.class );

		// Schedule period is 1000 by default in AbstractThreadedTargetHandler.
		// And it starts immediately. Here is a summary...
		//
		// 0-1000 => configure is invoked once.
		// 1001-2000 => configure has been invoked twice.
		// 2001-3000 => configure has been invoked three times.

		try {
			Assert.assertEquals( 0, configurators.size());
			Assert.assertEquals( 0, th.getCpt());

			// For test purpose, we call configure before start (to reduce thread sleep).
			th.configureMachine( null, null, "machine-id", null, null, null );
			th.start();

			Assert.assertEquals( 1, configurators.size());
			Assert.assertEquals( 0, th.getCpt());
			Thread.sleep( 500 );

			// Cancel the configuration
			th.cancelMachineConfigurator( "machine-id" );
			th.cancelMachineConfigurator( "unknown-id" );

			Thread.sleep( 2000 );
			Assert.assertEquals( 0, configurators.size());

		} finally {
			th.stop();
		}
	}
}
