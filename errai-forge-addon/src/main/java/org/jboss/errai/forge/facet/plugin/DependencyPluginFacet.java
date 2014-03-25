package org.jboss.errai.forge.facet.plugin;

import java.util.ArrayList;
import java.util.Arrays;

import org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact;
import org.jboss.errai.forge.facet.base.CoreBuildFacet;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.maven.plugins.ConfigurationBuilder;
import org.jboss.forge.addon.maven.plugins.ConfigurationElement;
import org.jboss.forge.addon.maven.plugins.ConfigurationElementBuilder;
import org.jboss.forge.addon.maven.plugins.Execution;
import org.jboss.forge.addon.maven.plugins.ExecutionBuilder;

/**
 * This facet configures the maven-dependency-plugin in the build section of the pom file.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@FacetConstraint({ CoreBuildFacet.class })
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
                                            .setName("groupId").setText("org.jboss.as"))
                                    .addChild(ConfigurationElementBuilder.create()
                                            .setName("artifactId").setText("jboss-as-dist"))
                                    .addChild(ConfigurationElementBuilder.create()
                                            .setName("version").setText("7.1.1.Final"))
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
  
}
