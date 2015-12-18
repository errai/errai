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

package org.jboss.errai.forge.facet.plugin;

import org.apache.maven.model.PluginExecution;
import org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact;
import org.jboss.errai.forge.facet.base.AbstractBaseFacet;
import org.jboss.errai.forge.facet.base.CoreBuildFacet;
import org.jboss.forge.addon.dependencies.builder.CoordinateBuilder;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.maven.plugins.Configuration;
import org.jboss.forge.addon.maven.plugins.ConfigurationElement;
import org.jboss.forge.addon.maven.plugins.ConfigurationElementBuilder;
import org.jboss.forge.addon.maven.projects.MavenPluginFacet;
import org.jboss.forge.addon.projects.Project;

import java.util.ArrayList;
import java.util.Arrays;

import static org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact.War;
import static org.jboss.errai.forge.constant.DefaultVault.DefaultValue.WarSourceDirectory;

/**
 * This facet configures the maven-war-plugin in the
 * {@link AbstractBaseFacet#MAIN_PROFILE main profile} of the pom file.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@FacetConstraint({ CoreBuildFacet.class })
public class WarPluginFacet extends AbstractProfilePluginFacet {

  public WarPluginFacet() {
    pluginArtifact = DependencyArtifact.War;
    dependencies = new ArrayList<DependencyBuilder>(0);
    executions = new ArrayList<PluginExecution>(0);
    configurations = new ArrayList<ConfigurationElement>(Arrays.asList(
            ConfigurationElementBuilder.create()
              .setName("packagingExcludes")
              .setText("**/javax/**/*.*,**/client/local/**/*.class"),
            ConfigurationElementBuilder.create()
              .setName("outputFileNameMapping")
              .setText("@{artifactId}@-@{baseVersion}@@{dashClassifier?}@.@{extension}@")));
  }

  public static String getWarSourceDirectory(final Project project) {
    final MavenPluginFacet pluginFacet = project.getFacet(MavenPluginFacet.class);
    final CoordinateBuilder warPluginCoordinate = CoordinateBuilder.create().setGroupId(War.getGroupId())
            .setArtifactId(War.getArtifactId());

    if (pluginFacet.hasPlugin(warPluginCoordinate)) {
      final Configuration warConfig = pluginFacet.getPlugin(warPluginCoordinate).getConfig();
      if (warConfig.hasConfigurationElement(WarSourceDirectory.getValueName())) {
        return warConfig.getConfigurationElement(WarSourceDirectory.getValueName()).getText();
      }
    }

    return WarSourceDirectory.getDefaultValue();
  }

  @Override
  protected void init() {
    configurations.add(ConfigurationElementBuilder.create().setName(WarSourceDirectory.getValueName())
            .setText(getWarSourceDirectory(getProject())));
  }
}
