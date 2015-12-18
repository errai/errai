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

import org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact;
import org.jboss.errai.forge.facet.base.CoreBuildFacet;
import org.jboss.errai.forge.util.VersionFacet;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.maven.plugins.*;

import java.util.ArrayList;
import java.util.Arrays;

import static org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact.WildflyDist;

/**
 * This facet configures the maven-dependency-plugin in the build section of the pom file.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@FacetConstraint({ CoreBuildFacet.class, VersionFacet.class })
public class DependencyPluginFacet extends AbstractPluginFacet {

  public DependencyPluginFacet() {
    pluginArtifact = DependencyArtifact.Dependency;
    configurations = new ArrayList<ConfigurationElement>(0);
    dependencies = new ArrayList<DependencyBuilder>(0);
    executions = Arrays.asList(new Execution[] {
            ExecutionBuilder.create().setId("unpack").setPhase("process-resources").addGoal("unpack")
            .setConfig(ConfigurationBuilder.create()
                    .addConfigurationElement(ConfigurationElementBuilder.create().setName("artifactItems")
                            .addChild(ConfigurationElementBuilder.create().setName("artifactItem")
                                    .addChild(ConfigurationElementBuilder.create()
                                            .setName("groupId").setText(WildflyDist.getGroupId()))
                                    .addChild(ConfigurationElementBuilder.create()
                                            .setName("artifactId").setText(WildflyDist.getArtifactId()))
                                    .addChild(ConfigurationElementBuilder.create()
                                            .setName("type").setText("zip"))
                                    .addChild(ConfigurationElementBuilder.create()
                                            .setName("overWrite").setText("false"))
                                    .addChild(ConfigurationElementBuilder.create()
                                            .setName("outputDirectory").setText("${project.build.directory}"))

                             )
                    )
            )
    });
  }

  @Override
  protected void init() {
    final Execution execution = executions.iterator().next();
    final ConfigurationElement artifactItems = execution.getConfig().getConfigurationElement("artifactItems");
    final ConfigurationElementBuilder artifactItem = (ConfigurationElementBuilder) artifactItems.getChildren().get(0);

    final VersionFacet versionFacet = getProject().getFacet(VersionFacet.class);
    artifactItem.addChild(ConfigurationElementBuilder.create().setName("version")
            .setText(versionFacet.resolveVersion(WildflyDist)));
  }

}
