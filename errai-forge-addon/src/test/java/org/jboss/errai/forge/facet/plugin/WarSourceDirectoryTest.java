/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.forge.facet.plugin;

import org.jboss.errai.forge.facet.aggregate.CoreFacet;
import org.jboss.errai.forge.test.base.ForgeTest;
import org.jboss.forge.addon.dependencies.builder.CoordinateBuilder;
import org.jboss.forge.addon.maven.plugins.*;
import org.jboss.forge.addon.maven.projects.MavenPluginFacet;
import org.jboss.forge.addon.projects.Project;
import org.junit.Before;
import org.junit.Test;

import static org.jboss.errai.forge.constant.DefaultVault.DefaultValue.WarSourceDirectory;
import static org.junit.Assert.*;

public class WarSourceDirectoryTest extends ForgeTest {

  private Project project;

  @Before
  public void setup() {
    project = createErraiTestProject();
  }

  @Test
  public void useDefaultWhenNoDirectoryDefined() throws Exception {
    facetFactory.install(project, CoreFacet.class);

    assertWarSourceDirectoryIsUsed(WarSourceDirectory.getDefaultValue());
  }

  @Test
  public void useGivenWarSourceDirectory() throws Exception {
    final MavenPluginFacet pluginFacet = project.getFacet(MavenPluginFacet.class);
    final MavenPlugin warPlugin = MavenPluginBuilder.create().setCoordinate(CoordinateBuilder.create()
            .setGroupId("org.apache.maven.plugins").setArtifactId("maven-war-plugin"));

    final String warSourceDirectory = "war";
    warPlugin.getConfig()
            .addConfigurationElement(
                    ConfigurationElementBuilder.create().setName(WarSourceDirectory.getValueName())
                            .setText(warSourceDirectory));

    pluginFacet.addPlugin(warPlugin);

    facetFactory.install(project, CoreFacet.class);

    assertWarSourceDirectoryIsUsed(warSourceDirectory);
  }

  private void assertWarSourceDirectoryIsUsed(final String warSourceDirectory) {
    final MavenPluginFacet pluginFacet = project.getFacet(MavenPluginFacet.class);
    final ConfigurationElement gwtMavenHostedWebapp = getGwtMavenPluginHostedWebapp(pluginFacet);
    final ConfigurationElement cleanPluginIncludes = getCleanPluginIncludes(pluginFacet);
    final ConfigurationElement warSourceElement = getWarSourceDirectory(project.getFacet(WarPluginFacet.class));

    assertWarSourceDirectoryInIncludes(warSourceDirectory, cleanPluginIncludes);
    assertEquals(warSourceDirectory, gwtMavenHostedWebapp.getText());
    assertEquals(warSourceDirectory, warSourceElement.getText());
  }

  private ConfigurationElement getWarSourceDirectory(final WarPluginFacet warPluginFacet) {
    for (final ConfigurationElement configElement : warPluginFacet.getConfigurations()) {
      if (configElement.getName().equals(WarSourceDirectory.getValueName())) {
        return configElement;
      }
    }

    fail("Could not find war source directory configuration.");
    return null;
  }

  private void assertWarSourceDirectoryInIncludes(final String warSourceDirectory,
          final ConfigurationElement includesElement) {
    assertNotNull(includesElement);
    for (final PluginElement include : includesElement.getChildren()) {
      final String text = ((ConfigurationElement) include).getText();
      if (text.contains("WEB-INF")) {
        assertTrue(text.startsWith(warSourceDirectory));
      }
    }
  }

  private ConfigurationElement getCleanPluginIncludes(final MavenPluginFacet pluginFacet) {
    final String groupId = "org.apache.maven.plugins";
    final String artifactId = "maven-clean-plugin";
    return getPlugin(pluginFacet, groupId, artifactId).getConfig().getConfigurationElement("filesets")
            .getChildByName("includes");
  }

  private ConfigurationElement getGwtMavenPluginHostedWebapp(final MavenPluginFacet pluginFacet) {
    final String groupId = "org.codehaus.mojo";
    final String artifactId = "gwt-maven-plugin";
    return getPlugin(pluginFacet, groupId, artifactId).getConfig().getConfigurationElement("hostedWebapp");
  }

  private MavenPlugin getPlugin(final MavenPluginFacet pluginFacet, final String groupId, final String artifactId) {
    return pluginFacet.getPlugin(CoordinateBuilder.create().setGroupId(groupId).setArtifactId(artifactId));
  }

}
