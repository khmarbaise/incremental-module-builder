package com.soebes.maven.extensions.incremental;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.execution.ProjectDependencyGraph;
import org.apache.maven.lifecycle.internal.LifecycleModuleBuilder;
import org.apache.maven.lifecycle.internal.ReactorContext;
import org.apache.maven.lifecycle.internal.TaskSegment;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
class IncrementalBuilderImpl
{

    private final Logger logger = LoggerFactory.getLogger( IncrementalBuilderImpl.class );

    // private final LifecycleModuleBuilder lifecycleModuleBuilder;

    IncrementalBuilderImpl( LifecycleModuleBuilder lifecycleModuleBuilder, MavenSession session,
                            ReactorContext reactorContext, List<TaskSegment> taskSegments )
    {

        ProjectDependencyGraph projectDependencyGraph = session.getProjectDependencyGraph();

        MavenProject p = new MavenProject();
        p.setGroupId( "com.soebes.examples.j2ee" );
        p.setArtifactId( "webgui" );
        p.setVersion( "1.0.8-SNAPSHOT" );

        List<MavenProject> downstreamProjects = projectDependencyGraph.getDownstreamProjects( p, true );

        session.setProjects( downstreamProjects );

        logger.info( " New reactor content:" );
        for ( MavenProject mavenProject : session.getProjects() )
        {
            logger.info( " building: {} {}", mavenProject.getId(), mavenProject.getBasedir() );
        }

        for ( TaskSegment taskSegment : taskSegments )
        {
            logger.info( " TaskSegment: {}", taskSegment.getTasks() );
            for ( MavenProject mavenProject : session.getProjects() )
            {
                logger.info( "Project: {}", mavenProject.getId() );
                lifecycleModuleBuilder.buildProject( session, reactorContext, mavenProject, taskSegment );
            }
        }
    }

    public void build()
        throws ExecutionException, InterruptedException
    {
        logger.info( "Starting building" );
    }

}
