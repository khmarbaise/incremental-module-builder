package com.soebes.maven.extensions.incremental;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Trivial Maven {@link Builder} implementation. All interesting stuff happens in {@link IncrementalBuilderImpl} .
 */
@Singleton
@Named( "incremental" )
public class IncrementalBuilder
    implements Builder
{
    private final Logger logger = LoggerFactory.getLogger( IncrementalBuilder.class );

    private final LifecycleModuleBuilder lifecycleModuleBuilder;

    @Inject
    public IncrementalBuilder( LifecycleModuleBuilder lifecycleModuleBuilder )
    {
        logger.info( " Incremental Builder started." );
        this.lifecycleModuleBuilder = lifecycleModuleBuilder;
    }

    @Override
    public void build( final MavenSession session, final ReactorContext reactorContext, ProjectBuildList projectBuilds,
                       final List<TaskSegment> taskSegments, ReactorBuildStatus reactorBuildStatus )
        throws ExecutionException, InterruptedException
    {
        // reactorBuildStatus.blackList( project );

        for ( TaskSegment taskSegment : taskSegments )
        {
            logger.info( "TaskSegment: {}", taskSegment.getTasks() );
        }
        new IncrementalBuilderImpl( lifecycleModuleBuilder, session, reactorContext, taskSegments ).build();
    }

}
