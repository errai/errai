package org.jboss.errai.forge.facet.ui.command;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
import org.apache.maven.model.Resource;
import org.jboss.errai.forge.facet.aggregate.ErraiCdiFacet;
import org.jboss.errai.forge.facet.aggregate.ErraiMessagingFacet;
import org.jboss.errai.forge.facet.resource.BeansXmlFacet;
import org.jboss.errai.forge.facet.resource.ErraiAppPropertiesFacet;
import org.jboss.errai.forge.facet.resource.WebXmlFacet;
import org.jboss.errai.forge.facet.ui.command.res.UIExecutionContextMock;
import org.jboss.errai.forge.facet.ui.command.res.UIInputMock;
import org.jboss.errai.forge.test.base.ForgeTest;
import org.jboss.errai.forge.ui.command.CreateIntegrationTest;
import org.jboss.forge.addon.dependencies.Dependency;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.maven.projects.MavenFacet;
import org.jboss.forge.addon.maven.projects.facets.MavenDependencyFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIContextProvider;
import org.junit.Before;
import org.junit.Test;

public class IntegrationTestCommandTest extends ForgeTest {
  
  private Project project;
  
  private UIInputMock<String> testClassSimpleName;
  private UIInputMock<String> testPackageName;

  private UIExecutionContextMock context;

  private CreateIntegrationTest testableInstance;
  
  @Before
  public void setup() {
    final String testName = "IntegrationTestClass";
    final String testPackage = "org.jboss.errai.forge.test";

    project = createErraiTestProject();
    
    context = new UIExecutionContextMock();

    testClassSimpleName = new UIInputMock<String>();
    testClassSimpleName.setValue(testName);
    
    testPackageName = new UIInputMock<String>();
    testPackageName.setValue(testPackage);

    testableInstance = new CreateIntegrationTest(null, facetFactory, testClassSimpleName, testPackageName) {
      @Override
      protected Project getSelectedProject(UIContext context) {
        return project;
      }
      @Override
      protected Project getSelectedProject(UIContextProvider contextProvider) {
        return project;
      }
    };
  }
  
  @Test
  public void checkGeneratedTestClass() throws Exception {
    testableInstance.execute(context);
    
    final File testFile = new File(project.getRootDirectory().getUnderlyingResourceObject(),
            "src/test/java/org/jboss/errai/forge/test/client/local/IntegrationTestClass.java");
    
    assertResourceAndFileContentsSame("org/jboss/errai/forge/test/IntegrationTestClass.java", testFile);
  }
  
  @Test
  public void checkTestModuleIsAdded() throws Exception {
    testableInstance.execute(context);
    
    final File moduleFile = new File(project.getRootDirectory().getUnderlyingResourceObject(),
            "src/test/java/org/jboss/errai/forge/test/Test.gwt.xml");
    
    assertResourceAndFileContentsSame("org/jboss/errai/forge/test/TestModule.gwt.xml", moduleFile);
  }
  
  @Test
  public void checkDependenciesAreAdded() throws Exception {
    final MavenDependencyFacet depFacet = project.getFacet(MavenDependencyFacet.class);
    
    final DependencyBuilder junitDependency = DependencyBuilder.create("junit:junit");
    final DependencyBuilder erraiCdiDependency = DependencyBuilder.create("org.jboss.errai:errai-cdi-client");
    final DependencyBuilder gwtDevDependency = DependencyBuilder.create("com.google.gwt:gwt-dev");

    assertFalse(depFacet.hasDirectDependency(junitDependency));
    assertFalse(depFacet.hasDirectDependency(erraiCdiDependency));
    assertFalse(depFacet.hasDirectDependency(gwtDevDependency));
    
    testableInstance.execute(context);
    
    assertTrue(depFacet.hasDirectDependency(junitDependency));
    assertTrue(depFacet.hasDirectDependency(erraiCdiDependency));
    assertTrue(depFacet.hasDirectDependency(gwtDevDependency));
  }
  
  @Test
  public void checkDependenciesAreOnlyAddedOnce() throws Exception {
    checkDependenciesAreAdded();
    
    final MavenDependencyFacet depFacet = project.getFacet(MavenDependencyFacet.class);
    final List<Dependency> dependencies = depFacet.getDependencies();
    
    testableInstance.execute(context);
    
    assertEquals(dependencies.size(), depFacet.getDependencies().size());
  }
  
