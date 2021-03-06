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

package net.roboconf.core.dsl.parsing;

import java.io.File;

import org.junit.Assert;
import net.roboconf.core.dsl.parsing.BlockBlank;
import net.roboconf.core.dsl.parsing.BlockComment;
import net.roboconf.core.dsl.parsing.BlockComponent;
import net.roboconf.core.dsl.parsing.BlockFacet;
import net.roboconf.core.dsl.parsing.BlockImport;
import net.roboconf.core.dsl.parsing.BlockProperty;
import net.roboconf.core.dsl.parsing.FileDefinition;

import org.junit.Test;

/**
 * @author Vincent Zurczak - Linagora
 */
public class CrapForCodeCoverageTest {

	@Test
	public void testCrap() {
		FileDefinition def = new FileDefinition( new File( "whatever" ));

		// Imports
		BlockImport imp = new BlockImport( def );
		Assert.assertNotNull( imp.toString());

		imp.setUri( "http://an-import.uri" );
		Assert.assertNotNull( imp.toString());

		// Comment
		BlockComment comment = new BlockComment( imp.getDeclaringFile(), "we don't ware" );
		Assert.assertNotNull( comment.toString());

		comment = new BlockComment( def, null );
		Assert.assertNotNull( comment.toString());

		// Component
		BlockComponent comp = new BlockComponent( def );
		Assert.assertNull( comp.toString());

		comp.setName( "woo" );
		Assert.assertEquals( "woo", comp.toString());

		// Facet
		BlockFacet facet = new BlockFacet( def );
		Assert.assertNull( facet.toString());

		facet.setName( "woo 2" );
		Assert.assertEquals( "woo 2", facet.toString());

		// Block Property
		BlockProperty prop = new BlockProperty( def );
		Assert.assertNotNull( prop.toString());

		prop.setName( "you" );
		Assert.assertNotNull( prop.toString());

		prop.setValue( "whatever" );
		Assert.assertNotNull( prop.toString());

		// Blank
		BlockBlank blank = new BlockBlank( def, null );
		Assert.assertNotNull( blank.toString());

		blank = new BlockBlank( def, "..." );
		Assert.assertNotNull( blank.toString());
	}


	@Test( expected = IllegalArgumentException.class )
	public void testInvalidDefinition() {
		new BlockProperty( null );
	}
}
