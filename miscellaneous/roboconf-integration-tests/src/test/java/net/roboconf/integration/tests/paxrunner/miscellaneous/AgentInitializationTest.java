/**
 * Copyright 2014-2016 Linagora, Université Joseph Fourier, Floralis
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

package net.roboconf.integration.tests.paxrunner.miscellaneous;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import net.roboconf.agent.internal.Agent;
import net.roboconf.agent.internal.AgentMessageProcessor;
import net.roboconf.agent.internal.misc.HeartbeatTask;
import net.roboconf.agent.internal.misc.PluginMock;
import net.roboconf.core.internal.tests.TestUtils;
import net.roboconf.core.model.beans.ApplicationTemplate;
import net.roboconf.core.model.beans.Instance;
import net.roboconf.core.model.beans.Instance.InstanceStatus;
import net.roboconf.core.model.helpers.InstanceHelpers;
import net.roboconf.dm.management.ManagedApplication;
import net.roboconf.dm.management.Manager;
import net.roboconf.integration.probes.DmTest;
import net.roboconf.integration.tests.internal.ItUtils;
import net.roboconf.integration.tests.internal.MyHandler;
import net.roboconf.integration.tests.internal.MyTargetResolver;
import net.roboconf.integration.tests.internal.runners.RoboconfPaxRunner;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;

/**
 * A set of tests for the agent's initialization.
 * <p>
 * We launch a Karaf installation with an agent in-memory. We load
 * an application and instantiates a root instance. The new agent
 * must send an initial message to the DM to indicate it is alive.
 * It must then receive its model from the DM.
 * </p>
 *
 * @author Vincent Zurczak - Linagora
 */
@RunWith( RoboconfPaxRunner.class )
@ExamReactorStrategy( PerMethod.class )
public class AgentInitializationTest extends DmTest {

	private static final String APP_LOCATION = "my.app.location";

	@Inject
	protected Manager manager;


	@ProbeBuilder
	public TestProbeBuilder probeConfiguration( TestProbeBuilder probe ) {

		// We need to specify the classes we need
		// and that come from external modules.
		probe.addTest( DmTest.class );
		probe.addTest( TestUtils.class );

		probe.addTest( MyHandler.class );
		probe.addTest( MyTargetResolver.class );

		// Classes from the agent
		probe.addTest( Agent.class );
		probe.addTest( PluginMock.class );
		probe.addTest( HeartbeatTask.class );
		probe.addTest( AgentMessageProcessor.class );

		return probe;
	}


	@Override
	@Configuration
	public Option[] config() throws Exception {

		List<Option> options = new ArrayList<> ();
		options.addAll( Arrays.asList( super.config()));

		// Store the application's location
		File resourcesDirectory = TestUtils.findApplicationDirectory( "lamp" );
		String appLocation = resourcesDirectory.getAbsolutePath();
		options.add( systemProperty( APP_LOCATION ).value( appLocation ));

		// Deploy the agent's bundles
		String roboconfVersion = ItUtils.findRoboconfVersion();
		options.add( mavenBundle()
				.groupId( "net.roboconf" )
				.artifactId( "roboconf-plugin-api" )
				.version( roboconfVersion )
				.start());

		options.add( mavenBundle()
				.groupId( "net.roboconf" )
				.artifactId( "roboconf-agent" )
				.version( roboconfVersion )
				.start());

		return options.toArray( new Option[ options.size()]);
	}


	@Test
	public void run() throws Exception {

		// Update the manager
		MyTargetResolver myResolver = new MyTargetResolver();
		this.manager.setTargetResolver( myResolver );
		this.manager.reconfigure();

		// Sleep for a while, to let the RabbitMQ client factory arrive.
		Thread.sleep(2000);

		// Load the application
		String appLocation = System.getProperty( APP_LOCATION );
		ApplicationTemplate tpl = this.manager.applicationTemplateMngr().loadApplicationTemplate( new File( appLocation ));
		ManagedApplication ma = this.manager.applicationMngr().createApplication( "test", null, tpl );
		Assert.assertNotNull( ma );
		Assert.assertEquals( 1, this.manager.applicationMngr().getManagedApplications().size());

		// Associate a default target for this application
		String targetId = this.manager.targetsMngr().createTarget( "handler: whatever" );
		this.manager.targetsMngr().associateTargetWithScopedInstance( targetId, ma.getApplication(), null );

		// There is no agent yet (no root instance was deployed)
		Assert.assertEquals( 0, myResolver.handler.agentIdToAgent.size());

		// Instantiate a new root instance
		Instance rootInstance = InstanceHelpers.findInstanceByPath( ma.getApplication(), "/MySQL VM" );
		Assert.assertNotNull( rootInstance );
		Assert.assertEquals( InstanceStatus.NOT_DEPLOYED, rootInstance.getStatus());

		this.manager.instancesMngr().changeInstanceState( ma, rootInstance, InstanceStatus.DEPLOYED_STARTED );
		Thread.sleep( 800 );
		Assert.assertEquals( InstanceStatus.DEPLOYED_STARTED, rootInstance.getStatus());

		// A new agent must have been created
		Assert.assertEquals( 1, myResolver.handler.agentIdToAgent.size());
		Agent agent = myResolver.handler.agentIdToAgent.values().iterator().next();
		Thread.sleep( 1000 );
		Assert.assertFalse( agent.needsModel());
		Assert.assertNotNull( agent.getScopedInstance());
		Assert.assertEquals( "MySQL VM", agent.getScopedInstance().getName());
		Assert.assertEquals( 2, InstanceHelpers.buildHierarchicalList( agent.getScopedInstance()).size());

		// Try to instantiate another VM
		Instance anotherRootInstance = InstanceHelpers.findInstanceByPath( ma.getApplication(), "/Tomcat VM 1" );
		Assert.assertNotNull( anotherRootInstance );
		Assert.assertEquals( InstanceStatus.NOT_DEPLOYED, anotherRootInstance.getStatus());

		this.manager.instancesMngr().changeInstanceState( ma, anotherRootInstance, InstanceStatus.DEPLOYED_STARTED );
		Thread.sleep( 800 );
		Assert.assertEquals( InstanceStatus.DEPLOYED_STARTED, anotherRootInstance.getStatus());

		Assert.assertEquals( 2, myResolver.handler.agentIdToAgent.size());
		Agent anotherAgent = myResolver.handler.agentIdToAgent.get( "/Tomcat VM 1 @ test" );
		Assert.assertNotNull( anotherAgent );

		Thread.sleep( 1000 );
		Assert.assertFalse( anotherAgent.needsModel());
		Assert.assertNotNull( anotherAgent.getScopedInstance());
		Assert.assertEquals( "Tomcat VM 1", anotherAgent.getScopedInstance().getName());
		Assert.assertEquals( 2, InstanceHelpers.buildHierarchicalList( anotherAgent.getScopedInstance()).size());

		// Undeploy them all
		this.manager.instancesMngr().changeInstanceState( ma, rootInstance, InstanceStatus.NOT_DEPLOYED );
		this.manager.instancesMngr().changeInstanceState( ma, anotherRootInstance, InstanceStatus.NOT_DEPLOYED );
		Thread.sleep( 300 );
		Assert.assertEquals( 0, myResolver.handler.agentIdToAgent.size());
	}
}
