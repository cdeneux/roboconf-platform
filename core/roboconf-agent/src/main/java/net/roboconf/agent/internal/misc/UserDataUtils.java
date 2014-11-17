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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.roboconf.agent.internal.AgentProperties;
import net.roboconf.core.utils.Utils;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Noël - LIG
 * @author Pierre-Yves Gibello - Linagora
 */
public final class UserDataUtils {

	/**
	 * Private empty constructor.
	 */
	private UserDataUtils() {
		// nothing
	}


	/**
	 * Configures the agent from a IaaS registry.
	 * @param logger a logger
	 * @return the agent's data
	 */
	public static AgentProperties findParametersForAmazonOrOpenStack( Logger logger ) {

		// Copy the user data
		String userData = "";
		InputStream in = null;
		try {
			URL userDataUrl = new URL( "http://169.254.169.254/latest/user-data" );
			in = userDataUrl.openStream();
			ByteArrayOutputStream os = new ByteArrayOutputStream();

			Utils.copyStream( in, os );
			userData = os.toString( "UTF-8" );

		} catch( IOException e ) {
			logger.severe( "The agent properties could not be read. " + e.getMessage());
			logger.finest( Utils.writeException( e ));

		} finally {
			Utils.closeQuietly( in );
		}

		// Parse them
		AgentProperties result = AgentProperties.readIaasProperties( userData, logger );

		// We need to ask our IP address because we may have several network interfaces.
		in = null;
		try {
			URL userDataUrl = new URL( "http://169.254.169.254/latest/meta-data/public-ipv4" );
			in = userDataUrl.openStream();
			ByteArrayOutputStream os = new ByteArrayOutputStream();

			Utils.copyStream( in, os );
			String ip = os.toString( "UTF-8" );
			if(! AgentUtils.isValidIP(ip)) {
				// Failed retrieving public IP: try private one instead
				Utils.closeQuietly( in );
				userDataUrl = new URL( "http://169.254.169.254/latest/meta-data/local-ipv4" );
				in = userDataUrl.openStream();
				os = new ByteArrayOutputStream();

				Utils.copyStream( in, os );
				ip = os.toString( "UTF-8" );
			}

			if(! AgentUtils.isValidIP(ip))
				throw new IOException("No IP address could be retrieved (either public-ipv4 or local-ipv4)");

			result.setIpAddress( os.toString( "UTF-8" ));

		} catch( IOException e ) {
			logger.severe( "The network properties could not be read. " + e.getMessage());
			logger.finest( Utils.writeException( e ));

		} finally {
			Utils.closeQuietly( in );
		}

		return result;
	}


	/**
	 * Configures the agent from Azure.
	 * @param logger a logger
	 * @return the agent's data
	 */
	public static AgentProperties findParametersForAzure( Logger logger ) {

		String userData = "";
		try {
			// Get the user data from /var/lib/waagent/ovf-env.xml and decode it
			String userDataEncoded = getValueOfTagInXMLFile( "/var/lib/waagent/ovf-env.xml", "CustomData" );
			userData = new String( Base64.decodeBase64( userDataEncoded.getBytes( "UTF-8" )), "UTF-8" );

		} catch( IOException e ) {
			logger.severe( "The agent properties could not be read. " + e.getMessage());
			logger.finest( Utils.writeException( e ));

		} catch( ParserConfigurationException e ) {
			logger.severe( "The agent properties could not be read. " + e.getMessage());
			logger.finest( Utils.writeException( e ));

		} catch( SAXException e ) {
			logger.severe( "The agent properties could not be read. " + e.getMessage());
			logger.finest( Utils.writeException( e ));
		}

		// Parse them
		AgentProperties result = AgentProperties.readIaasProperties( userData, logger );

		// Get the public IP Address from /var/lib/waagent/SharedConfig.xml
		String publicIPAddress;
		try {
			publicIPAddress = getSpecificAttributeOfTagInXMLFile( "/var/lib/waagent/SharedConfig.xml", "Endpoint", "loadBalancedPublicAddress" );
			result.setIpAddress( publicIPAddress );

		} catch( ParserConfigurationException e ) {
			logger.severe( "The agent could not retrieve a public IP address. " + e.getMessage());
			logger.finest( Utils.writeException( e ));

		} catch( SAXException e ) {
			logger.severe( "The agent could not retrieve a public IP address. " + e.getMessage());
			logger.finest( Utils.writeException( e ));

		} catch( IOException e ) {
			logger.severe( "The agent could not retrieve a public IP address. " + e.getMessage());
			logger.finest( Utils.writeException( e ));
		}

		return result;
	}


	// FIXME: there must be a shorter way with XPath...
	private static String getValueOfTagInXMLFile(String filePath, String tagName)
	throws ParserConfigurationException, SAXException, IOException {

		File fXmlFile = new File(filePath);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);

		// Optional, but recommended
		// Read this: http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
		doc.getDocumentElement().normalize();

		NodeList nList = doc.getElementsByTagName(tagName);
		String valueOfTagName = "";

		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);
			valueOfTagName = nNode.getTextContent();
		}

		return valueOfTagName;
	}


	private static String getSpecificAttributeOfTagInXMLFile(String filePath, String tagName, String attrName)
	throws ParserConfigurationException, SAXException, IOException {

		File fXmlFile = new File(filePath);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);

		doc.getDocumentElement().normalize();

		NodeList nList = doc.getElementsByTagName(tagName);
		Node aNode = nList.item(2);
		NamedNodeMap attributes = aNode.getAttributes();
		String attrValue = "";
		for (int a = 0; a < attributes.getLength(); a++) {
			Node theAttribute = attributes.item(a);
			if (attrName.equals(theAttribute.getNodeName()))
				attrValue = theAttribute.getTextContent().split(":")[0];
		}

		return attrValue;
	}
}