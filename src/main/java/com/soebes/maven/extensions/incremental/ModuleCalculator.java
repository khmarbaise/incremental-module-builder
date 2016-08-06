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

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.project.MavenProject;
import org.apache.maven.scm.ScmFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Karl Heinz Marbaise <khmarbaise@apache.org>
 *
 */
public class ModuleCalculator {
    private static final Logger logger = LoggerFactory.getLogger(ModuleCalculator.class);

    //TODO: Don't use a static method? We can use a usual class? 
    /**
     * Calculate the modules which needed to be rebuilt based on the list of
     * changes from SCM.
     * 
     * @param projectRootpath
     *            Root path of the project.
     * @param projectList
     *            The list of Maven Projects which are in the reactor.
     * @param changeList
     *            The list of changes within this structure.
     * @return The list of modules which needed to be rebuilt.
     */
    public static List<MavenProject> calculateChangedModules(Path projectRootpath, List<MavenProject> projectList,
	    List<ScmFile> changeList) {
	List<MavenProject> result = new ArrayList<>();
	for (MavenProject project : projectList) {
	    Path relativize = projectRootpath.relativize(project.getBasedir().toPath());
	    for (ScmFile fileItem : changeList) {
		boolean startsWith = new File(fileItem.getPath()).toPath().startsWith(relativize);
		logger.debug("startswith: " + startsWith + " " + fileItem.getPath() + " " + relativize);
		if (startsWith) {
		    if (!result.contains(project)) {
			result.add(project);
		    }
		}
	    }
	}
	return result;
    }

}
