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

package net.roboconf.agent.impl.internal;

import net.roboconf.agent.api.AbstractAgent;
import net.roboconf.agent.api.IAgent;
import net.roboconf.agent.api.internal.PluginMock;
import net.roboconf.core.model.runtime.Instance;
import net.roboconf.plugin.api.PluginInterface;

/**
 * @author Vincent Zurczak - Linagora
 */
public class DefaultAgentImpl extends AbstractAgent implements IAgent {

	// Fields that should be injected (ipojo)
	protected PluginInterface[] plugins;


	/**
	 * Finds the right plug-in for an instance.
	 * <p>
	 * If {@link #simulatePlugins} is true, this method returns an instance
	 * of {@link PluginMock}, no matter what is the installer name.
	 * </p>
	 *
	 * @param instance a non-null instance
	 * @return the plug-in associated with the instance's installer name
	 */
	@Override
	public PluginInterface findPlugin( Instance instance ) {

		PluginInterface result = null;
		String installerName = instance.getComponent().getInstallerName();

		// Run through available plug-ins
		// Handle the case of unit tests (without iPojo)
		if( this.simulatePlugins )
			result = super.findPlugin( instance );

		else if( this.plugins != null ) {
			for( PluginInterface pi : this.plugins ) {
				if( pi.getPluginName().equalsIgnoreCase( installerName )) {
					result = pi;
					break;
				}
			}
		}

		// Initialize the result, if any
		if( result == null )
			this.logger.severe( "No plugin was found for instance " + instance.getName() + " with installer " + installerName + "." );
		else
			result.setNames( this.applicationName, this.rootInstanceName );

		return result;
	}
}
