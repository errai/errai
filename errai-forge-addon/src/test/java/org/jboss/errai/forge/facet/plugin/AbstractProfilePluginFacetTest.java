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

import org.apache.maven.model.BuildBase;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.Profile;
import org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact;
import org.jboss.errai.forge.facet.base.AbstractBaseFacet;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.maven.plugins.*;
import org.jboss.forge.addon.maven.projects.MavenFacet;
import org.jboss.forge.addon.projects.Project;
import org.junit.Assert;
import org.junit.Test;

import javax.enterprise.context.Dependent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.*;

public class AbstractProfilePluginFacetTest extends BasePluginFacetTest {

  public static abstract class BaseTestAbstractProfilePluginFacet extends AbstractProfilePluginFacet {
    @Override
    public DependencyArtifact getPluginArtifact() {
      return pluginArtifact;
    }
    @Override
    public Collection<ConfigurationElement> getConfigurations() {
      return configurations;
    }
    @Override
    public Collection<DependencyBuilder> getDependencies() {
      return dependencies;
    }
    @Override
    public Collection<PluginExecution> getPluginExecutions() {
      return executions;
    }
  }

  @Dependent
  public static class DefinitionOnly extends BaseTestAbstractProfilePluginFacet {
    public DefinitionOnly() {
      pluginArtifact = DependencyArtifact.Clean;
      configurations = Collections.emptyList();
      dependencies = Collections.emptyList();
      executions = Collections.emptyList();
    }
  }

  @Dependent
  public static class DependencyHavingPlugin extends BaseTestAbstractProfilePluginFacet {
    public DependencyHavingPlugin() {
      pluginArtifact = DependencyArtifact.Clean;
      configurations = Collections.emptyList();
      executions = Collections.emptyList();
      // Do not put errai dependencies here. They will fail because the
      // errai.version propery is not set.
      dependencies = Arrays.asList(new DependencyBuilder[] { DependencyBuilder.create(DependencyArtifact.Guava
              .toString()) });
    }
  }

  @Dependent
  public static class ConfigHavingPlugin extends BaseTestAbstractProfilePluginFacet {
    public ConfigHavingPlugin() {
      pluginArtifact = DependencyArtifact.Clean;
      configurations = Arrays.asList(new ConfigurationElement[] {
          ConfigurationElementBuilder.create().setName("configName").setText("configText"),
          ConfigurationElementBuilder.create().setName("parent")
                  .addChild(ConfigurationElementBuilder.create().setName("child").setText("childText")) });
      executions = Collections.emptyList();
      dependencies = Collections.emptyList();
    }
  }

  @Dependent
  public static class ExecutionHavingPlugin extends BaseTestAbstractProfilePluginFacet {
    public ExecutionHavingPlugin() {
      pluginArtifact = DependencyArtifact.Clean;
      configurations = Collections.emptyList();
      dependencies = Collections.emptyList();

      final PluginExecution exec = new PluginExecution();
      exec.setId("testExec");
      exec.setGoals(Arrays.asList(new String[] { "compile" }));
      exec.setPhase("test");
      final MavenPluginAdapter adapter = new MavenPluginAdapter(MavenPluginBuilder.create().setCoordinate(
              DependencyBuilder.create("maven-clean-plugin").getCoordinate()));
      adapter.setConfig(ConfigurationBuilder
              .create()
              .addConfigurationElement(
                      ConfigurationElementBuilder.create().setName("parent")
                              .addChild(ConfigurationElementBuilder.create().setName("child").setText("childText")))
              .addConfigurationElement(ConfigurationElementBuilder.create().setName("leaf").setText("leafText")));
      exec.setConfiguration(adapter.getConfiguration());
      executions = Arrays.asList(new PluginExecution[] { exec });
    }
  }

  @Dependent
  public static class InitHavingPlugin extends BaseTestAbstractProfilePluginFacet {
    private int initCalls;
    private Project initProject;

    public InitHavingPlugin() {
      pluginArtifact = DependencyArtifact.Clean;
      configurations = Collections.emptyList();
      executions = Collections.emptyList();
      dependencies = Collections.emptyList();
    }

    public Project getInitProject() {
      return initProject;
    }

    @Override
    protected void init() {
      initCalls++;
      initProject = getProject();
    }

    public int getInitCallCount() {
      return initCalls;
    }
  }

  @Test
  public void testEmptyPlugin() throws Exception {
    final Project project = initializeJavaProject();
    final DefinitionOnly facet = facetFactory.install(project, DefinitionOnly.class);

    checkPlugin(project, facet, AbstractBaseFacet.MAIN_PROFILE);
  }

  @Test
  public void testDependencyHavingPlugin() throws Exception {
    final Project project = initializeJavaProject();
    final DependencyHavingPlugin facet = facetFactory.install(project,
            DependencyHavingPlugin.class);

    checkPlugin(project, facet, AbstractBaseFacet.MAIN_PROFILE);
  }

  @Test
  public void testDependencyHavingPluginIsInstalled() throws Exception {
    final Project project = initializeJavaProject();
    final DependencyHavingPlugin testFacet = facetFactory.create(project,
            DependencyHavingPlugin.class);

    assertFalse(testFacet.isInstalled());

    final DependencyHavingPlugin facet = facetFactory.install(project,
            DependencyHavingPlugin.class);
    checkPlugin(project, facet, AbstractBaseFacet.MAIN_PROFILE);

    assertTrue(testFacet.isInstalled());
  }

