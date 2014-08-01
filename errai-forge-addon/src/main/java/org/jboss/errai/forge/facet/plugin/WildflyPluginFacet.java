/**
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.forge.facet.plugin;

import static org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact.WildflyPlugin;

import java.util.ArrayList;

import org.apache.maven.model.PluginExecution;
import org.jboss.errai.forge.facet.base.AbstractBaseFacet;
import org.jboss.errai.forge.facet.base.CoreBuildFacet;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.maven.plugins.ConfigurationElement;

/**
 * This facet configures the jboss-as-maven-plugin in the
 * {@link AbstractBaseFacet#MAIN_PROFILE main profile} of the pom file.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@FacetConstraint({ CoreBuildFacet.class })
public class WildflyPluginFacet extends AbstractProfilePluginFacet {

  public WildflyPluginFacet() {
    pluginArtifact = WildflyPlugin;
    dependencies = new ArrayList<DependencyBuilder>(0);
    executions = new ArrayList<PluginExecution>(0);
    configurations = new ArrayList<ConfigurationElement>(0);
    extensions = false;
  }
}
