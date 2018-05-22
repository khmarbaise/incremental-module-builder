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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.maven.project.MavenProject;
import org.apache.maven.scm.ScmFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Karl Heinz Marbaise <khmarbaise@apache.org>
 */
public class ModuleCalculator
{
    private static final Path PATH_ROOT = Paths.get("");

    private final Logger logger = LoggerFactory.getLogger( getClass().getName() );

    private List<MavenProject> projectList;

    private List<ScmFile> changeList;

    /**
     * @param projectList The list of Maven Projects which are in the reactor.
     * @param changeList The list of changes within this structure.
     */
    public ModuleCalculator( List<MavenProject> projectList, List<ScmFile> changeList )
    {
        this.projectList = Objects.requireNonNull( projectList, "projectList is not allowed to be null." );
        this.changeList = Objects.requireNonNull( changeList, "changeList is not allowed to be null." );
    }

    /**
     * Calculate the modules which needed to be rebuilt based on the list of changes from SCM.
     * 
     * @param projectRootpath Root path of the project.
     * @return The list of modules which needed to be rebuilt.
     */
    public List<MavenProject> calculateChangedModules( Path projectRootpath )
    {
        // TODO: Think about if we got only pom packaging modules? Do we
        // need to do something special there?
        List<MavenProject> result = new ArrayList<>();

        Map<Path, MavenProject> projectsByPath = new HashMap<>();
        for ( MavenProject project : projectList )
        {
            projectsByPath.put( projectRootpath.relativize( project.getBasedir().toPath() ), project );
        }
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "projectsByPath: " + projectsByPath );
        }

        for ( ScmFile fileItem : changeList )
        {
            MavenProject foundProject = findClosestMatchingProject( fileItem, projectsByPath );
            // TODO: Fail if foundProject is null?
            if ( logger.isDebugEnabled() )
            {
                logger.debug( "foundProject for " + fileItem.getPath() + " : " + foundProject );
            }
            if ( foundProject != null && !result.contains( foundProject ) )
            {
                result.add( foundProject );
            }
        }

        return result;
    }

    private MavenProject findClosestMatchingProject( ScmFile fileItem, Map<Path, MavenProject> projectsByPath ) {
        Path path = Paths.get( fileItem.getPath() );
        do
        {
            MavenProject project = projectsByPath.get( path );
            if ( project != null )
            {
                return project;
            }
            Path parent = path.getParent();
            path = parent == null && !path.equals(PATH_ROOT) ? PATH_ROOT : parent; 
        }
        while ( path != null );

        return null;
    }
}
