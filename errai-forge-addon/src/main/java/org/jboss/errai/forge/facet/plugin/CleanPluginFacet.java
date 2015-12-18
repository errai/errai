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

import org.jboss.errai.forge.config.ProjectConfig;
import org.jboss.errai.forge.config.ProjectProperty;
import org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact;
import org.jboss.errai.forge.facet.base.CoreBuildFacet;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.maven.plugins.ConfigurationElement;
import org.jboss.forge.addon.maven.plugins.ConfigurationElementBuilder;
import org.jboss.forge.addon.maven.plugins.Execution;
import org.jboss.forge.addon.maven.plugins.PluginElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This facet configures the maven-clean-plugin in the build section of the
 * projects pom file.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@FacetConstraint({ CoreBuildFacet.class })
public class CleanPluginFacet extends AbstractPluginFacet {

  private static final String WAR_SRC_PLACEHOLDER = "${warSrc}";

  public CleanPluginFacet() {
    pluginArtifact = DependencyArtifact.Clean;
    dependencies = new ArrayList<DependencyBuilder>(0);
    executions = new ArrayList<Execution>(0);

    configurations = Arrays.asList(new ConfigurationElement[] {
        ConfigurationElementBuilder.create().setName("filesets").addChild(
                ConfigurationElementBuilder.create().setName("fileset")
                        .addChild(ConfigurationElementBuilder.create().setName("directory").setText("${basedir}"))
                        .addChild(ConfigurationElementBuilder.create().setName("includes")
                                .addChild(ConfigurationElementBuilder.create().setName("include")
                                        .setText(WAR_SRC_PLACEHOLDER + "/WEB-INF/deploy/"))
                                .addChild(ConfigurationElementBuilder.create().setName("include")
                                        .setText(WAR_SRC_PLACEHOLDER + "/WEB-INF/lib/"))
                                .addChild(ConfigurationElementBuilder.create().setName("include")
                                        .setText(WAR_SRC_PLACEHOLDER + "/WEB-INF/classes/"))
                                .addChild(ConfigurationElementBuilder.create().setName("include")
                                        .setText("**/gwt-unitCache/**"))
                                .addChild(ConfigurationElementBuilder.create().setName("include")
                                        .setText(".errai/"))
                        )
                )
    });
  }

  @Override
  protected void init() {
    final String moduleName = getProject().getFacet(ProjectConfig.class).getProjectProperty(
            ProjectProperty.MODULE_NAME,
            String.class);

    addGwtModuleInclude(moduleName);
    convertWarSrcPlaceholder(WarPluginFacet.getWarSourceDirectory(getProject()));
  }

  private void addGwtModuleInclude(final String moduleName) {
    ((ConfigurationElementBuilder) configurations.iterator().next().getChildByName("includes"))
            .addChild(ConfigurationElementBuilder.create().setName("include")
                    .setText(WAR_SRC_PLACEHOLDER + "/" + moduleName + "/"));
  }

  private void convertWarSrcPlaceholder(final String warSrcDirectory) {
    final List<PluginElement> includes = configurations.iterator().next().getChildByName("includes").getChildren();
    for (final PluginElement include : includes) {
      final ConfigurationElementBuilder includeAsBuilder = (ConfigurationElementBuilder) include;
      includeAsBuilder.setText(includeAsBuilder.getText().replace(WAR_SRC_PLACEHOLDER, warSrcDirectory));
    }
  }
}
