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

package net.roboconf.messaging.http.internal.messages;

import java.io.Serializable;

import net.roboconf.messaging.api.extensions.MessagingContext;
import net.roboconf.messaging.api.messages.Message;

/**
 * A message with additional meta-data useful for HTTP messaging.
 * @author Pierre-Yves Gibello - Linagora
 */
public class HttpMessage extends Message implements Serializable {

	private static final long serialVersionUID = -1012611751797601185L;

	private final String ownerId;
	private final Message message;
	private final MessagingContext ctx;


	/**
	 * Constructor.
	 * @param ownerId who sent the message
	 * @param message the wrapped message
	 * @param ctx the message recipient(s)
	 */
	public HttpMessage( String ownerId, Message message, MessagingContext ctx ) {
		this.ownerId = ownerId;
		this.message = message;
		this.ctx = ctx;
	}

	public String getOwnerId() {
		return this.ownerId;
	}

	public Message getMessage() {
		return this.message;
	}

	public MessagingContext getCtx() {
		return this.ctx;
	}
}
