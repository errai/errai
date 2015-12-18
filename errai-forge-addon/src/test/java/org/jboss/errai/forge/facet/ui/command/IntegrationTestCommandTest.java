/*
 * Copyright (C) 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.forge.facet.ui.command;

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
import org.jboss.errai.forge.util.MavenModelUtil;
import org.jboss.forge.addon.dependencies.Dependency;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.maven.projects.MavenFacet;
import org.jboss.forge.addon.maven.projects.facets.MavenDependencyFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIContextProvider;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;

import static org.junit.Assert.*;

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
  public void checkTestScopedDependenciesAreAdded() throws Exception {
    final MavenDependencyFacet depFacet = project.getFacet(MavenDependencyFacet.class);
    
    final DependencyBuilder junitDependency = DependencyBuilder.create("junit:junit");
    final DependencyBuilder erraiCdiDependency = DependencyBuilder.create("org.jboss.errai:errai-cdi-client");
    final DependencyBuilder gwtDevDependency = DependencyBuilder.create("com.google.gwt:gwt-dev");
    final DependencyBuilder erraiCdiTestJarDependency = DependencyBuilder.create("org.jboss.errai:errai-cdi-client:::test-jar");

    assertFalse(depFacet.hasDirectDependency(junitDependency));
    assertFalse(depFacet.hasDirectDependency(erraiCdiDependency));
    assertFalse(depFacet.hasDirectDependency(gwtDevDependency));
    assertFalse(depFacet.hasDirectDependency(erraiCdiTestJarDependency));

    testableInstance.execute(context);
    
    assertTrue(depFacet.hasDirectDependency(junitDependency));
    assertTrue(depFacet.hasDirectDependency(erraiCdiDependency));
    assertTrue(depFacet.hasDirectDependency(gwtDevDependency));
    assertTrue(depFacet.hasDirectDependency(erraiCdiTestJarDependency));
  }
  
  @Test
  public void checkTestScopedDependenciesAreOnlyAddedOnce() throws Exception {
    checkTestScopedDependenciesAreAdded();
    
    final MavenDependencyFacet depFacet = project.getFacet(MavenDependencyFacet.class);
    final List<Dependency> dependencies = depFacet.getDependencies();
    
    testableInstance.execute(context);
    
    assertEquals(dependencies.size(), depFacet.getDependencies().size());
  }
  
  @Test
  public void addIntegrationTestProfile() throws Exception {
    final MavenFacet mavenFacet = project.getFacet(MavenFacet.class);
    Model pom = mavenFacet.getModel();

    assertEquals(0, pom.getProfiles().size());

    testableInstance.execute(context);
    pom = mavenFacet.getModel();

    final Profile testProfile = MavenModelUtil.getProfileById("integration-test", pom.getProfiles());
    
    assertNotNull("No integration-test profile was found.", testProfile);
    assertNotNull(testProfile.getBuild());

    assertEquals(1, testProfile.getBuild().getPlugins().size());
    assertEquals("maven-surefire-plugin", testProfile.getBuild().getPlugins().get(0).getArtifactId());

    final List<org.apache.maven.model.Dependency> dependencies = testProfile.getDependencies();

    assertEquals(5, dependencies.size());
    hasDependency(dependencies, "org.jboss.weld.se:weld-se-core");
    hasDependency(dependencies, "org.jboss.weld.servlet:weld-servlet-core");
    hasDependency(dependencies, "org.mortbay.jetty:jetty");
    hasDependency(dependencies, "org.mortbay.jetty:jetty-plus");
    hasDependency(dependencies, "org.mortbay.jetty:jetty-naming");
  }

  @Test
  public void addTestSourceDirectoryAsResourceInTestPorfile() throws Exception {
    testableInstance.execute(context);
    
    final MavenFacet mavenFacet = project.getFacet(MavenFacet.class);
    final Model pom = mavenFacet.getModel();

    assertNotNull(pom.getProfiles());

    final Profile testProfile = MavenModelUtil.getProfileById("integration-test", pom.getProfiles());

    assertNotNull("No integration-test profile was found.", testProfile);
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
    addIntegrationTestProfile();
    
    testableInstance.execute(context);
    
    final MavenFacet mavenFacet = project.getFacet(MavenFacet.class);
    final Model pom = mavenFacet.getModel();
    
    final Profile testProfile = MavenModelUtil.getProfileById("integration-test", pom.getProfiles());
    assertNotNull("No integration-test profile was found.", testProfile);
    assertEquals(1, testProfile.getBuild().getPlugins().size());
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
  public void writeWebXml() throws Exception {
    facetFactory.install(project, ErraiMessagingFacet.class);
    final WebXmlFacet webXmlFacet = project.getFacet(WebXmlFacet.class);
    
    testableInstance.execute(context);
    
    final File copiedWebXml = new File(project.getRootDirectory().getUnderlyingResourceObject(), "war/WEB-INF/web.xml");
    
    assertTrue(copiedWebXml.exists());
    
    try (final InputStreamReader originalStream = new InputStreamReader(
            ClassLoader.getSystemResourceAsStream("org/jboss/errai/forge/test/web.xml"));
            final InputStreamReader copiedStream = new InputStreamReader(new FileInputStream(copiedWebXml))) {
      assertFileContentsSame(originalStream, copiedStream);
    }
  }

  @Test
  public void writeJettyEnvXml() throws Exception {
    testableInstance.execute(context);

    final File copiedJettyEnvXml = new File(project.getRootDirectory().getUnderlyingResourceObject(),
            "war/WEB-INF/jetty-env.xml");

    assertTrue(copiedJettyEnvXml.exists());

    try (final InputStreamReader originalStream = new InputStreamReader(
            ClassLoader.getSystemResourceAsStream("org/jboss/errai/forge/test/jetty-env.xml"));
            final InputStreamReader copiedStream = new InputStreamReader(new FileInputStream(copiedJettyEnvXml))) {
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

  private void hasDependency(final List<org.apache.maven.model.Dependency> dependencies, final String coordinate) {
    for (final org.apache.maven.model.Dependency dep : dependencies) {
      if ((dep.getGroupId() + ":" + dep.getArtifactId()).equals(coordinate)) {
        return;
      }
    }

    fail(coordinate + " was not found in the given dependencies.");
  }
}
