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

import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.jboss.errai.forge.constant.ArtifactVault;
import org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact;
import org.jboss.errai.forge.constant.PomPropertyVault.Property;
import org.jboss.errai.forge.facet.base.AbstractBaseFacet;
import org.jboss.errai.forge.util.VersionFacet;
import org.jboss.forge.addon.dependencies.Dependency;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.maven.plugins.*;
import org.jboss.forge.addon.maven.projects.MavenFacet;
import org.jboss.forge.addon.maven.projects.MavenPluginFacet;

import java.util.Collection;

/**
 * This is a base class for facets that add Maven plugins to the build section
 * of the pom file. Concrete subclasses must assign values to the fields
 * {@link AbstractPluginFacet#pluginArtifact pluginArtifact},
 * {@link AbstractPluginFacet#configurations configurations},
 * {@link AbstractPluginFacet#dependencies dependencies}, and
 * {@link AbstractPluginFacet#executions executions}
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public abstract class AbstractPluginFacet extends AbstractBaseFacet {

  /**
   * The Maven artifact of the plugin to be installed.
   */
  protected DependencyArtifact pluginArtifact;

  private boolean isInitialized;

  public DependencyArtifact getPluginArtifact() {
    return pluginArtifact;
  }

  /**
   * Configurations for the plugin.
   */
  protected Collection<ConfigurationElement> configurations;

  public Collection<ConfigurationElement> getConfigurations() {
    return configurations;
  }

  public Collection<DependencyBuilder> getDependencies() {
    return dependencies;
  }

  public Collection<Execution> getExecutions() {
    return executions;
  }

  /**
   * Dependencies for the plugin.
   */
  protected Collection<DependencyBuilder> dependencies;
  /**
   * Executions for the plugin.
   */
  protected Collection<Execution> executions;

  @Override
  public boolean install() {
    maybeInit();
    final MavenPluginFacet pluginFacet = getProject().getFacet(MavenPluginFacet.class);
    final VersionFacet versionFacet = getProject().getFacet(VersionFacet.class);
    final Dependency pluginDep = DependencyBuilder.create(getPluginArtifact().toString()).setVersion(
            versionFacet.resolveVersion(getPluginArtifact()));
    final MavenPluginBuilder plugin;

    if (pluginFacet.hasPlugin(pluginDep.getCoordinate())) {
      plugin = MavenPluginBuilder.create(pluginFacet.getPlugin(pluginDep.getCoordinate()));
      // So that it is not duplicated when added later on
      pluginFacet.removePlugin(pluginDep.getCoordinate());
    }
    else {
      plugin = MavenPluginBuilder.create();
      plugin.setCoordinate(pluginDep.getCoordinate());
    }

    Configuration config = plugin.getConfig();
    for (final ConfigurationElement configElem : getConfigurations()) {
      mergeConfigurationElement(config, configElem);
    }

    for (final DependencyBuilder dep : getDependencies()) {
      if (dep.getCoordinate().getVersion() == null || dep.getCoordinate().getVersion().equals("")) {
        if (dep.getGroupId().equals(ArtifactVault.ERRAI_GROUP_ID))
          dep.setVersion(Property.ErraiVersion.invoke());
        else
          dep.setVersion(versionFacet.resolveVersion(dep.getGroupId(), dep.getCoordinate().getArtifactId()));
      }
      plugin.addPluginDependency(dep);
    }

    for (final Execution exec : getExecutions()) {
      plugin.addExecution(exec);
    }
    pluginFacet.addPlugin(plugin);

    return true;
  }

  @Override
  public boolean isInstalled() {
    maybeInit();
    final MavenFacet coreFacet = getProject().getFacet(MavenFacet.class);
    final Model pom = coreFacet.getModel();
    if (pom.getBuild() == null)
      return false;

    final Plugin plugin = pom.getBuild().getPluginsAsMap().get(getPluginArtifact().toString());

    if (plugin == null)
      return false;

    outer: for (final DependencyBuilder dep : getDependencies()) {
      for (final org.apache.maven.model.Dependency pluginDep : plugin.getDependencies()) {
        if (dep.getCoordinate().getArtifactId().equals(pluginDep.getArtifactId())
                && dep.getGroupId().equals(pluginDep.getGroupId()))
          continue outer;
      }
      return false;
    }

    final MavenPluginFacet pluginFacet = getProject().getFacet(MavenPluginFacet.class);
    final MavenPlugin mPlugin = pluginFacet.getPlugin(DependencyBuilder.create(getPluginArtifact().toString())
            .getCoordinate());

    outer: for (final Execution exec : getExecutions()) {
      for (final Execution pluginExec : mPlugin.listExecutions()) {
        // TODO check more than just id
        if (exec.getId().equals(pluginExec.getId()))
          continue outer;
      }
      return false;
    }

    if (!isMatchingConfiguration(mPlugin.getConfig(), getConfigurations()))
      return false;

    return true;
  }

  /**
   * Check that a {@link Configuration} is consistent with a collection of
   * {@link ConfigurationElement ConfigurationElements}.
   * 
   * A configuration is consistent with a collection if for any element in the
   * collection, {@code elem}, there exists an element in the configuration,
   * {@code other}, such that
   * {@link AbstractPluginFacet#isMatchingElement(ConfigurationElement, ConfigurationElement)
   * isMatchingElement}{@code (elem, matching)} is {@code true}.
   * 
   * @param config
   *          A Maven plugin configuration.
   * @param elements
   *          A set of configuration elements for a Maven plugin.
   * @return True iff the given configuration is consistent with the given
   *         collection of configuration elements.
   */
  protected static boolean isMatchingConfiguration(final Configuration config,
          final Collection<ConfigurationElement> elements) {
    for (final ConfigurationElement elem : elements) {
      if (!config.hasConfigurationElement(elem.getName())
              || !isMatchingElement(config.getConfigurationElement(elem.getName()), elem))
        return false;
    }

    return true;
  }

  /**
   * Checks that the given {@link ConfigurationElement} is consistent with the
   * expected one. This means that the expected configuration tree is a subtree
   * of the given (i.e. the given configuration can have <i>additional</i>
   * elements, but must not be missing any).
   * 
   * @param given
   *          The given configuration element.
   * @param expected
   *          The expected configuration element.
   * @return True if expected is a subtree of given.
   */
  private static boolean isMatchingElement(final ConfigurationElement given, final ConfigurationElement expected) {
    if (given == null)
      return false;

    if (expected.hasChildren()) {
      for (final PluginElement pluginElem : expected.getChildren()) {
        if (pluginElem instanceof ConfigurationElement) {
          final ConfigurationElement elem = ConfigurationElement.class.cast(pluginElem);
          ConfigurationElement child;
          int i;
          for (i = 0; i < given.getChildren().size(); i++) {
            child = ConfigurationElement.class.cast(given.getChildren().get(i));
            if (child.getName().equals(elem.getName()) && isMatchingElement(child, elem)) {
              return true;
            }
          }
          if (i == given.getChildren().size())
            return false;
        }
      }

      return true;
    }
    else {
      return expected.getText().equals(given.getText());
    }
  }

  @Override
  public boolean uninstall() {
    maybeInit();
    final MavenPluginFacet pluginFacet = getProject().getFacet(MavenPluginFacet.class);
    pluginFacet.removePlugin(DependencyBuilder.create(getPluginArtifact().toString()).getCoordinate());

    return true;
  }

  /**
   * Merge a {@link ConfigurationElement} into a {@link Configuration}. If there
   * is no element in the given configuration with a name matching the given
   * element, the element is simply added. Otherwise, the two elements will be
   * recursively merged, with any conflicting values in the configuration being
   * overwritten.
   * 
   * @param config
   *          A Maven plugin configuration.
   * @param configElem
   *          A Maven plugin configuration element.
   */
  protected void mergeConfigurationElement(final Configuration config, final ConfigurationElement configElem) {
    if (!config.hasConfigurationElement(configElem.getName())) {
      config.addConfigurationElement(configElem);
    }
    else {
      final ConfigurationElement prev = config.getConfigurationElement(configElem.getName());
      config.removeConfigurationElement(configElem.getName());
      config.addConfigurationElement(merge(prev, configElem));
    }
  }

  /**
   * Recursively merge two configuration elements.
   * 
   * @param prev
   *          A Maven plugin configuration element.
   * @param configElem
   *          A Maven plugin configuration element. Values in this element and
   *          it's children will take precedence over conflicting values in the
   *          other argument.
   * @return A merged configuration element.
   */
  protected ConfigurationElement merge(ConfigurationElement prev, ConfigurationElement configElem) {
    // Replace text-only elements
    if (!prev.hasChildren()) {
      return configElem;
    }
    else {
      final ConfigurationElementBuilder retVal = ConfigurationElementBuilder.create();
      // Copy non-conflicting elements from old config element
      for (final PluginElement child : prev.getChildren()) {
        if (!(child instanceof ConfigurationElement)) {
          // configElem should only contain other ConfigurationElemnents, so
          // this case is non-conflicting
          retVal.addChild(child);
        }
        else {
          final ConfigurationElement oldChild = ConfigurationElement.class.cast(child);
          if (!configElem.hasChildByName(oldChild.getName(), true))
            retVal.addChild(oldChild);
        }
      }
      // Add or merge from new config element
      for (final PluginElement child : configElem.getChildren()) {
        if (!(child instanceof ConfigurationElement)) {
          throw new IllegalArgumentException("Cannot merge PluginElement of type " + child.getClass().getName());
        }
        else {
          final ConfigurationElement newChild = ConfigurationElement.class.cast(child);
          if (prev.hasChildByName(newChild.getName(), true)) {
            retVal.addChild(merge(prev.getChildByName(newChild.getName(), true), newChild));
          }
          else {
            retVal.addChild(newChild);
          }
        }
      }

      return retVal;
    }
  }

  /**
   * This method is invoked exactly once after a project has been set for this facet. Subclasses should override this to
   * perform tasks that require access to the project (such as requiring other facets).
   */
  protected void init() {
  }

  protected final void maybeInit() {
    if (!isInitialized) {
      init();
      isInitialized = true;
    }
  }
}