  @Test
  public void testUninstall() throws Exception {
    final Project project = initializeJavaProject();
    final DependencyHavingPlugin facet = facetFactory.install(project,
            DependencyHavingPlugin.class);

    checkPlugin(project, facet, AbstractBaseFacet.MAIN_PROFILE);
    facet.uninstall();

    checkUninstalled(project, facet, AbstractBaseFacet.MAIN_PROFILE);
  }

  @Test
  public void testConfigHavingPlugin() throws Exception {
    final Project project = initializeJavaProject();
    final ConfigHavingPlugin facet = facetFactory.install(project, ConfigHavingPlugin.class);

    checkPlugin(project, facet, AbstractBaseFacet.MAIN_PROFILE);
  }

  @Test
  public void testConfigHavingPluginIsInstalled() throws Exception {
    final Project project = initializeJavaProject();
    final ConfigHavingPlugin testFacet = facetFactory.create(project, ConfigHavingPlugin.class);

    assertFalse(testFacet.isInstalled());

    final ConfigHavingPlugin facet = facetFactory.install(project, ConfigHavingPlugin.class);
    checkPlugin(project, facet, AbstractBaseFacet.MAIN_PROFILE);

    assertTrue(testFacet.isInstalled());
  }

  @Test
  public void testExecutionHavingPlugin() throws Exception {
    final Project project = initializeJavaProject();
    final ExecutionHavingPlugin facet = facetFactory.install(project,
            ExecutionHavingPlugin.class);

    checkPlugin(project, facet, AbstractBaseFacet.MAIN_PROFILE);
  }

  @Test
  public void testExecutionHavingPluginIsInstalled() throws Exception {
    final Project project = initializeJavaProject();
    final ExecutionHavingPlugin testFacet = facetFactory.create(project,
            ExecutionHavingPlugin.class);

    assertFalse(testFacet.isInstalled());

    final ExecutionHavingPlugin facet = facetFactory.install(project,
            ExecutionHavingPlugin.class);
    checkPlugin(project, facet, AbstractBaseFacet.MAIN_PROFILE);

    assertTrue(testFacet.isInstalled());
  }

  @Test
  public void initShouldBeCalledExactlyOnce() throws Exception {
    final Project project = initializeJavaProject();

    final InitHavingPlugin facet = facetFactory.create(project, InitHavingPlugin.class);

    assertEquals(0 , facet.getInitCallCount());

    facetFactory.install(project, facet);
    assertTrue(facet.isInstalled());
    assertEquals(1 , facet.getInitCallCount());

    facet.uninstall();
    assertFalse(facet.isInstalled());
    assertEquals(1 , facet.getInitCallCount());
  }

  protected void checkPlugin(Project project, AbstractProfilePluginFacet facet, String profileId) {
    final MavenFacet coreFacet = project.getFacet(MavenFacet.class);
    Profile profile = null;
    for (final Profile prof : coreFacet.getModel().getProfiles()) {
      if (profileId.equals(prof.getId())) {
        profile = prof;
        break;
      }
    }
    Assert.assertNotNull("Could not find profile with matching id, " + profileId, profile);
    final BuildBase build = profile.getBuild();
    Assert.assertNotNull("No build for profile " + profileId, build);
    final Plugin plugin = build.getPluginsAsMap().get(facet.getPluginArtifact().toString());
    final MavenPluginAdapter adapter = new MavenPluginAdapter(plugin);

    // This is hack to go from maven to forge configurations
    final MavenPluginAdapter configAdapter = new MavenPluginAdapter(MavenPluginBuilder.create().setCoordinate(
            DependencyBuilder.create("maven-clean-plugin").getCoordinate()));

    final Collection<Execution> executions = new ArrayList<Execution>();
    for (final PluginExecution plugExec : facet.getPluginExecutions()) {
      configAdapter.setConfiguration(plugExec.getConfiguration());
      ExecutionBuilder newExec = ExecutionBuilder.create().setId(plugExec.getId()).setPhase(plugExec.getPhase())
              .setConfig(configAdapter.getConfig());
      for (final String goal : plugExec.getGoals())
        newExec.addGoal(goal);
      executions.add(newExec);
    }
    checkExecutions(adapter, executions);
    checkDependencies(build, facet.getDependencies(), plugin.getDependencies(), facet.getPluginArtifact().toString());
    checkConfigurations(adapter.getConfig(), facet.getConfigurations());
  }

  private void checkUninstalled(Project project, AbstractProfilePluginFacet facet, String profileId) {
    final MavenFacet coreFacet = project.getFacet(MavenFacet.class);
    Profile profile = null;
    for (final Profile prof : coreFacet.getModel().getProfiles()) {
      if (profileId.equals(prof.getId())) {
        profile = prof;
        break;
      }
    }
    Assert.assertNotNull("Could not find profile with matching id, " + profileId, profile);
    final BuildBase build = profile.getBuild();
    Assert.assertNotNull("No build for profile " + profileId, build);

    final Plugin plugin = build.getPluginsAsMap().get(facet.getPluginArtifact().toString());
    assertNull("Plugin was not uninstalled.", plugin);
  }

}
