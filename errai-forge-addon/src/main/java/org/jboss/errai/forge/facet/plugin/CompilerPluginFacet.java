package org.jboss.errai.forge.facet.plugin;

import java.util.ArrayList;
import java.util.Arrays;

import org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact;
import org.jboss.errai.forge.facet.base.CoreBuildFacet;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.maven.plugins.ConfigurationElement;
import org.jboss.forge.addon.maven.plugins.ConfigurationElementBuilder;
import org.jboss.forge.addon.maven.plugins.Execution;

/**
 * This facet configures the maven-compiler-plugin in the build section of the pom file.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@FacetConstraint({ CoreBuildFacet.class })
public class CompilerPluginFacet extends AbstractPluginFacet {

  public CompilerPluginFacet() {
    pluginArtifact = DependencyArtifact.Compiler;
    dependencies = new ArrayList<DependencyBuilder>(0);
    executions = new ArrayList<Execution>(0);
    configurations = Arrays.asList(new ConfigurationElement[] {
            ConfigurationElementBuilder.create().setName("source").setText("1.6"),
            ConfigurationElementBuilder.create().setName("target").setText("1.6")
    });
  }
  
}