  @Test
  public void addIntegrationTestProfileAndSurfireConfiguration() throws Exception {
    final MavenFacet mavenFacet = project.getFacet(MavenFacet.class);
    Model pom = mavenFacet.getModel();

    assertEquals(0, pom.getProfiles().size());

    testableInstance.execute(context);
    pom = mavenFacet.getModel();

    assertEquals(1, pom.getProfiles().size());
    final Profile profile = pom.getProfiles().get(0);
    
    assertEquals("integration-test", profile.getId());
    assertNotNull(profile.getBuild());
    assertEquals(1, profile.getBuild().getPlugins().size());
    assertEquals("maven-surefire-plugin", profile.getBuild().getPlugins().get(0).getArtifactId());
  }
  
  @Test
  public void addTestSourceDirectoryAsResourceInTestPorfile() throws Exception {
    testableInstance.execute(context);
    
    final MavenFacet mavenFacet = project.getFacet(MavenFacet.class);
    final Model pom = mavenFacet.getModel();

    assertNotNull(pom.getProfiles());
    assertEquals(1, pom.getProfiles().size());

    final Profile testProfile = pom.getProfiles().get(0);
    assertEquals("integration-test", testProfile.getId());
    assertNotNull(testProfile.getBuild().getTestResources());
    assertEquals(2, testProfile.getBuild().getTestResources().size());
    
    for (final Resource testResource : testProfile.getBuild().getTestResources()) {
      if (!testResource.getDirectory().equals("src/test/resources")
              && !testResource.getDirectory().equals("src/test/java")) {
        fail("Unexpected resource directory: " + testResource.getDirectory());
      }
    }
  }
  
  @Test
  public void checkIntegrationTestProfileAndSurefireConfigurationOnlyRunOnce() throws Exception {
    addIntegrationTestProfileAndSurfireConfiguration();
    
    testableInstance.execute(context);
    
    final MavenFacet mavenFacet = project.getFacet(MavenFacet.class);
    final Model pom = mavenFacet.getModel();
    
    assertEquals(1, pom.getProfiles().size());
    assertEquals(1, pom.getProfiles().get(0).getBuild().getPlugins().size());
  }

  @Test
  public void copyErraiAppPropertiesFromSrcMainResources() throws Exception {
    facetFactory.install(project, ErraiMessagingFacet.class);
    final ErraiAppPropertiesFacet appPropertiesFacet = project.getFacet(ErraiAppPropertiesFacet.class);
    final File originalAppProperties = appPropertiesFacet.getAbsoluteFilePath();
    
    testableInstance.execute(context);
    
    final File copiedAppProperties = new File(project.getRootDirectory().getUnderlyingResourceObject(),
            "src/test/resources/ErraiApp.properties");
    
    assertTrue(copiedAppProperties.exists());
    
    try (final InputStreamReader originalStream = new InputStreamReader(new FileInputStream(originalAppProperties));
            final InputStreamReader copiedStream = new InputStreamReader(new FileInputStream(copiedAppProperties))) {
      assertFileContentsSame(originalStream, copiedStream);
    }
  }
  
  @Test
  public void copyWebXmlFromWebInf() throws Exception {
    facetFactory.install(project, ErraiMessagingFacet.class);
    final WebXmlFacet webXmlFacet = project.getFacet(WebXmlFacet.class);
    final File originalWebXml = webXmlFacet.getAbsoluteFilePath();
    
    testableInstance.execute(context);
    
    final File copiedWebXml = new File(project.getRootDirectory().getUnderlyingResourceObject(), "war/WEB-INF/web.xml");
    
    assertTrue(copiedWebXml.exists());
    
    try (final InputStreamReader originalStream = new InputStreamReader(new FileInputStream(originalWebXml));
            final InputStreamReader copiedStream = new InputStreamReader(new FileInputStream(copiedWebXml))) {
      assertFileContentsSame(originalStream, copiedStream);
    }
  }

  @Test
  public void copyBeansXmlFromWebInf() throws Exception {
    facetFactory.install(project, ErraiCdiFacet.class);
    final BeansXmlFacet beansXmlFacet = project.getFacet(BeansXmlFacet.class);
    final File originalBeansXml = beansXmlFacet.getAbsoluteFilePath();
    
    testableInstance.execute(context);
    
    final File copiedBeansXml = new File(project.getRootDirectory().getUnderlyingResourceObject(), "war/WEB-INF/beans.xml");
    
    assertTrue(copiedBeansXml.exists());
    
    try (final InputStreamReader originalStream = new InputStreamReader(new FileInputStream(originalBeansXml));
            final InputStreamReader copiedStream = new InputStreamReader(new FileInputStream(copiedBeansXml))) {
      assertFileContentsSame(originalStream, copiedStream);
    }
  }
}
