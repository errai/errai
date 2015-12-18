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

package org.jboss.errai.forge.test.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.errai.forge.config.ProjectConfig;
import org.jboss.errai.forge.config.ProjectProperty;
import org.jboss.errai.forge.config.SerializableSet;
import org.jboss.errai.forge.facet.aggregate.AggregatorFacetTest;
import org.jboss.errai.forge.facet.plugin.BasePluginFacetTest;
import org.jboss.errai.forge.facet.ui.command.res.ProjectFactoryMock;
import org.jboss.errai.forge.facet.ui.command.res.SimpleTestableClass;
import org.jboss.errai.forge.facet.ui.command.res.UIExecutionContextMock;
import org.jboss.errai.forge.facet.ui.command.res.UIInputMock;
import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public abstract class ForgeTest {

  public static final String DEPENDENCY = "org.jboss.errai.forge:errai-forge-addon";
  public static final String ADDON_GROUP = "org.jboss.forge.addon";
  // TODO Programmatically lookup the Errai version this test is running in.
  public static final String ERRAI_TEST_VERSION = "4.0.0-SNAPSHOT";

  @Inject
  protected ProjectFactory projectFactory;

  @Inject
  protected FacetFactory facetFactory;

  @Deployment
  @Dependencies({
      @AddonDependency(name = DEPENDENCY),
      @AddonDependency(name = ADDON_GROUP + ":projects"),
      @AddonDependency(name = ADDON_GROUP + ":parser-java"),
      @AddonDependency(name = ADDON_GROUP + ":facets"),
      @AddonDependency(name = ADDON_GROUP + ":maven"),
      @AddonDependency(name = ADDON_GROUP + ":ui")
  })
  public static ForgeArchive getDeployment() {
    final ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
            .addBeansXML()
            .addClasses(
                    ForgeTest.class,
                    BasePluginFacetTest.class,
                    AggregatorFacetTest.class,
                    ProjectFactoryMock.class,
                    SimpleTestableClass.class,
                    UIExecutionContextMock.class,
                    UIInputMock.class
                    )
            .addAsResource("org/jboss/errai/forge/test/SimpleTestClass.java",
                    "org/jboss/errai/forge/test/SimpleTestClass.java")
            .addAsAddonDependencies(
                    AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi"),
                    AddonDependencyEntry.create(DEPENDENCY),
                    AddonDependencyEntry.create(ADDON_GROUP + ":projects"),
                    AddonDependencyEntry.create(ADDON_GROUP + ":parser-java"),
                    AddonDependencyEntry.create(ADDON_GROUP + ":facets"),
                    AddonDependencyEntry.create(ADDON_GROUP + ":maven"),
                    AddonDependencyEntry.create(ADDON_GROUP + ":ui")
            );

    return archive;
  }

  protected Project initializeJavaProject() {
    final Project project = projectFactory.createTempProject();

    return project;
  }

  protected void assertResourceAndFileContentsSame(final String resourcePath, final File file)
          throws IOException {
    assertTrue(file.getAbsolutePath() + " was not created.", file.exists());
    try (final InputStreamReader expectedReader = new InputStreamReader(
            ClassLoader.getSystemResourceAsStream(resourcePath));
            final InputStreamReader observedReader = new InputStreamReader(new FileInputStream(file))) {
      assertFileContentsSame(expectedReader, observedReader);
    }
  }

  protected void assertFileContentsSame(final InputStreamReader expectedReader, final InputStreamReader observedReader) throws IOException {
    final StringBuilder[] builders = new StringBuilder[] { new StringBuilder(), new StringBuilder()};
    final InputStreamReader[] inputReaders = new InputStreamReader[] {expectedReader, observedReader};
    for (int i = 0; i < inputReaders.length; i++) {
      final InputStreamReader reader = inputReaders[i];
      final char[] chars = new char[256];
      int read;
      while ((read = reader.read(chars)) > -1) {
        builders[i].append(chars, 0, read);
      }
    }

    try {
      assertEquals(builders[0].toString(), builders[1].toString());
    }
    catch (final AssertionError e) {
      System.out.println("EXPECTED");
      System.out.println(builders[0].toString());

      System.out.println("OBSERVED");
      System.out.println(builders[1].toString());

      throw e;
    }
  }

  protected Project createErraiTestProject() {
    final Project project = initializeJavaProject();
    final ProjectConfig projectConfig = facetFactory.install(project, ProjectConfig.class);

    projectConfig.setProjectProperty(ProjectProperty.ERRAI_VERSION, ERRAI_TEST_VERSION);
    projectConfig.setProjectProperty(ProjectProperty.MODULE_LOGICAL, "org.jboss.errai.ForgeTest");
    projectConfig.setProjectProperty(ProjectProperty.MODULE_FILE, new File(project.getRootDirectory()
            .getUnderlyingResourceObject(), "src/main/java/org/jboss/errai/ForgeTest.gwt.xml"));
    projectConfig.setProjectProperty(ProjectProperty.MODULE_NAME, "test");
    projectConfig.setProjectProperty(ProjectProperty.INSTALLED_FEATURES, new SerializableSet());

    return project;
  }

}
