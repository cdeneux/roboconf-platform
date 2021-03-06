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

package net.roboconf.dm.management.events;

import net.roboconf.core.model.beans.Application;
import net.roboconf.core.model.beans.ApplicationTemplate;
import net.roboconf.core.model.beans.Instance;

/**
 * An interface to propagate events generated by the DM.
 * @author Vincent Zurczak - Linagora
 */
public interface IDmListener {

	/**
	 * Returns a string to identify the listener in the logs.
	 * @return a non-null string
	 */
	String getId();

	/**
	 * Enables the notifications.
	 */
	void enableNotifications();

	/**
	 * Disables the notifications.
	 */
	void disableNotifications();

	/**
	 * Invoked when an application is modified.
	 * @param application the modified application
	 * @param eventType the event type
	 */
	void application( Application application, EventType eventType );

	/**
	 * Invoked when an application template is modified.
	 * @param tpl the modified application template
	 * @param eventType the event type
	 */
	void applicationTemplate( ApplicationTemplate tpl, EventType eventType );

	/**
	 * Invoked when an instance is modified.
	 * @param instance the modified instance
	 * @param application the instance's application
	 * @param eventType the event type
	 */
	void instance( Instance instance, Application application, EventType eventType );

	/**
	 * Invoked to signal a raw event.
	 * @param message a message
	 * @param data optional data
	 */
	void raw( String message, Object... data );
}
