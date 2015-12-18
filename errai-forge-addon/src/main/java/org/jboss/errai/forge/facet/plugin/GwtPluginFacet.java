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
import org.jboss.errai.forge.constant.PomPropertyVault.Property;
import org.jboss.errai.forge.facet.base.CoreBuildFacet;
import org.jboss.errai.forge.facet.resource.GwtHostPageFacet;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.maven.plugins.ConfigurationElement;
import org.jboss.forge.addon.maven.plugins.ConfigurationElementBuilder;
import org.jboss.forge.addon.maven.plugins.Execution;
import org.jboss.forge.addon.maven.plugins.ExecutionBuilder;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * This facet configures the gwt-maven-plugin in the build section of the pom
 * file.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@FacetConstraint({ CoreBuildFacet.class, GwtHostPageFacet.class })
public class GwtPluginFacet extends AbstractPluginFacet {

  public GwtPluginFacet() {
    pluginArtifact = DependencyArtifact.GwtPlugin;
    dependencies = new ArrayList<DependencyBuilder>(0);
    executions = Arrays.asList(new Execution[] {
        // Note: phase and id must be explicitly set, otherwise forge will write
        // "null" as values to the tags
        //ExecutionBuilder.create().setId("resources").setPhase("process-resources").addGoal("resources"),

        ExecutionBuilder.create().setId("compile").setPhase("prepare-package").addGoal("compile")
    });
    configurations = Arrays
            .asList(new ConfigurationElement[] {
                ConfigurationElementBuilder.create().setName("logLevel").setText("INFO"),
                ConfigurationElementBuilder.create().setName("noServer").setText("false"),
                ConfigurationElementBuilder.create().setName("server")
                        .setText("org.jboss.errai.cdi.server.gwt.JBossLauncher"),
                ConfigurationElementBuilder.create().setName("disableCastChecking").setText("true"),
                ConfigurationElementBuilder.create().setName("runTarget").setText("${errai.dev.context}/index.html"),
                ConfigurationElementBuilder.create().setName("soyc").setText("false"),
                ConfigurationElementBuilder.create().setName("hostedWebapp"),
                ConfigurationElementBuilder
                        .create()
                        .setName("extraJvmArgs")
                        .setText(
                                "-Xmx712m "
                                        + "-XX:CompileThreshold=7000 "
                                        + "-XX:MaxPermSize=128M "
                                        + "-D"
                                        + Property.JbossHome.getName()
                                        + "="
                                        + Property.JbossHome.invoke()
                                        + " "
                                        + "-D"
                                        + Property.DevContext.getName()
                                        + "="
                                        + Property.DevContext.invoke()
                                        + " "
                                        + "-Derrai.jboss.javaagent.path=${settings.localRepository}/org/jboss/errai/errai-client-local-class-hider/"
                                        + Property.ErraiVersion.invoke() + "/errai-client-local-class-hider-"
                                        + Property.ErraiVersion.invoke() + ".jar"
                        )
            });
  }

  @Override
  protected void init() {
    for (final ConfigurationElement elem : configurations) {
      if (elem.getName().equals("hostedWebapp")) {
        ConfigurationElementBuilder.class.cast(elem).setText(WarPluginFacet.getWarSourceDirectory(getProject()));
        break;
      }
    }
  }
}
