package com.soebes.maven.extensions.incremental;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.project.MavenProject;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ModuleCalculatorTest
{

    private File baseDir = new File( "/usr/local/project/" );

    private List<MavenProject> projectList;

    private MavenProject parent = createProject( "parent", new File( baseDir, "." ) );

    private MavenProject assembly = createProject( "assembly", new File( baseDir, "assembly" ) );

    private MavenProject domain = createProject( "domain", new File( baseDir, "domain" ) );

    private MavenProject subdomain = createProject( "subdomain", new File( baseDir, "domain/subdomain" ) );

    private MavenProject subsubdomain = createProject( "subsubdomain", new File( baseDir, "domain/subdomain/subsubdomain" ) );

    private ModuleCalculator moduleCalculator;

    @Before
    public void before()
    {
        projectList = new ArrayList<>();
        projectList.add( parent );
        projectList.add( assembly );
        projectList.add( domain );
        projectList.add( subdomain );
        projectList.add( subsubdomain );
    }

    private MavenProject createProject( String artifactId, File baseDir )
    {
        MavenProject mock = mock( MavenProject.class );
        when( mock.getGroupId() ).thenReturn( "c.s.e" );
        when( mock.getArtifactId() ).thenReturn( artifactId );
        when( mock.getVersion() ).thenReturn( "0.1.0-SNAPSHOT" );
        when( mock.getBasedir() ).thenReturn( baseDir );
        return mock;
    }

    @Test
    public void shouldResultInASingleModule()
    {
        Path root = baseDir.toPath();
        List<ScmFile> changeList =
            Arrays.asList( new ScmFile( "domain/src/main/java/com/test.java", ScmFileStatus.MODIFIED ) );
        moduleCalculator = new ModuleCalculator( projectList, changeList );
        List<MavenProject> changedModules = moduleCalculator.calculateChangedModules( root );

        assertThat( changedModules ).hasSize( 1 ).containsExactly( domain );
    }

    @Test
    public void shouldResultInTwoModules()
    {
        Path root = baseDir.toPath();
        List<ScmFile> changeList =
            Arrays.asList( new ScmFile( "domain/src/main/java/com/test.java", ScmFileStatus.MODIFIED ),
                           new ScmFile( "assembly/pom.xml", ScmFileStatus.MODIFIED ) );
        moduleCalculator = new ModuleCalculator( projectList, changeList );
        List<MavenProject> changedModules = moduleCalculator.calculateChangedModules( root );

        assertThat( changedModules ).hasSize( 2 ).containsOnly( domain, assembly );
    }

    @Test
    public void shouldResultInTwoModulesTwoChangesInSingleModule()
    {
        Path root = baseDir.toPath();
        List<ScmFile> changeList =
            Arrays.asList( new ScmFile( "domain/src/main/java/com/test.java", ScmFileStatus.MODIFIED ),
                           new ScmFile( "domain/src/main/java/Anton.java", ScmFileStatus.MODIFIED ),
                           new ScmFile( "assembly/pom.xml", ScmFileStatus.MODIFIED ) );

        moduleCalculator = new ModuleCalculator( projectList, changeList );
        List<MavenProject> changedModules = moduleCalculator.calculateChangedModules( root );

        assertThat( changedModules ).hasSize( 2 ).containsOnly( domain, assembly );
    }

    @Test
    public void shouldResultInTwoModulesDomainAndSubDomain()
    {
        Path root = baseDir.toPath();
        List<ScmFile> changeList = Arrays.asList( new ScmFile( "domain/subdomain/pom.xml", ScmFileStatus.MODIFIED ),
                                                  new ScmFile( "domain/pom.xml", ScmFileStatus.MODIFIED ) );
        moduleCalculator = new ModuleCalculator( projectList, changeList );
        List<MavenProject> changedModules = moduleCalculator.calculateChangedModules( root );

        assertThat( changedModules ).hasSize( 2 ).containsOnly( domain, subdomain );
    }

    @Test
    public void shouldResultInThreeModules()
    {
        // TODO: Think about this test case. What
        // should be returned for the root module ?
        // If i call mvn -pl root ... it will not work?
        Path root = baseDir.toPath();
        List<ScmFile> changeList = Arrays.asList( 
          new ScmFile( "domain/subdomain/pom.xml", ScmFileStatus.MODIFIED ),
          new ScmFile( "domain/pom.xml", ScmFileStatus.MODIFIED ),
          new ScmFile( "pom.xml", ScmFileStatus.MODIFIED ) 
        );
        moduleCalculator = new ModuleCalculator( projectList, changeList );
        List<MavenProject> changedModules = moduleCalculator.calculateChangedModules( root );

        assertThat( changedModules ).hasSize( 3 ).containsOnly( domain, subdomain, parent );
    }

    @Test
    public void shouldReturnOnlyChangedModules() {
        Path root = baseDir.toPath();
        List<ScmFile> changeList = Arrays.asList(
            new ScmFile( "domain/subdomain/subsubdomain/pom.xml", ScmFileStatus.MODIFIED ),
            new ScmFile( "domain/pom.xml", ScmFileStatus.MODIFIED )
        );
        moduleCalculator = new ModuleCalculator( projectList, changeList );
        List<MavenProject> changedModules = moduleCalculator.calculateChangedModules( root );

        assertThat( changedModules ).hasSize( 2 ).containsOnly( subsubdomain, domain );
    }

}
