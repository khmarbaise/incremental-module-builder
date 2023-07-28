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

import com.soebes.module.calculator.ModuleCalculator;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author Karl Heinz Marbaise <khmarbaise@apache.org>
 */
class ChangedModules
{
    private final Logger LOGGER = LoggerFactory.getLogger( getClass().getName() );

    private List<MavenProject> projectList;

    /**
     * @param projectList The list of Maven Projects which are in the reactor.
     */
    ChangedModules(List<MavenProject> projectList )
    {
        this.projectList = Objects.requireNonNull( projectList, "projectList is not allowed to be null." );
    }

    /**
     * Calculate the modules which needed to be rebuilt based on the list of changes from SCM.
     * 
     * @param projectRootpath Root path of the project.
     * @return The list of modules which needed to be rebuilt.
     */
    List<MavenProject> findChangedModules(Path projectRootpath )
    {
        // TODO: Think about if we got only pom packaging modules? Do we
        // need to do something special there?
        LOGGER.debug("findChangedModules: {}", projectRootpath);
        projectList.forEach(s -> LOGGER.debug(" -> {}", s.getArtifactId()));
        List<MavenProject> result = new LinkedList<>();
        for ( MavenProject project : projectList )
        {
            ModuleCalculator moduleCalculator = new ModuleCalculator();
            Path relativize = projectRootpath.relativize(project.getBasedir().toPath());
            Path moduleHash = relativize.resolve(Paths.get("target/module.hash"));
            LOGGER.info("Project: {} ModuleHash: {}", project, moduleHash);
            boolean hashHasChanged = moduleCalculator.hashChanged(relativize, moduleHash, Arrays.asList(".git", ".github", "target", ".idea"));
            if (hashHasChanged) {
                LOGGER.info(" -> Changed {}", project);
                result.add(project);
            }
        }
        return result;
    }

}
