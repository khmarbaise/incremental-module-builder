package com.soebes.maven.extensions.incremental;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.project.MavenProject;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.junit.Before;
import org.junit.Test;

public class ModuleCalculatorTest {

    private File baseDir = new File ("/usr/local/project/");
    private List<MavenProject> projectList;
    
    private MavenProject parent = createProject("parent", new File(baseDir, "."));
    private MavenProject assembly = createProject("assembly", new File(baseDir, "assembly"));
    private MavenProject domain = createProject("domain", new File(baseDir, "domain"));
    private MavenProject subdomain = createProject("subdomain", new File(baseDir, "domain/subdomain"));

    @Before
    public void before() {
	projectList = new ArrayList<>();
	projectList.add(parent);
	projectList.add(assembly);
	projectList.add(domain);
	projectList.add(subdomain);
    }

    private MavenProject createProject(String artifactId, File baseDir) {
	MavenProject mock = mock(MavenProject.class);
	when(mock.getGroupId()).thenReturn("c.s.e");
	when(mock.getArtifactId()).thenReturn(artifactId);
	when(mock.getVersion()).thenReturn("0.1.0-SNAPSHOT");
	when(mock.getBasedir()).thenReturn(baseDir);
	return mock;
    }

    @Test
    public void shouldResultInASingleModule() {
	Path root = baseDir.toPath();
	List<ScmFile> changeList = Arrays.asList(
		new ScmFile("domain/src/main/java/com/test.java", ScmFileStatus.MODIFIED)
	);
	List<MavenProject> changedModules = ModuleCalculator.calculateChangedModules(root, projectList, changeList);

	assertThat(changedModules).hasSize(1).containsExactly(domain);
    }

    @Test
    public void shouldResultInTwoModules() {
	Path root = baseDir.toPath();
	List<ScmFile> changeList = Arrays.asList(
		new ScmFile("domain/src/main/java/com/test.java", ScmFileStatus.MODIFIED),
		new ScmFile("assembly/pom.xml", ScmFileStatus.MODIFIED)
	);
	List<MavenProject> changedModules = ModuleCalculator.calculateChangedModules(root, projectList, changeList);

	assertThat(changedModules).hasSize(2).containsOnly(domain, assembly);
    }

    @Test
    public void shouldResultInTwoModulesTwoChangesInSingleModule() {
	Path root = baseDir.toPath();
	List<ScmFile> changeList = Arrays.asList(
		new ScmFile("domain/src/main/java/com/test.java", ScmFileStatus.MODIFIED),
		new ScmFile("domain/src/main/java/Anton.java", ScmFileStatus.MODIFIED), 
		new ScmFile("assembly/pom.xml", ScmFileStatus.MODIFIED)
	);

	List<MavenProject> changedModules = ModuleCalculator.calculateChangedModules(root, projectList, changeList);

	assertThat(changedModules).hasSize(2).containsOnly(domain, assembly);
    }

    @Test
    public void shouldResultInTwoModulesDomainAndSubDomain() {
	Path root = baseDir.toPath();
	List<ScmFile> changeList = Arrays.asList(
		new ScmFile("domain/subdomain/pom.xml", ScmFileStatus.MODIFIED),
		new ScmFile("domain/pom.xml", ScmFileStatus.MODIFIED)
	);
	List<MavenProject> changedModules = ModuleCalculator.calculateChangedModules(root, projectList, changeList);

	assertThat(changedModules).hasSize(2).containsOnly(domain, subdomain);
    }

    @Test
    public void shouldResultInThreeModules() {
	//TODO: Think about this test case. What
	// should be returned for the root module ?
	Path root = baseDir.toPath();
	List<ScmFile> changeList = Arrays.asList(
		new ScmFile("domain/subdomain/pom.xml", ScmFileStatus.MODIFIED),
		new ScmFile("domain/pom.xml", ScmFileStatus.MODIFIED),
		new ScmFile("pom.xml", ScmFileStatus.MODIFIED)
	);
	System.out.println("shouldResultInThreeModules");
	List<MavenProject> changedModules = ModuleCalculator.calculateChangedModules(root, projectList, changeList);

	assertThat(changedModules).hasSize(3).containsOnly(domain, subdomain, parent);
    }

}
