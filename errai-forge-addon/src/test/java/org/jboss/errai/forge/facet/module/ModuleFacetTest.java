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

package org.jboss.errai.forge.facet.module;

import org.apache.maven.model.Model;
import org.jboss.errai.forge.config.ProjectConfig;
import org.jboss.errai.forge.config.ProjectProperty;
import org.jboss.errai.forge.constant.ModuleVault;
import org.jboss.errai.forge.constant.ModuleVault.Module;
import org.jboss.errai.forge.test.base.ForgeTest;
import org.jboss.forge.addon.maven.projects.MavenFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFacet;
import org.junit.Test;

import javax.enterprise.context.Dependent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class ModuleFacetTest extends ForgeTest {

  @Dependent
  public static class SimpleModuleFacet extends AbstractModuleFacet {
    public SimpleModuleFacet() {
      modules = Arrays.asList(new ModuleVault.Module[] { Module.GwtUser, Module.ErraiCommon });
    }
  }

  @Test
  public void testEmptyModuleInstall() throws Exception {
    final Project project = initializeJavaProject();
    final File moduleFile = makeBlankModuleFile(project, ModuleCoreFacet.emptyModuleContents);
    final ProjectConfig config = facetFactory.install(project, ProjectConfig.class);
    config.setProjectProperty(ProjectProperty.MODULE_FILE, moduleFile);

    facetFactory.install(project, SimpleModuleFacet.class);

    final String moduleContent = getFileContentAsString(moduleFile);
    assertTrue(moduleContent, moduleContent.contains("<inherits name=\"org.jboss.errai.common.ErraiCommon\"/>"));
    assertTrue(moduleContent, moduleContent.contains("<inherits name=\"com.google.gwt.user.User\"/>"));
    assertEquals(2, countMatches("<inherits name=\"[^\"]*\"/>", moduleContent));
  }

  @Test
  public void testNonEmptyModuleInstall() throws Exception {
    final Project project = initializeJavaProject();
    final String body = ModuleCoreFacet.emptyModuleContents.replace("</module>",
            "<inherits name=\"org.jboss.errai.common.Logging\"/></module>");
    final File moduleFile = makeBlankModuleFile(project, body);
    final ProjectConfig config = facetFactory.install(project, ProjectConfig.class);
    config.setProjectProperty(ProjectProperty.MODULE_FILE, moduleFile);

    facetFactory.install(project, SimpleModuleFacet.class);

    final String moduleContent = getFileContentAsString(moduleFile);
    assertTrue(moduleContent, moduleContent.contains("<inherits name=\"org.jboss.errai.common.Logging\"/>"));
    assertTrue(moduleContent, moduleContent.contains("<inherits name=\"org.jboss.errai.common.ErraiCommon\"/>"));
    assertTrue(moduleContent, moduleContent.contains("<inherits name=\"com.google.gwt.user.User\"/>"));
    assertEquals(3, countMatches("<inherits name=\"[^\"]*\"/>", moduleContent));
  }

  @Test
  public void testModuleCoreFacetWithModule() throws Exception {
    final Project project = initializeJavaProject();
    final File moduleFile = makeBlankModuleFile(project, ModuleCoreFacet.emptyModuleContents);
    final ProjectConfig config = facetFactory.install(project, ProjectConfig.class);
    config.setProjectProperty(ProjectProperty.MODULE_FILE, moduleFile);

    // Forge bug: this does not actually invoke ModuleCoreFacet#install when all tests are run
//    facetFactory.install(project, ModuleCoreFacet.class);
    final ModuleCoreFacet facet = facetFactory.create(project, ModuleCoreFacet.class);
    facet.install();

    final String moduleContent = getFileContentAsString(moduleFile);
    assertTrue(moduleContent, moduleContent.contains("<inherits name=\"com.google.gwt.user.User\"/>"));
    assertEquals(1, countMatches("<inherits name=\"[^\"]*\"/>", moduleContent));
  }

  @Test
  public void testModuleCoreFacetWithoutModule() throws Exception {
    final Project project = initializeJavaProject();
    final File moduleFile = makeBlankModuleFile(project, ModuleCoreFacet.emptyModuleContents);
    moduleFile.delete();
    final ProjectConfig config = facetFactory.install(project, ProjectConfig.class);
    config.setProjectProperty(ProjectProperty.MODULE_FILE, moduleFile);

    // Forge bug: this does not actually invoke ModuleCoreFacet#install when all tests are run
//    facetFactory.install(project, ModuleCoreFacet.class);
    final ModuleCoreFacet facet = facetFactory.create(project, ModuleCoreFacet.class);
    facet.install();

    final String moduleContent = getFileContentAsString(moduleFile);
    assertTrue(moduleContent, moduleContent.contains("<inherits name=\"com.google.gwt.user.User\"/>"));
    assertEquals(1, countMatches("<inherits name=\"[^\"]*\"/>", moduleContent));
  }

  @Test
  public void testAbstractModuleFacetIsInstalled() throws Exception {
    final Project project = initializeJavaProject();
    String body = ModuleCoreFacet.emptyModuleContents.replace("</module>",
            "<inherits name='org.jboss.errai.common.ErraiCommon'/>\n" + "<inherits name='com.google.gwt.user.User'/>\n"
                    + "</module>");
    final File moduleFile = makeBlankModuleFile(project, body);
    final ProjectConfig config = facetFactory.install(project, ProjectConfig.class);
    final ProjectFacet facet = facetFactory.create(project, SimpleModuleFacet.class);
    config.setProjectProperty(ProjectProperty.MODULE_FILE, moduleFile);

    assertTrue(facet.isInstalled());
  }

  @Test
  public void testAbstractModuleFacetIsInstalledNegative()
          throws Exception {
    final Project project = initializeJavaProject();
    String body = ModuleCoreFacet.emptyModuleContents;
    final File moduleFile = makeBlankModuleFile(project, body);
    final ProjectConfig config = facetFactory.install(project, ProjectConfig.class);
    final ProjectFacet facet = facetFactory.create(project, SimpleModuleFacet.class);
    config.setProjectProperty(ProjectProperty.MODULE_FILE, moduleFile);

    assertFalse(facet.isInstalled());
  }

  @Test
  public void testAbstractModuleFacetUninstall() throws Exception {
    final Project project = initializeJavaProject();
    String body = ModuleCoreFacet.emptyModuleContents.replace("</module>",
            "<inherits name='org.jboss.errai.common.ErraiCommon'/>\n" + "<inherits name='com.google.gwt.user.User'/>\n"
                    + "</module>");
    final File moduleFile = makeBlankModuleFile(project, body);
    final ProjectConfig config = facetFactory.install(project, ProjectConfig.class);
    final ProjectFacet facet = facetFactory.create(project, SimpleModuleFacet.class);
    config.setProjectProperty(ProjectProperty.MODULE_FILE, moduleFile);

    boolean res = facet.uninstall();

    assertTrue(res);
    assertEquals(0, countMatches("<inherits name=\"[^\"]*\"/>", getFileContentAsString(moduleFile)));
  }

  private int countMatches(final String regex, final String content) {
    final Pattern pattern = Pattern.compile(regex);
    final Matcher matcher = pattern.matcher(content);

    int count = 0;
    while (matcher.find())
      count++;

    return count;
  }

  private File makeBlankModuleFile(final Project project, final String body) throws IOException {
    final String sourcePath;
    final MavenFacet mavenFacet = project.getFacet(MavenFacet.class);
    final Model model = mavenFacet.getModel();

    if (model.getBuild() != null && model.getBuild().getSourceDirectory() != null) {
      sourcePath = model.getBuild().getSourceDirectory();
    }
    else {
      sourcePath = "src/main/java";
    }

    final File moduleFile = new File(new File(project.getRootDirectory().getUnderlyingResourceObject(), sourcePath),
            "org/jboss/errai/Test.gwt.xml");
    moduleFile.getParentFile().mkdirs();
    moduleFile.createNewFile();
    final FileWriter writer = new FileWriter(moduleFile);
    writer.append(body);
    writer.close();

    return moduleFile;
  }

  private String getFileContentAsString(File f) throws IOException {
    final StringBuilder builder = new StringBuilder();
    final FileReader reader = new FileReader(f);
    char[] buf = new char[256];
    int amt;
    do {
      amt = reader.read(buf);
      if (amt > -1)
        builder.append(buf, 0, amt);
      else
        break;
    }
    while (true);
    reader.close();

    return builder.toString();
  }

}
