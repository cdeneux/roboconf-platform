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

package net.roboconf.target.azure.internal;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;
import net.roboconf.target.api.TargetException;

import org.junit.Test;

/**
 * @author Linh-Manh Pham - LIG
 */
public class AzureIaasHandlerTest {

	@Test( expected = TargetException.class )
	public void testInvalidConfiguration() throws Exception {

		AzureIaasHandler target = new AzureIaasHandler();
		target.setTargetProperties( new HashMap<String,String> ());
	}


	@Test
	public void testValidConfiguration() throws Exception {

		Map<String, String> targetProperties = new HashMap<String,String> ();
		AzureIaasHandler target = new AzureIaasHandler();

		targetProperties.put( AzureConstants.AZURE_SUBSCRIPTION_ID, "my subscription id" );
		targetProperties.put( AzureConstants.AZURE_KEY_STORE_FILE, "path to key store file" );
		targetProperties.put( AzureConstants.AZURE_KEY_STORE_PASSWORD, "key store password" );
		targetProperties.put( AzureConstants.AZURE_CREATE_CLOUD_SERVICE_TEMPLATE, "create cloud service template" );
		targetProperties.put( AzureConstants.AZURE_CREATE_DEPLOYMENT_TEMPLATE, "create deployment template" );
		targetProperties.put( AzureConstants.AZURE_LOCATION, "azure location" );
		targetProperties.put( AzureConstants.AZURE_VM_SIZE, "azure VM size" );
		targetProperties.put( AzureConstants.AZURE_VM_TEMPLATE, "azure VM template" );

		target.setTargetProperties( targetProperties );
	}


	@Test
	public void testGetTargetId() {
		Assert.assertEquals( AzureIaasHandler.TARGET_ID, new AzureIaasHandler().getTargetId());
	}
}
