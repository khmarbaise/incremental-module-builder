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

import org.apache.commons.lang.StringUtils;
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
 * Incremental Module Builder behaviour.
 * 
 * @author Karl Heinz Marbaise <khmarbaise@apache.org>
 * 
 */
@Singleton
@Named("incremental")
public class IncrementalModuleBuilder implements Builder {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private final LifecycleModuleBuilder lifecycleModuleBuilder;

    @Inject
    private ScmManager scmManager;

    @Inject
    public IncrementalModuleBuilder(LifecycleModuleBuilder lifecycleModuleBuilder) {
	LOGGER.info(" ------------------------------------");
	LOGGER.info(" Maven Incremental Module Builder");
	LOGGER.info(" Version: {}", IncrementalModuleBuilderVersion.getVersion());
	LOGGER.debug("     SHA: {}", IncrementalModuleBuilderVersion.getRevision());
	LOGGER.info(" ------------------------------------");
	this.lifecycleModuleBuilder = lifecycleModuleBuilder;
    }

    private boolean havingScmDeveloperConnection(MavenSession session) {
	if (session.getTopLevelProject().getScm() == null) {
	    LOGGER.error("The incremental module builder needs a correct scm configuration.");
	    return false;
	}

	if (StringUtils.isEmpty(session.getTopLevelProject().getScm().getDeveloperConnection())) {
	    LOGGER.error("The incremental module builder needs the scm developerConnection to work properly.");
	    return false;
	}

	return true;
    }

    @Override
    public void build(final MavenSession session, final ReactorContext reactorContext, ProjectBuildList projectBuilds,
	    final List<TaskSegment> taskSegments, ReactorBuildStatus reactorBuildStatus)
	    throws ExecutionException, InterruptedException {

	// Think about this?
	if (!session.getCurrentProject().isExecutionRoot()) {
	    LOGGER.info("Not executing in root.");
	}

	Path projectRootpath = session.getTopLevelProject().getBasedir().toPath();

	if (!havingScmDeveloperConnection(session)) {
	    LOGGER.warn("There is no scm developer connection configured.");
	    LOGGER.warn("So we can't estimate which modules have changed.");
	    return;
	}

	// TODO: Make more separation of concerns..(Extract the SCM Code from
	// here?
	ScmRepository repository = null;
	try {
	    // Assumption: top level project contains the SCM entry.
	    repository = scmManager.makeScmRepository(session.getTopLevelProject().getScm().getDeveloperConnection());
	} catch (ScmRepositoryException | NoSuchScmProviderException e) {
	    LOGGER.error("Failure during makeScmRepository", e);
	    return;
	}

	StatusScmResult result = null;
	try {
	    result = scmManager.status(repository, new ScmFileSet(session.getTopLevelProject().getBasedir()));
	} catch (ScmException e) {
	    LOGGER.error("Failure during status", e);
	    return;
	}

	List<ScmFile> changedFiles = result.getChangedFiles();
	if (changedFiles.isEmpty()) {
	    LOGGER.info(" Nothing has been changed.");
	} else {

	    for (ScmFile scmFile : changedFiles) {
		LOGGER.info(" Changed file: " + scmFile.getPath() + " " + scmFile.getStatus());
	    }

	    ModuleCalculator mc = new ModuleCalculator(session.getProjectDependencyGraph().getSortedProjects(),
		    changedFiles);
	    List<MavenProject> calculateChangedModules = mc.calculateChangedModules(projectRootpath);

	    for (MavenProject mavenProject : calculateChangedModules) {
		LOGGER.info("Changed Project: " + mavenProject.getId());
	    }

	    IncrementalModuleBuilderImpl incrementalModuleBuilderImpl = new IncrementalModuleBuilderImpl(
		    calculateChangedModules, lifecycleModuleBuilder, session, reactorContext, taskSegments);

	    // Really build only changed modules.
	    incrementalModuleBuilderImpl.build();
	}
    }

}
