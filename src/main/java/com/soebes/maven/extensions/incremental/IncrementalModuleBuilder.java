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

import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.internal.LifecycleModuleBuilder;
import org.apache.maven.lifecycle.internal.ProjectBuildList;
import org.apache.maven.lifecycle.internal.ReactorBuildStatus;
import org.apache.maven.lifecycle.internal.ReactorContext;
import org.apache.maven.lifecycle.internal.TaskSegment;
import org.apache.maven.lifecycle.internal.builder.Builder;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Incremental Module Builder behaviour.
 * 
 * @author Karl Heinz Marbaise <khmarbaise@apache.org>
 */
@Singleton
@Named( "incremental" )
public class IncrementalModuleBuilder
    implements Builder
{
    private final Logger LOGGER = LoggerFactory.getLogger( getClass() );

    private final LifecycleModuleBuilder lifecycleModuleBuilder;

    @Inject
    public IncrementalModuleBuilder( LifecycleModuleBuilder lifecycleModuleBuilder )
    {
        LOGGER.info( " ------------------------------------" );
        LOGGER.info( " Maven Incremental Module Builder" );
        LOGGER.info( " Version: {}", IncrementalModuleBuilderVersion.getVersion() );
        LOGGER.debug( "     SHA: {}", IncrementalModuleBuilderVersion.getRevision() );
        LOGGER.info( " ------------------------------------" );
        this.lifecycleModuleBuilder = lifecycleModuleBuilder;
    }

    @Override
    public void build( final MavenSession session, final ReactorContext reactorContext, ProjectBuildList projectBuilds,
                       final List<TaskSegment> taskSegments, ReactorBuildStatus reactorBuildStatus )
        throws ExecutionException, InterruptedException
    {

        // Think about this?
        if ( !session.getCurrentProject().isExecutionRoot() )
        {
            LOGGER.info( "Not executing in root." );
        }

        Path projectRootpath = session.getTopLevelProject().getBasedir().toPath();

//        if ( changedFiles.isEmpty() )
//        {
//            LOGGER.info( " Nothing has been changed." );
//        }
//        else

        ChangedModules mc =
            new ChangedModules( session.getProjectDependencyGraph().getSortedProjects() );
        List<MavenProject> changedModules = mc.findChangedModules( projectRootpath );

        for ( MavenProject mavenProject : changedModules )
        {
            LOGGER.info( "Changed Project: " + mavenProject.getId() );
        }

        IncrementalModuleBuilderImpl incrementalModuleBuilderImpl =
            new IncrementalModuleBuilderImpl( changedModules, lifecycleModuleBuilder, session,
                                              reactorContext, taskSegments );

        // Really build only changed modules.
        incrementalModuleBuilderImpl.build();
    }

}
