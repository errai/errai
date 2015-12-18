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

package org.jboss.errai.forge.ui.command;

import org.apache.maven.model.BuildBase;
import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
import org.apache.maven.model.Resource;
import org.jboss.errai.forge.config.ProjectConfig;
import org.jboss.errai.forge.config.ProjectProperty;
import org.jboss.errai.forge.facet.aggregate.CoreFacet;
import org.jboss.errai.forge.facet.aggregate.ErraiCdiFacet;
import org.jboss.errai.forge.facet.dependency.JettyIntegrationTestDependencyFacet;
import org.jboss.errai.forge.facet.dependency.WeldIntegrationTestDependencyFacet;
import org.jboss.errai.forge.facet.plugin.SurefirePluginFacet;
import org.jboss.errai.forge.facet.resource.AbstractFileResourceFacet;
import org.jboss.errai.forge.facet.resource.BeansXmlFacet;
import org.jboss.errai.forge.facet.resource.ErraiAppPropertiesFacet;
import org.jboss.errai.forge.util.MavenModelUtil;
import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.maven.projects.MavenFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFacet;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact.*;

@FacetConstraint({ CoreFacet.class, ErraiCdiFacet.class })
public class CreateIntegrationTest extends CreateTestCommand {
  
  @Inject
  private UIInput<String> testClassSimpleName;
  
  @Inject
  private UIInput<String> testPackageName;
  
  @Inject
  private FacetFactory facetFactory;
  
  private final TemplateWriter testClassTemplateWriter;
  private final TemplateWriter testModuleTemplateWriter;
  private final TemplateWriter testWebXmlTemplateWriter;
  private final TemplateWriter testJettyEnvTemplateWriter;
  
  public CreateIntegrationTest() {
    testClassTemplateWriter = new TemplateWriter(
            "/org/jboss/errai/forge/ui/command/IntegratedTestClassTemplate.java",
            "$$_testClassPackage_$$",
            "$$_testClassSimpleName_$$",
            "$$_moduleLogicalName_$$");

    testModuleTemplateWriter = new TemplateWriter(
            "/org/jboss/errai/forge/ui/command/TestTemplate.gwt.xml",
            "$$_projectLogicalModuleName_$$");

    testWebXmlTemplateWriter = new TemplateWriter("/org/jboss/errai/forge/ui/command/test-web.xml");

    testJettyEnvTemplateWriter = new TemplateWriter("/org/jboss/errai/forge/ui/command/test-jetty-env.xml");
  }
  
  public CreateIntegrationTest(final ProjectFactory projectFactory, final FacetFactory facetFactory,
          final UIInput<String> testClassName, final UIInput<String> testPackageName) {
    this();
    this.projectFactory = projectFactory;
    this.facetFactory = facetFactory;
    this.testClassSimpleName = testClassName;
    this.testPackageName = testPackageName;
  }

  @Override
  public void initializeUI(final UIBuilder builder) throws Exception {
    builder.add(
            testClassSimpleName.setLabel("Test Class Simple Name")
            .setDescription("The simple name of the test class to be generated.")
            .setEnabled(true)
            .setRequired(true)
            )
            .add(
            testPackageName.setLabel("Test Class Package")
            .setDescription("The root package for the test module.")
            .setEnabled(true)
            .setRequired(true));
  }

  @Override
  public Result execute(final UIExecutionContext context) throws Exception {
    final Project project = getSelectedProject(context);

    addTestDependencies(project);
    setupTestProfile(project);
    produceTestFile(project);
    produceTestModuleFile(project);
    produceConfigurationsFiles(project);

    return Results.success();
  }

  private void addTestDependencies(final Project project) {
    addTestScopedDependency(project, JUnit);
    addTestScopedDependency(project, ErraiCdiClient);
    addTestScopedDependency(project, GwtDev);
    addTestJarDependency(project, ErraiCdiClient);
  }
  
  private void setupTestProfile(final Project project) {
    installSurefirePluginFacet(project);
    installIntegrationTestDependencies(project);
    addTestResources(project);
  }

  private void installSurefirePluginFacet(final Project project) {
    installFacet(project, SurefirePluginFacet.class);
  }

  private void installIntegrationTestDependencies(final Project project) {
    installFacet(project, WeldIntegrationTestDependencyFacet.class);
    installFacet(project,JettyIntegrationTestDependencyFacet.class);
  }

  private void installFacet(final Project project, final Class<? extends ProjectFacet> facetType) {
    if (!project.hasFacet(facetType)) {
      facetFactory.install(project, facetType);
    }
  }

  private void addTestResources(final Project project) {
    final MavenFacet mavenFacet = project.getFacet(MavenFacet.class);

    final Model pom = mavenFacet.getModel();
    final Map<String, Resource> resourcesByDirectory = new HashMap<String, Resource>();
    final Profile testProfile = MavenModelUtil.getProfileById("integration-test", pom.getProfiles());
    
    addTestResourcesFromMainBuild(pom, resourcesByDirectory);
    addTestResourcesFromTestProfile(resourcesByDirectory, testProfile);
    maybeAddTestSourceDirectory(pom, resourcesByDirectory);
    
    testProfile.getBuild().setTestResources(new ArrayList<Resource>(resourcesByDirectory.values()));
    mavenFacet.setModel(pom);
  }

