/**
 * Copyright 2015 Linagora, Université Joseph Fourier, Floralis
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

package net.roboconf.dm.internal.api.impl;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import junit.framework.Assert;
import net.roboconf.core.Constants;
import net.roboconf.core.internal.tests.TestUtils;
import net.roboconf.core.model.ApplicationTemplateDescriptor;
import net.roboconf.core.model.beans.ApplicationTemplate;
import net.roboconf.core.utils.Utils;
import net.roboconf.dm.internal.utils.ConfigurationUtils;
import net.roboconf.dm.management.api.IApplicationMngr;
import net.roboconf.dm.management.api.IApplicationTemplateMngr;
import net.roboconf.dm.management.api.IConfigurationMngr;
import net.roboconf.dm.management.api.INotificationMngr;
import net.roboconf.dm.management.api.ITargetsMngr;
import net.roboconf.dm.management.exceptions.AlreadyExistingException;
import net.roboconf.dm.management.exceptions.InvalidApplicationException;
import net.roboconf.dm.management.exceptions.UnauthorizedActionException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

/**
 * @author Vincent Zurczak - Linagora
 */
public class ApplicationTemplateMngrImplTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private IApplicationTemplateMngr mngr;
	private IApplicationMngr applicationMngr;
	private IConfigurationMngr configurationMngr;
	private ITargetsMngr targetsMngr;
	private File dmDirectory;


	@Before
	public void prepareTemplateManager() throws Exception {

		INotificationMngr notificationMngr = Mockito.mock( INotificationMngr.class );

		this.targetsMngr = Mockito.mock( ITargetsMngr.class );
		this.applicationMngr = Mockito.mock( IApplicationMngr.class );
		this.configurationMngr = Mockito.mock( IConfigurationMngr.class );
		this.mngr = new ApplicationTemplateMngrImpl( notificationMngr, this.targetsMngr, this.applicationMngr, this.configurationMngr );

		this.dmDirectory = this.folder.newFolder();
		Mockito.when( this.configurationMngr.getWorkingDirectory()).thenReturn( this.dmDirectory );
	}


	@Test
	@SuppressWarnings( "unchecked" )
	public void testFindTemplate() throws Exception {

		Map<ApplicationTemplate,Boolean> map = TestUtils.getInternalField( this.mngr, "templates", Map.class );

		Assert.assertNull( this.mngr.findTemplate( "lamp", null ));
		ApplicationTemplate tpl = new ApplicationTemplate( "lamp" );
		map.put( tpl, Boolean.TRUE );
		Assert.assertEquals( tpl, this.mngr.findTemplate( "lamp", null ));

		ApplicationTemplate tpl2 = new ApplicationTemplate( "lamp" ).qualifier( "v2" );
		map.put( tpl2, Boolean.TRUE );

		Assert.assertFalse( tpl.equals( tpl2 ));
		Assert.assertEquals( tpl2, this.mngr.findTemplate( "lamp", "v2" ));
		Assert.assertEquals( tpl, this.mngr.findTemplate( "lamp", null ));
		Assert.assertNull( this.mngr.findTemplate( "lamp", "v3" ));
		Assert.assertNull( this.mngr.findTemplate( "lamp2", null ));
	}


	@Test
	public void testRestoreTemplates_noTemplate_noTplDirectory() throws Exception {

		Assert.assertEquals( 0, this.mngr.getApplicationTemplates().size());
		this.mngr.restoreTemplates();
		Assert.assertEquals( 0, this.mngr.getApplicationTemplates().size());
	}


	@Test
	public void testRestoreTemplates_noTemplate_withTplDirectory() throws Exception {

		Assert.assertTrue( new File( this.dmDirectory, ConfigurationUtils.TEMPLATES ).mkdir());
		Assert.assertEquals( 0, this.mngr.getApplicationTemplates().size());
		this.mngr.restoreTemplates();
		Assert.assertEquals( 0, this.mngr.getApplicationTemplates().size());
	}


	@Test
	public void testRestoreTemplates_withTemplate() throws Exception {

		File tplDir = new File( this.dmDirectory, ConfigurationUtils.TEMPLATES + "/Legacy LAMP - sample" );
		Assert.assertTrue( tplDir.mkdirs());

		File toCopy = TestUtils.findApplicationDirectory( "lamp" );
		Utils.copyDirectory( toCopy, tplDir );

		// The VM directory cannot exist for this test
		File toDelete = new File( tplDir, "graph/VM" );
		Assert.assertFalse( toDelete.isDirectory());

		Assert.assertEquals( 0, this.mngr.getApplicationTemplates().size());
		this.mngr.restoreTemplates();
		Assert.assertEquals( 1, this.mngr.getApplicationTemplates().size());

		ApplicationTemplate tpl = this.mngr.getApplicationTemplates().iterator().next();
		Assert.assertEquals( "Legacy LAMP", tpl.getName());
		Assert.assertEquals( "sample", tpl.getQualifier());
		Assert.assertNotNull( tpl.getGraphs());
		Assert.assertFalse( tpl.getRootInstances().isEmpty());
	}


	@Test
	public void testRestoreTemplates_withConflict() throws Exception {

		// Copy the template twice
		File tplDir1 = new File( this.dmDirectory, ConfigurationUtils.TEMPLATES + "/Legacy LAMP - sample" );
		Assert.assertTrue( tplDir1.mkdirs());

		File tplDir2 = new File( this.dmDirectory, ConfigurationUtils.TEMPLATES + "/Legacy LAMP - sample copy" );
		Assert.assertTrue( tplDir2.mkdirs());

		File toCopy = TestUtils.findApplicationDirectory( "lamp" );
		Utils.copyDirectory( toCopy, tplDir1 );
		Utils.copyDirectory( toCopy, tplDir2 );

		// The VM directory cannot exist for this test
		File toDelete = new File( tplDir1, "graph/VM" );
		Assert.assertFalse( toDelete.isDirectory());

		Assert.assertEquals( 0, this.mngr.getApplicationTemplates().size());
		this.mngr.restoreTemplates();
		Assert.assertEquals( 1, this.mngr.getApplicationTemplates().size());

		ApplicationTemplate tpl = this.mngr.getApplicationTemplates().iterator().next();
		Assert.assertEquals( "Legacy LAMP", tpl.getName());
		Assert.assertEquals( "sample", tpl.getQualifier());
		Assert.assertNotNull( tpl.getGraphs());
		Assert.assertFalse( tpl.getRootInstances().isEmpty());
	}


	@Test
	public void testRestoreTemplates_withError() throws Exception {

		File tplDir = new File( this.dmDirectory, ConfigurationUtils.TEMPLATES + "/Legacy LAMP - sample" );
		Assert.assertTrue( tplDir.mkdirs());

		Assert.assertEquals( 0, this.mngr.getApplicationTemplates().size());
		this.mngr.restoreTemplates();
		Assert.assertEquals( 0, this.mngr.getApplicationTemplates().size());
	}


	@Test( expected = InvalidApplicationException.class )
	public void testDeleteApplicationTemplate_inexisting() throws Exception {

		this.mngr.deleteApplicationTemplate( "whatever", "version" );
	}


	@Test
	public void testDeleteApplicationTemplate_success() throws Exception {

		File directory = TestUtils.findApplicationDirectory( "lamp" );
		Assert.assertTrue( directory.exists());
		Assert.assertEquals( 0, this.mngr.getApplicationTemplates().size());

		ApplicationTemplate tpl = this.mngr.loadApplicationTemplate( directory );
		Assert.assertNotNull( tpl );
		Assert.assertEquals( 1, this.mngr.getApplicationTemplates().size());

		this.mngr.deleteApplicationTemplate( tpl.getName(), tpl.getQualifier());
		Assert.assertEquals( 0, this.mngr.getApplicationTemplates().size());
	}


	@Test( expected = UnauthorizedActionException.class )
	public void testDeleteApplicationTemplate_unauthorized() throws Exception {

		File directory = TestUtils.findApplicationDirectory( "lamp" );
		Assert.assertTrue( directory.exists());
		Assert.assertEquals( 0, this.mngr.getApplicationTemplates().size());

		ApplicationTemplate tpl = this.mngr.loadApplicationTemplate( directory );
		Assert.assertNotNull( tpl );
		Assert.assertEquals( 1, this.mngr.getApplicationTemplates().size());

		Mockito.when( this.applicationMngr.isTemplateUsed( tpl )).thenReturn( true );
		this.mngr.deleteApplicationTemplate( tpl.getName(), tpl.getQualifier());
	}


	@Test( expected = AlreadyExistingException.class )
	public void testLoadApplicationTemplate_conflict() throws Exception {

		File directory = TestUtils.findApplicationDirectory( "lamp" );
		Assert.assertTrue( directory.exists());
		try {
			this.mngr.loadApplicationTemplate( directory );

		} catch( Exception e ) {
			Assert.fail( "Loading the application the first time should not fail." );
		}

		this.mngr.loadApplicationTemplate( directory );
	}


	@Test( expected = IOException.class )
	public void testLoadApplicationTemplate_externalExportsPrefixIsAlreadyUsed() throws Exception {

		// We need to modify the descriptor, so work on a copy
		File originalDirectory = TestUtils.findApplicationDirectory( "lamp" );
		Assert.assertTrue( originalDirectory.exists());

		File directoryCopy = this.folder.newFolder();
		Utils.copyDirectory( originalDirectory, directoryCopy );

		// Update the external export ID
		File descriptorFile = new File( directoryCopy, Constants.PROJECT_DIR_DESC + "/" + Constants.PROJECT_FILE_DESCRIPTOR );
		Assert.assertTrue( descriptorFile.exists());
		ApplicationTemplateDescriptor desc = ApplicationTemplateDescriptor.load( descriptorFile );
		desc.setExternalExportsPrefix( "for-test" );

		ApplicationTemplateDescriptor.save( descriptorFile, desc );
		try {
			this.mngr.loadApplicationTemplate( directoryCopy );

		} catch( Exception e ) {
			Assert.fail( "Loading the application the first time should not fail." );
		}

		// Change the qualifier in the files
		desc.setQualifier( "v33.2" );
		ApplicationTemplateDescriptor.save( descriptorFile, desc );
		this.mngr.loadApplicationTemplate( directoryCopy );
	}


	@Test
	public void testLoadApplicationTemplate_externalExportsPrefixDoNotConflict() throws Exception {

		// We need to modify the descriptor, so work on a copy
		File originalDirectory = TestUtils.findApplicationDirectory( "lamp" );
		Assert.assertTrue( originalDirectory.exists());

		File directoryCopy = this.folder.newFolder();
		Utils.copyDirectory( originalDirectory, directoryCopy );

		// Update the external export ID
		File descriptorFile = new File( directoryCopy, Constants.PROJECT_DIR_DESC + "/" + Constants.PROJECT_FILE_DESCRIPTOR );
		Assert.assertTrue( descriptorFile.exists());
		ApplicationTemplateDescriptor desc = ApplicationTemplateDescriptor.load( descriptorFile );
		desc.setExternalExportsPrefix( "for-test" );

		ApplicationTemplateDescriptor.save( descriptorFile, desc );
		Assert.assertEquals( 0, this.mngr.getApplicationTemplates().size());
		try {
			this.mngr.loadApplicationTemplate( directoryCopy );

		} catch( Exception e ) {
			Assert.fail( "Loading the application the first time should not fail." );
		}

		Assert.assertEquals( 1, this.mngr.getApplicationTemplates().size());

		// Change the qualifier in the files
		desc.setQualifier( "v33.2" );
		desc.setExternalExportsPrefix( null );
		ApplicationTemplateDescriptor.save( descriptorFile, desc );
		this.mngr.loadApplicationTemplate( directoryCopy );

		Assert.assertEquals( 2, this.mngr.getApplicationTemplates().size());
	}


	@Test( expected = IOException.class )
	public void testLoadApplicationTemplate_invalidDirectory() throws Exception {

		File source = TestUtils.findApplicationDirectory( "lamp" );
		Assert.assertTrue( source.exists());

		File apps = new File( this.dmDirectory, ConfigurationUtils.TEMPLATES );
		File target = new File( apps, "Legacy LAMP - sample/sub/dir" );
		Utils.copyDirectory( source, target );
		this.mngr.loadApplicationTemplate( target );
	}


	@Test( expected = InvalidApplicationException.class )
	public void testApplicationTemplate_invalidTemplate() throws Exception {
		this.mngr.loadApplicationTemplate( this.folder.newFolder());
	}


	@Test
	public void testRegisterTargets_noTarget() throws Exception {

		File tplDir = new File( this.dmDirectory, ConfigurationUtils.TEMPLATES + "/Legacy LAMP - sample" );
		Assert.assertTrue( tplDir.mkdirs());

		File toCopy = TestUtils.findApplicationDirectory( "lamp" );
		Utils.copyDirectory( toCopy, tplDir );

		// The VM directory cannot exist for this test
		File toDelete = new File( tplDir, "graph/VM" );
		Assert.assertFalse( toDelete.isDirectory());

		Assert.assertEquals( 0, this.mngr.getApplicationTemplates().size());
		ApplicationTemplate tpl = this.mngr.loadApplicationTemplate( tplDir );
		Assert.assertEquals( 1, this.mngr.getApplicationTemplates().size());

		Assert.assertEquals( "Legacy LAMP", tpl.getName());
		Assert.assertEquals( "sample", tpl.getQualifier());
		Assert.assertNotNull( tpl.getGraphs());
		Assert.assertFalse( tpl.getRootInstances().isEmpty());

		Mockito.verifyZeroInteractions( this.targetsMngr );
	}


	@Test
	public void testRegisterTargets_withTarget() throws Exception {

		File tplDir = new File( this.dmDirectory, ConfigurationUtils.TEMPLATES + "/Legacy LAMP - sample" );
		Assert.assertTrue( tplDir.mkdirs());

		File toCopy = TestUtils.findApplicationDirectory( "lamp" );
		Utils.copyDirectory( toCopy, tplDir );

		// The VM directory MUST exist for this test
		File targetPropertiesFile = new File( tplDir, "graph/VM/" + Constants.TARGET_PROPERTIES_FILE_NAME );
		Assert.assertTrue( targetPropertiesFile.getParentFile().mkdirs());
		Utils.writeStringInto( "", targetPropertiesFile );

		Mockito.when( this.targetsMngr.createTarget( Mockito.any( File.class ))).thenReturn( "the_id" );

		Assert.assertEquals( 0, this.mngr.getApplicationTemplates().size());
		ApplicationTemplate tpl = this.mngr.loadApplicationTemplate( tplDir );
		Assert.assertEquals( 1, this.mngr.getApplicationTemplates().size());

		Assert.assertEquals( "Legacy LAMP", tpl.getName());
		Assert.assertEquals( "sample", tpl.getQualifier());
		Assert.assertNotNull( tpl.getGraphs());
		Assert.assertFalse( tpl.getRootInstances().isEmpty());

		Mockito.verify( this.targetsMngr, Mockito.times( 1 )).createTarget( Mockito.any( File.class ));
		Mockito.verify( this.targetsMngr, Mockito.times( 1 )).addHint( "the_id", tpl );
	}
}
