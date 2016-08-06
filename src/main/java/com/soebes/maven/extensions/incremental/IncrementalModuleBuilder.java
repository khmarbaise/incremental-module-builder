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
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.internal.LifecycleModuleBuilder;
import org.apache.maven.lifecycle.internal.ProjectBuildList;
import org.apache.maven.lifecycle.internal.ReactorBuildStatus;
import org.apache.maven.lifecycle.internal.ReactorContext;
import org.apache.maven.lifecycle.internal.TaskSegment;
import org.apache.maven.lifecycle.internal.builder.Builder;
import org.apache.maven.project.MavenProject;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Incremental Builder behaviour.
 * 
 * @author Karl Heinz Marbaise <khmarbaise@apache.org>
 * 
 */
@Singleton
@Named("incremental")
public class IncrementalModuleBuilder implements Builder {
    private static final Logger LOGGER = LoggerFactory.getLogger(IncrementalModuleBuilder.class);

    private final LifecycleModuleBuilder lifecycleModuleBuilder;

    @Inject
    private ScmManager scmManager;

    @Inject
    public IncrementalModuleBuilder(LifecycleModuleBuilder lifecycleModuleBuilder) {
	LOGGER.info(" ------------------------------------");
	LOGGER.info(" Maven Incremental Module Builder");
	LOGGER.info(" ------------------------------------");
	this.lifecycleModuleBuilder = lifecycleModuleBuilder;
    }

    @Override
    public void build(final MavenSession session, final ReactorContext reactorContext, ProjectBuildList projectBuilds,
	    final List<TaskSegment> taskSegments, ReactorBuildStatus reactorBuildStatus)
	    throws ExecutionException, InterruptedException {

	if (!session.getCurrentProject().isExecutionRoot()) {
	    LOGGER.info("Not executing in root.");
	}

	Path projectRootpath = session.getTopLevelProject().getBasedir().toPath();

	//TODO: Make more separation of concerns..(Extract the SCM Code from here? 
	ScmRepository repository = null;
	try {
	    // Assumption: top level project contains the SCM entry.
	    // More checks missing..
	    // TODO: check for null in chaining
	    repository = scmManager.makeScmRepository(session.getTopLevelProject().getScm().getDeveloperConnection());
	} catch (ScmRepositoryException | NoSuchScmProviderException e) {
	    // Better error handling?
	    e.printStackTrace();
	    return;
	}

	StatusScmResult result = null;
	try {
	    result = scmManager.status(repository, new ScmFileSet(session.getTopLevelProject().getBasedir()));
	} catch (ScmException e) {
	    // Better error handling?
	    e.printStackTrace();
	}

	List<ScmFile> changedFiles = result.getChangedFiles();
	if (changedFiles.isEmpty()) {
	    LOGGER.info(" Nothing has been changed.");
	} else {

	    for (ScmFile scmFile : changedFiles) {
		LOGGER.info(" scmFile: " + scmFile.getPath() + " " + scmFile.getStatus());
	    }

	    List<MavenProject> sortedProjects = session.getProjectDependencyGraph().getSortedProjects();
	    List<MavenProject> calculateChangedModules = ModuleCalculator.calculateChangedModules(projectRootpath,
		    sortedProjects, changedFiles);
	    
	    //TODO: Think about if we got only pom packaging modules? Do we need to do something special there?
	    for (MavenProject mavenProject : calculateChangedModules) {
		LOGGER.info("Changed Project: " + mavenProject.getId());
	    }
	    // Change the reactor.
	    new IncrementalModuleBuilderImpl(calculateChangedModules, lifecycleModuleBuilder, session, reactorContext,
		    taskSegments).build();
	}
    }

}
