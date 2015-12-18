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
import org.jboss.errai.forge.facet.base.CoreBuildFacet;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.maven.plugins.ConfigurationElement;
import org.jboss.forge.addon.maven.plugins.ConfigurationElementBuilder;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Configures the cordova plugin for building native mobile apps.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
@FacetConstraint({ CoreBuildFacet.class })
public class CordovaPluginFacet extends AbstractProfilePluginFacet {

  public CordovaPluginFacet() {
    profileId = "mobile";
    pluginArtifact = DependencyArtifact.CordovaPlugin;
    dependencies = new ArrayList<DependencyBuilder>(0);

    final PluginExecution execution = new PluginExecution();
    execution.setId("build");
    execution.setPhase("package");
    execution.addGoal("build-project");

    executions = Arrays.asList(new PluginExecution[] { execution });
    configurations = Arrays.asList(new ConfigurationElement[] {
        ConfigurationElementBuilder.create().setName("source").setText("1.6"),
        ConfigurationElementBuilder.create().setName("target").setText("1.6")
    });
  }

}
