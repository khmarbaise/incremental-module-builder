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

import java.io.File;
import java.util.List;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.codehaus.plexus.PlexusTestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecondTest
    extends PlexusTestCase
{
    private final Logger LOGGER = LoggerFactory.getLogger( getClass() );

    protected void setUp()
        throws Exception
    {
        super.setUp();
        super.setupContainer();
    }

    protected void tearDown()
        throws Exception
    {
        super.tearDown();
    }

    private List<ScmFile> getChangedFiles()
        throws Exception
    {
        ScmManager scmManager = (ScmManager) lookup( ScmManager.ROLE );

        assertThat( scmManager ).isNotNull();

        ScmRepository repository = null;
        try
        {
            // TODO: This can be extracted from the pom file (scm connection).
            repository = scmManager.makeScmRepository( "scm:git:ssh://git@github.com:khmarbaise/supose.git" );
        }
        catch ( ScmRepositoryException | NoSuchScmProviderException e )
        {
            e.printStackTrace();
        }

        StatusScmResult result = null;
        try
        {
            result = scmManager.status( repository, new ScmFileSet( new File( "/Users/kama/ws-git/supose" ) ) );
        }
        catch ( ScmException e )
        {
            e.printStackTrace();
        }

        List<ScmFile> changedFiles = result.getChangedFiles();
        for ( ScmFile scmFile : changedFiles )
        {
            LOGGER.info( " file:" + scmFile.getPath() + " " + scmFile.getStatus() );
        }
        return changedFiles;

    }

    public void testShouldGetChangesFiles()
        throws Exception
    {
        getChangedFiles();
    }

}
