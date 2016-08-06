package com.soebes.maven.extensions.incremental;

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

public class SecondTest extends PlexusTestCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecondTest.class);

    protected void setUp() throws Exception {
	super.setUp();
	super.setupContainer();
    }

    protected void tearDown() throws Exception {
	super.tearDown();
    }

    private List<ScmFile> getChangedFiles() throws Exception {
	ScmManager scmManager = (ScmManager) lookup(ScmManager.ROLE);

	assertThat(scmManager).isNotNull();

	ScmRepository repository = null;
	try {
	    // TODO: This can be extracted from the pom file (scm connection).
	    repository = scmManager.makeScmRepository("scm:git:ssh://git@github.com:khmarbaise/supose.git");
	} catch (ScmRepositoryException | NoSuchScmProviderException e) {
	    e.printStackTrace();
	}

	StatusScmResult result = null;
	try {
	    result = scmManager.status(repository, new ScmFileSet(new File("/Users/kama/ws-git/supose")));
	} catch (ScmException e) {
	    e.printStackTrace();
	}

	List<ScmFile> changedFiles = result.getChangedFiles();
	for (ScmFile scmFile : changedFiles) {
	    LOGGER.info(" file:" + scmFile.getPath() + " " + scmFile.getStatus());
	}
	return changedFiles;

    }

    public void testRelativze() {
	File s1 = new File("domain/pom.xml");
	File base = new File("domain");
	LOGGER.info("1)startsWith:" + s1.toPath().startsWith(base.toPath()));
    }

    public void testRelativze2() {
	File s1 = new File("domain/src/main/java/com/Test.java");
	File base = new File("domain");
	LOGGER.info("2)startsWith:" + s1.toPath().startsWith(base.toPath()));
    }
    
    public void testXX() throws Exception {
	getChangedFiles();
    }

}
