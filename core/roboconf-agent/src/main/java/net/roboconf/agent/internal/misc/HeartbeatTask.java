/**
 * Copyright 2014 Linagora, Université Joseph Fourier, Floralis
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

package net.roboconf.agent.internal.misc;

import java.io.IOException;
import java.util.TimerTask;
import java.util.logging.Logger;

import net.roboconf.agent.internal.Agent;
import net.roboconf.core.utils.Utils;
import net.roboconf.messaging.client.IAgentClient;
import net.roboconf.messaging.messages.from_agent_to_dm.MsgNotifHeartbeat;

/**
 * @author Vincent Zurczak - Linagora
 */
public class HeartbeatTask extends TimerTask {

	private final Logger logger = Logger.getLogger( getClass().getName());
	private final Agent agent;


	/**
	 * Constructor.
	 * @param agent
	 */
	public HeartbeatTask( Agent agent ) {
		this.agent = agent;
	}


	@Override
	public void run() {
		try {
			MsgNotifHeartbeat heartBeat = new MsgNotifHeartbeat(
					this.agent.getApplicationName(),
					this.agent.getRootInstanceName(),
					this.agent.getIpAddress());

			heartBeat.setModelRequired( this.agent.needsModel());
			this.logger.finer( "Model is required by the agent: " + heartBeat.isModelRequired());

			IAgentClient messagingClient = this.agent.getMessagingClient();
			if( messagingClient != null
					&& messagingClient.isConnected())
				messagingClient.sendMessageToTheDm( heartBeat );

		} catch( IOException e ) {
			this.logger.severe( e.getMessage());
			this.logger.finest( Utils.writeException( e ));
		}
	}
}
