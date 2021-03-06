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

package net.roboconf.core.model;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.roboconf.core.utils.Utils;

/**
 * The descriptor for an application template to deploy with Roboconf.
 * @author Vincent Zurczak - Linagora
 */
public class ApplicationTemplateDescriptor {

	public static final String APPLICATION_NAME = "application-name";
	public static final String APPLICATION_QUALIFIER = "application-qualifier";
	public static final String APPLICATION_DESCRIPTION = "application-description";
	public static final String APPLICATION_DSL_ID = "application-dsl-id";
	public static final String APPLICATION_GRAPH_EP = "graph-entry-point";
	public static final String APPLICATION_INSTANCES_EP = "instance-entry-point";
	public static final String APPLICATION_EXTERNAL_EXPORTS_PREFIX = "exports-prefix";
	public static final String APPLICATION_EXTERNAL_EXPORTS = "exports";
	public static final String APPLICATION_EXTERNAL_EXPORTS_AS = "as";

	private String name, description, qualifier, graphEntryPoint, instanceEntryPoint, dslId, externalExportsPrefix;
	public final Map<String,String> externalExports = new HashMap<> ();
	public final Set<String> invalidExternalExports = new HashSet<> ();


	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName( String name ) {
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription( String description ) {
		this.description = description;
	}

	/**
	 * @return the qualifier
	 */
	public String getQualifier() {
		return this.qualifier;
	}

	/**
	 * @param qualifier the qualifier to set
	 */
	public void setQualifier( String qualifier ) {
		this.qualifier = qualifier;
	}

	/**
	 * @return the externalExportsPrefix
	 */
	public String getExternalExportsPrefix() {
		return this.externalExportsPrefix;
	}

	/**
	 * @param externalExportsPrefix the externalExportsPrefix to set
	 */
	public void setExternalExportsPrefix( String externalExportsId ) {
		this.externalExportsPrefix = externalExportsId;
	}

	/**
	 * @return the graphEntryPoint
	 */
	public String getGraphEntryPoint() {
		return this.graphEntryPoint;
	}

	/**
	 * @param graphEntryPoint the graphEntryPoint to set
	 */
	public void setGraphEntryPoint( String graphEntryPoint ) {
		this.graphEntryPoint = graphEntryPoint;
	}

	/**
	 * @return the instanceEntryPoint
	 */
	public String getInstanceEntryPoint() {
		return this.instanceEntryPoint;
	}

	/**
	 * @param instanceEntryPoint the instanceEntryPoint to set
	 */
	public void setInstanceEntryPoint( String instanceEntryPoint ) {
		this.instanceEntryPoint = instanceEntryPoint;
	}


	/**
	 * @return the dslId
	 */
	public String getDslId() {
		return this.dslId;
	}

	/**
	 * @param dslId the dslId to set
	 */
	public void setDslId( String dslId ) {
		this.dslId = dslId;
	}

	/**
	 * Loads an application descriptor.
	 * @param properties a properties object
	 * @return an application descriptor (not null)
	 */
	public static ApplicationTemplateDescriptor load( Properties properties ) {

		ApplicationTemplateDescriptor result = new ApplicationTemplateDescriptor();
		result.name = properties.getProperty( APPLICATION_NAME, null );
		result.description = properties.getProperty( APPLICATION_DESCRIPTION, null );
		result.qualifier = properties.getProperty( APPLICATION_QUALIFIER, null );
		result.graphEntryPoint = properties.getProperty( APPLICATION_GRAPH_EP, null );
		result.instanceEntryPoint = properties.getProperty( APPLICATION_INSTANCES_EP, null );
		result.dslId = properties.getProperty( APPLICATION_DSL_ID, null );
		result.externalExportsPrefix = properties.getProperty( APPLICATION_EXTERNAL_EXPORTS_PREFIX, null );

		final Pattern pattern = Pattern.compile(
				"([^=\\s]+)\\s+" + APPLICATION_EXTERNAL_EXPORTS_AS + "\\s+([^=\\s]+)",
				Pattern.CASE_INSENSITIVE );

		String rawExports = properties.getProperty( APPLICATION_EXTERNAL_EXPORTS, "" );
		for( String rawExport : Utils.splitNicely( rawExports, "," )) {
			Matcher m = pattern.matcher( rawExport );
			if( m.matches())
				result.externalExports.put( m.group( 1 ), m.group( 2 ));
			else
				result.invalidExternalExports.add( rawExport );
		}

		return result;
	}


	/**
	 * Loads an application template's descriptor.
	 * @param f a file
	 * @return an application descriptor (not null)
	 * @throws IOException if the file could not be read
	 */
	public static ApplicationTemplateDescriptor load( File f ) throws IOException {

		Properties properties = Utils.readPropertiesFile( f );
		if( properties.get( "fail.read" ) != null )
			throw new IOException( "This is for test purpose..." );

		return load( properties );
	}


	/**
	 * Saves an application template's descriptor.
	 * @param f the file where the properties will be saved
	 * @param descriptor an application descriptor (not null)
	 * @throws IOException if the file could not be written
	 */
	public static void save( File f, ApplicationTemplateDescriptor descriptor ) throws IOException {

		Properties properties = new Properties();
		if( descriptor.name != null )
			properties.setProperty( APPLICATION_NAME, descriptor.name );

		if( descriptor.qualifier != null )
			properties.setProperty( APPLICATION_QUALIFIER, descriptor.qualifier );

		if( descriptor.dslId != null )
			properties.setProperty( APPLICATION_DSL_ID, descriptor.dslId );

		if( descriptor.description != null )
			properties.setProperty( APPLICATION_DESCRIPTION, descriptor.description );

		if( descriptor.graphEntryPoint != null )
			properties.setProperty( APPLICATION_GRAPH_EP, descriptor.graphEntryPoint );

		if( descriptor.instanceEntryPoint != null )
			properties.setProperty( APPLICATION_INSTANCES_EP, descriptor.instanceEntryPoint );

		if( descriptor.externalExportsPrefix != null )
			properties.setProperty( APPLICATION_EXTERNAL_EXPORTS_PREFIX, descriptor.externalExportsPrefix );

		StringBuilder sb = new StringBuilder();
		for( Iterator<Map.Entry<String,String>> it = descriptor.externalExports.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<String,String> entry = it.next();
			sb.append( entry.getKey());
			sb.append( " " );
			sb.append( APPLICATION_EXTERNAL_EXPORTS_AS );
			sb.append( " " );
			sb.append( entry.getValue());
			if( it.hasNext())
				sb.append( ", " );
		}

		if( sb.length() > 0 )
			properties.setProperty( APPLICATION_EXTERNAL_EXPORTS, sb.toString());

		Utils.writePropertiesFile( properties, f );
	}
}
