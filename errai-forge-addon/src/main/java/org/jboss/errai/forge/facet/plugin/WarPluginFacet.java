package org.jboss.errai.forge.facet.plugin;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.Profile;
import org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact;
import org.jboss.errai.forge.constant.DefaultVault.DefaultValue;
import org.jboss.errai.forge.facet.base.AbstractBaseFacet;
import org.jboss.errai.forge.facet.base.CoreBuildFacet;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.maven.plugins.ConfigurationElement;
import org.jboss.forge.addon.maven.plugins.ConfigurationElementBuilder;
import org.jboss.forge.addon.maven.plugins.MavenPluginAdapter;
import org.jboss.forge.addon.maven.projects.MavenFacet;
import org.jboss.forge.addon.projects.Project;

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
    configurations = Arrays.asList(new ConfigurationElement[] {
        ConfigurationElementBuilder.create().setName("packagingExcludes")
                .setText("**/javax/**/*.*,**/client/local/**/*.class"),
        ConfigurationElementBuilder.create().setName("outputFileNameMapping")
                .setText("@{artifactId}@-@{baseVersion}@@{dashClassifier?}@.@{extension}@"), });
  }

  public static String getWarSourceDirectory(final Project project) {
    final MavenFacet coreFacet = project.getFacet(MavenFacet.class);
    final Profile profile = getProfile(MAIN_PROFILE, coreFacet.getModel().getProfiles());

    if (profile != null && profile.getBuild() != null
            && profile.getBuild().getPluginsAsMap().containsKey(DependencyArtifact.War.toString())) {
      final Plugin warPlugin = profile.getBuild().getPluginsAsMap().get(DependencyArtifact.War.toString());
      final MavenPluginAdapter adapter = new MavenPluginAdapter(warPlugin);

      if (adapter.getConfig() != null
              && adapter.getConfig().hasConfigurationElement(DefaultValue.WarSourceDirectory.getValueName())) {
        adapter.getConfig().getConfigurationElement(DefaultValue.WarSourceDirectory.getValueName()).getText();
      }
    }

    return DefaultValue.WarSourceDirectory.getDefaultValue();
  }
}
