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

import java.util.LinkedList;
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
 * @author Karl Heinz Marbaise <khmabaise@apache.org>
 */
class IncrementalModuleBuilderImpl {

    private final Logger logger = LoggerFactory.getLogger(IncrementalModuleBuilderImpl.class);

    // private final LifecycleModuleBuilder lifecycleModuleBuilder;

    IncrementalModuleBuilderImpl(List<MavenProject> selectedProjects, LifecycleModuleBuilder lifecycleModuleBuilder,
	    MavenSession session, ReactorContext reactorContext, List<TaskSegment> taskSegments) {

	// FIXME: Move the execution out of ctor ?

	ProjectDependencyGraph projectDependencyGraph = session.getProjectDependencyGraph();

	List<MavenProject> intermediateResult = new LinkedList<>();

	for (MavenProject selectedProject : selectedProjects) {
	    intermediateResult.add(selectedProject);
	    // Up or downstream ?
	    intermediateResult.addAll(projectDependencyGraph.getDownstreamProjects(selectedProject, true));
	    // TODO: Need to think about this?
	    // intermediateResult.addAll(projectDependencyGraph.getUpstreamProjects(selectedProject,
	    // true));
	}

	List<MavenProject> result = new LinkedList<>();

	for (MavenProject project : intermediateResult) {
	    if (!result.contains(project)) {
		result.add(project);
	    }
	}

	session.setProjects(result);

	logger.info("Calculated Reactor Order:");
	for (MavenProject mavenProject : session.getProjects()) {
	    logger.info(" {}", mavenProject.getName());
	}

	for (TaskSegment taskSegment : taskSegments) {
	    for (MavenProject mavenProject : session.getProjects()) {
		logger.info("Project: {}", mavenProject.getId());
		lifecycleModuleBuilder.buildProject(session, reactorContext, mavenProject, taskSegment);
	    }
	}
    }

    public void build() throws ExecutionException, InterruptedException {
	logger.info("Starting building");
    }

}
