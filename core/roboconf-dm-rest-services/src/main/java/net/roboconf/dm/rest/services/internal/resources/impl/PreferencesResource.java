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

package net.roboconf.dm.rest.services.internal.resources.impl;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import net.roboconf.core.model.runtime.Preference;
import net.roboconf.dm.management.Manager;
import net.roboconf.dm.rest.services.internal.RestServicesUtils;
import net.roboconf.dm.rest.services.internal.resources.IPreferencesResource;

/**
 * @author Vincent Zurczak - Linagora
 */
@Path( IPreferencesResource.PATH )
public class PreferencesResource implements IPreferencesResource {

	private final Logger logger = Logger.getLogger( getClass().getName());
	private final Manager manager;


	/**
	 * Constructor.
	 * @param manager the manager
	 */
	public PreferencesResource( Manager manager ) {
		this.manager = manager;
	}


	@Override
	public List<Preference> getAllPreferences() {

		this.logger.fine( "Request: get all the preferences." );
		return this.manager.preferencesMngr().getAllPreferences();
	}


	@Override
	public Response savePreference( String key, String value ) {

		this.logger.fine( "Request: save preference " + key + " with value " + value );
		Response response;
		try {
			this.manager.preferencesMngr().save( key, value );
			response = Response.ok().build();

		} catch( IOException e ) {
			response = RestServicesUtils.handleException( this.logger, Status.INTERNAL_SERVER_ERROR, null, e ).build();
		}

		return response;
	}
}
