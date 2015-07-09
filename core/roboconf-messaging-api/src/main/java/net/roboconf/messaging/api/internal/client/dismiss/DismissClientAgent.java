/**
 * Copyright 2014-2015 Linagora, Université Joseph Fourier, Floralis
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

package net.roboconf.messaging.api.internal.client.dismiss;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import net.roboconf.core.model.beans.Instance;
import net.roboconf.messaging.api.MessagingConstants;
import net.roboconf.messaging.api.client.IAgentClient;
import net.roboconf.messaging.api.client.ListenerCommand;
import net.roboconf.messaging.api.messages.Message;

/**
 * @author Vincent Zurczak - Linagora
 */
public class DismissClientAgent implements IAgentClient {

	private final Logger logger = Logger.getLogger( getClass().getName());


	@Override
	public void setMessageQueue( LinkedBlockingQueue<Message> messageQueue ) {
		this.logger.warning( MessagingConstants.DISMISSED_MESSAGE );
	}


	@Override
	public boolean isConnected() {
		this.logger.warning( MessagingConstants.DISMISSED_MESSAGE );
		return false;
	}


	@Override
	public void openConnection() throws IOException {
		this.logger.warning( MessagingConstants.DISMISSED_MESSAGE );
	}


	@Override
	public void closeConnection() throws IOException {
		this.logger.warning( MessagingConstants.DISMISSED_MESSAGE );
	}


	@Override
	public void setApplicationName( String applicationName ) {
		this.logger.warning( MessagingConstants.DISMISSED_MESSAGE );
	}


	@Override
	public void setScopedInstancePath( String scopedInstancePath ) {
		this.logger.warning( MessagingConstants.DISMISSED_MESSAGE );
	}


	@Override
	public void publishExports( Instance instance ) throws IOException {
		this.logger.warning( MessagingConstants.DISMISSED_MESSAGE );
	}


	@Override
	public void publishExports( Instance instance, String facetOrComponentName ) throws IOException {
		this.logger.warning( MessagingConstants.DISMISSED_MESSAGE );
	}


	@Override
	public void unpublishExports( Instance instance ) throws IOException {
		this.logger.warning( MessagingConstants.DISMISSED_MESSAGE );
	}


	@Override
	public void listenToRequestsFromOtherAgents( ListenerCommand command, Instance instance ) throws IOException {
		this.logger.warning( MessagingConstants.DISMISSED_MESSAGE );
	}


	@Override
	public void requestExportsFromOtherAgents( Instance instance ) throws IOException {
		this.logger.warning( MessagingConstants.DISMISSED_MESSAGE );
	}


	@Override
	public void listenToExportsFromOtherAgents( ListenerCommand command, Instance instance ) throws IOException {
		this.logger.warning( MessagingConstants.DISMISSED_MESSAGE );
	}


	@Override
	public void sendMessageToTheDm( Message message ) throws IOException {
		this.logger.warning( MessagingConstants.DISMISSED_MESSAGE );
	}


	@Override
	public void listenToTheDm( ListenerCommand command ) throws IOException {
		this.logger.warning( MessagingConstants.DISMISSED_MESSAGE );
	}


	@Override
	public String getMessagingType() {
		// Dismiss client has no type.
		return null;
	}


	@Override
	public Map<String, String> getConfiguration() {
		// Dismiss client has no configuration.
		return Collections.emptyMap();
	}
}
