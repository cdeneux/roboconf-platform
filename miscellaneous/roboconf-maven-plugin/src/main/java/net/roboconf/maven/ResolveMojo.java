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

package net.roboconf.maven;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.roboconf.core.Constants;
import net.roboconf.core.utils.Utils;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;

/**
 * The <strong>resolve</strong> mojo.
 * @author Vincent Zurczak - Linagora
 */
@Mojo( name="resolve", defaultPhase = LifecyclePhase.GENERATE_RESOURCES )
public class ResolveMojo extends AbstractMojo {

	@Parameter( defaultValue = "${project}", readonly = true )
	private MavenProject project;

	@Component
	private RepositorySystem repoSystem;

	@Parameter( defaultValue = "${repositorySystemSession}", readonly = true, required = true )
	private RepositorySystemSession repoSession;

	@Parameter( defaultValue = "${project.remoteProjectRepositories}", readonly = true, required = true )
	private List<RemoteRepository> repositories;



	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		// Find the target directory
		File completeAppDirectory = new File( this.project.getBuild().getOutputDirectory());

		// Copy the dependencies resources in the target directory
		Set<Artifact> artifacts = new HashSet<Artifact> ();
		if( this.project.getDependencyArtifacts() != null )
			artifacts.addAll( this.project.getDependencyArtifacts());

		for( Artifact unresolvedArtifact : artifacts ) {

			// Here, it becomes messy. We ask Maven to resolve the artifact's location.
			// It may imply downloading it from a remote repository,
			// searching the local repository or looking into the reactor's cache.

			// To achieve this, we must use Aether
			// (the dependency mechanism behind Maven).
			String artifactId = unresolvedArtifact.getArtifactId();
			org.eclipse.aether.artifact.Artifact aetherArtifact = new DefaultArtifact(
					unresolvedArtifact.getGroupId(),
					unresolvedArtifact.getArtifactId(),
					unresolvedArtifact.getClassifier(),
					unresolvedArtifact.getType(),
					unresolvedArtifact.getVersion());

			ArtifactRequest req = new ArtifactRequest().setRepositories( this.repositories ).setArtifact( aetherArtifact );
			ArtifactResult resolutionResult;
			try {
				resolutionResult = this.repoSystem.resolveArtifact( this.repoSession, req );

			} catch( ArtifactResolutionException e ) {
				throw new MojoExecutionException( "Artifact " + artifactId + " could not be resolved.", e );
			}

			// The file should exists, but we never know.
			File file = resolutionResult.getArtifact().getFile();
			if( file == null || ! file.exists()) {
				getLog().warn( "Artifact " + artifactId + " has no attached file. Its content will not be copied in the target model directory." );
				continue;
			}

			// Prepare the extraction
			File temporaryDirectory = new File( System.getProperty( "java.io.tmpdir" ), "roboconf-temp" );
			File targetDirectory = new File( completeAppDirectory, Constants.PROJECT_DIR_GRAPH + "/" + artifactId );
			getLog().debug( "Copying the content of artifact " + artifactId + " under " + targetDirectory );
			try {

				// Extract graph files - assumed to be at the root of the graph directory
				Utils.extractZipArchive( file, targetDirectory, "graph/[^/]*\\.graph", "graph/" );

				// Extract component files - directories
				Utils.extractZipArchive( file, targetDirectory.getParentFile(), "graph/.*/.*", "graph/" );

			} catch( IOException e ) {
				throw new MojoExecutionException( "The ZIP archive for artifact " + artifactId + " could not be extracted.", e );

			} finally {
				Utils.deleteFilesRecursivelyAndQuietly( temporaryDirectory );
			}
		}
	}
}
