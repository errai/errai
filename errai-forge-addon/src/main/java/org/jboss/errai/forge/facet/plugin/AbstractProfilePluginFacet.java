package org.jboss.errai.forge.facet.plugin;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.maven.model.BuildBase;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.Profile;
import org.jboss.errai.forge.constant.ArtifactVault;
import org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact;
import org.jboss.errai.forge.constant.PomPropertyVault.Property;
import org.jboss.errai.forge.facet.base.AbstractBaseFacet;
import org.jboss.errai.forge.util.MavenConverter;
import org.jboss.errai.forge.util.VersionOracle;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.maven.plugins.Configuration;
import org.jboss.forge.addon.maven.plugins.ConfigurationElement;
import org.jboss.forge.addon.maven.plugins.MavenPluginAdapter;
import org.jboss.forge.addon.maven.projects.MavenFacet;
import org.jboss.forge.addon.projects.facets.DependencyFacet;

/**
 * A base class for facets that install Maven plugins within the given profile
 * id of the pom file. Concrete subclasses must assign values to the fields
 * {@link AbstractPluginFacet#pluginArtifact pluginArtifact},
 * {@link AbstractPluginFacet#configurations configurations},
 * {@link AbstractPluginFacet#dependencies dependencies},
 * {@link AbstractProfilePluginFacet#profileId profileId}, and
 * {@link AbstractProfilePluginFacet#executions executions}.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public abstract class AbstractProfilePluginFacet extends AbstractPluginFacet {
  // TODO Refactor AbstractProfilePluginFacet and AbstractPluginFacet to have common base class

  /**
   * Executions for the installed plugin.
   */
  protected Collection<PluginExecution> executions;
  
  public Collection<PluginExecution> getPluginExecutions() {
    return executions;
  }

  protected boolean isExtensions() {
    return extensions;
  }

  protected boolean extensions = true;

  /**
   * The profile to add this plugin to. Defaults to
   * {@link AbstractBaseFacet#MAIN_PROFILE the main profile}.
   */
  protected String profileId = MAIN_PROFILE;

  @Override
  public boolean install() {
    final MavenFacet coreFacet = getProject().getFacet(MavenFacet.class);
    Model pom = coreFacet.getModel();
    Profile profile = getProfile(profileId, pom.getProfiles());
    final VersionOracle oracle = new VersionOracle(getProject().getFacet(DependencyFacet.class));

    if (profile == null) {
      addDependenciesToProfile(profileId, Collections.<DependencyBuilder> emptyList(), oracle);
      pom = coreFacet.getModel();
      profile = getProfile(profileId, pom.getProfiles());
    }

    if (profile.getBuild() == null) {
      profile.setBuild(new BuildBase());
    }

    Plugin plugin = getPlugin(getPluginArtifact(), profile.getBuild().getPlugins());

    if (plugin == null) {
      plugin = new Plugin();
      plugin.setArtifactId(getPluginArtifact().getArtifactId());
      plugin.setGroupId(getPluginArtifact().getGroupId());
      if (ArtifactVault.ERRAI_GROUP_ID.equals(plugin.getGroupId()))
        plugin.setVersion(Property.ErraiVersion.invoke());
      else
        plugin.setVersion(oracle.resolveVersion(plugin.getGroupId(), plugin.getArtifactId()));
      profile.getBuild().addPlugin(plugin);
    }

    final MavenPluginAdapter adapter = new MavenPluginAdapter(plugin);
    final Configuration config = adapter.getConfig();
    for (final ConfigurationElement elem : getConfigurations()) {
      mergeConfigurationElement(config, elem);
    }
    adapter.setConfig(config);

    for (final DependencyBuilder depBuilder : getDependencies()) {
      if (depBuilder.getCoordinate().getVersion() == null || depBuilder.getCoordinate().getVersion().equals("")) {
        if (ArtifactVault.ERRAI_GROUP_ID.equals(depBuilder.getGroupId())) {
          depBuilder.setVersion(Property.ErraiVersion.invoke());
        }
        else {
          depBuilder.setVersion(new VersionOracle(getProject().getFacet(DependencyFacet.class)).resolveVersion(
                  depBuilder.getGroupId(), depBuilder.getCoordinate().getArtifactId()));
        }
      }
      adapter.addDependency(MavenConverter.convert(depBuilder));
    }

    for (final PluginExecution exec : getPluginExecutions()) {
      adapter.addExecution(exec);
    }
    adapter.setExtensions(isExtensions());

    // Changes are not committed from adapter to original plugin
    plugin.setConfiguration(adapter.getConfiguration());
    plugin.setExecutions(adapter.getExecutions());
    plugin.setDependencies(adapter.getDependencies());

    coreFacet.setModel(pom);

    return true;
  }

  @Override
  public boolean isInstalled() {
    final MavenFacet coreFacet = getProject().getFacet(MavenFacet.class);
    final Model pom = coreFacet.getModel();

    final Profile profile = getProfile(profileId, pom.getProfiles());
    if (profile == null || profile.getBuild() == null)
      return false;

    Plugin plugin = profile.getBuild().getPluginsAsMap().get(getPluginArtifact().toString());
    if (plugin == null) {
      plugin = new Plugin();
      plugin.setGroupId(pluginArtifact.getGroupId());
      plugin.setArtifactId(pluginArtifact.getArtifactId());
      
      final VersionOracle oracle = new VersionOracle(getFaceted().getFacet(DependencyFacet.class));
      plugin.setVersion(oracle.resolveVersion(pluginArtifact));
      
      profile.getBuild().addPlugin(plugin);
    }

    outer: for (final DependencyBuilder dep : getDependencies()) {
      for (final Dependency pluginDep : plugin.getDependencies()) {
        if (pluginDep.getArtifactId().equals(dep.getCoordinate().getArtifactId())
                && pluginDep.getGroupId().equals(dep.getGroupId()))
          continue outer;
      }
      return false;
    }

    outer: for (final PluginExecution exec : getPluginExecutions()) {
      for (final PluginExecution pluginExec : plugin.getExecutions()) {
        if (pluginExec.getId().equals(exec.getId()))
          continue outer;
      }
      return false;
    }

    final MavenPluginAdapter adapter = new MavenPluginAdapter(plugin);

    if (!isMatchingConfiguration(adapter.getConfig(), getConfigurations()))
      return false;

    return true;
  }

  @Override
  public boolean uninstall() {
    final MavenFacet coreFacet = getProject().getFacet(MavenFacet.class);
    final Model pom = coreFacet.getModel();

    final Profile profile = getProfile(profileId, pom.getProfiles());
    if (profile == null)
      return false;

    final BuildBase build = profile.getBuild();
    if (build == null)
      return false;

    final Plugin plugin = build.getPluginsAsMap().get(getPluginArtifact().toString());
    if (plugin == null)
      return false;

    build.removePlugin(plugin);
    profile.setBuild(build);
    coreFacet.setModel(pom);

    return true;
  }

  private Plugin getPlugin(DependencyArtifact pluginArtifact, List<Plugin> plugins) {
    for (final Plugin plugin : plugins) {
      if (pluginArtifact.getGroupId().equals(plugin.getGroupId())
              && pluginArtifact.getArtifactId().equals(plugin.getArtifactId())) {
        return plugin;
      }
    }

    return null;
  }

}
