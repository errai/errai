package org.jboss.errai.forge.facet.plugin;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.maven.model.PluginExecution;
import org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.maven.plugins.ConfigurationElement;
import org.jboss.forge.addon.maven.plugins.ConfigurationElementBuilder;

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