  private void maybeAddTestSourceDirectory(final Model pom, final Map<String, Resource> resourcesByDirectory) {
    final Resource testSource = new Resource();
    if (pom.getBuild() != null && pom.getBuild().getTestSourceDirectory() != null) {
      testSource.setDirectory(pom.getBuild().getTestSourceDirectory());
    }
    else {
      testSource.setDirectory("src/test/java");
    }
    
    if (!resourcesByDirectory.containsKey(testSource.getDirectory())) {
      resourcesByDirectory.put(testSource.getDirectory(), testSource);
    }
  }

  private void addTestResourcesFromTestProfile(final Map<String, Resource> resourcesByDirectory, final Profile testProfile) {
    if (testProfile.getBuild() == null)
      testProfile.setBuild(new BuildBase());
    if (testProfile.getBuild().getTestResources() != null) {
      for (final Resource resource : testProfile.getBuild().getTestResources()) {
        resourcesByDirectory.put(resource.getDirectory(), resource);
      }
    }
  }

  private void addTestResourcesFromMainBuild(final Model pom, final Map<String, Resource> resourcesByDirectory) {
    if (pom.getBuild() != null && pom.getBuild().getTestResources() != null) {
      for (final Resource resource : pom.getBuild().getTestResources()) {
        resourcesByDirectory.put(resource.getDirectory(), resource);
      }
    }
    else {
      final Resource testRes = new Resource();
      testRes.setDirectory("src/test/resources");

      resourcesByDirectory.put(testRes.getDirectory(), testRes);
    }
  }

  private void produceTestFile(final Project project) throws IOException {
    final String testClassPackage = testPackageName.getValue() + ".client.local";
    final File outputFile = new File(getTestSourceDirectory(project), testClassPackage.replace('.', File.separatorChar)
            + File.separator + testClassSimpleName.getValue() + ".java");
    final String testModuleName = getTestModuleLogicalName();

    testClassTemplateWriter
      .set("$$_testClassPackage_$$", testClassPackage)
      .set("$$_testClassSimpleName_$$", testClassSimpleName.getValue())
      .set("$$_moduleLogicalName_$$", testModuleName)
      .writeTemplate(outputFile);
  }

  private void produceTestModuleFile(final Project project) throws IOException {
    final String testModuleName = getTestModuleLogicalName();
    final File testModuleFile = new File(getTestSourceDirectory(project), testModuleName.replace('.',
            File.separatorChar) + ".gwt.xml");

    final String projectModuleName = project.getFacet(ProjectConfig.class).getProjectProperty(
            ProjectProperty.MODULE_LOGICAL, String.class);
    
    testModuleTemplateWriter
      .set("$$_projectLogicalModuleName_$$", projectModuleName)
      .writeTemplate(testModuleFile);
  }

  private void produceConfigurationsFiles(final Project project) throws IOException {
    final File projectRootDirectory = project.getRootDirectory().getUnderlyingResourceObject();

    testWebXmlTemplateWriter.writeTemplate(new File(projectRootDirectory, "war/WEB-INF/web.xml"));
    testJettyEnvTemplateWriter.writeTemplate(new File(projectRootDirectory, "war/WEB-INF/jetty-env.xml"));

    copyConfigurationFile(project, BeansXmlFacet.class, new File(projectRootDirectory, "war/WEB-INF/beans.xml"));
    copyConfigurationFile(project, ErraiAppPropertiesFacet.class, new File(projectRootDirectory, "src/test/resources/ErraiApp.properties"));
  }
  
  private void copyConfigurationFile(final Project project, final Class<? extends AbstractFileResourceFacet> facetType,
          final File copiedFile) throws IOException {
    if (project.hasFacet(facetType)) {
      final AbstractFileResourceFacet facet = project.getFacet(facetType);
      final File originalFile = facet.getAbsoluteFilePath();
      
      if (!copiedFile.exists()) {
        copiedFile.getParentFile().mkdirs();
        copiedFile.createNewFile();
      }
      
      try (final FileInputStream in = new FileInputStream(originalFile);
              final FileOutputStream out = new FileOutputStream(copiedFile)) {
        final byte[] buf = new byte[256];
        int read;
        
        while ((read = in.read(buf)) > -1) {
          out.write(buf, 0, read);
        }
      }
    }
  }

  private String getTestModuleLogicalName() {
    return testPackageName.getValue() + ".Test";
  }

  @Override
  protected String getCommandName() {
    return "Add Integration Test";
  }

  @Override
  protected String getCommandDescription() {
    return "Create a GWTTestCase for integration testing.";
  }

}
