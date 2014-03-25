package org.jboss.errai.forge.facet.plugin;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import javax.enterprise.context.Dependent;

import org.apache.maven.model.Build;
import org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.maven.plugins.ConfigurationBuilder;
import org.jboss.forge.addon.maven.plugins.ConfigurationElement;
import org.jboss.forge.addon.maven.plugins.ConfigurationElementBuilder;
import org.jboss.forge.addon.maven.plugins.Execution;
import org.jboss.forge.addon.maven.plugins.ExecutionBuilder;
import org.jboss.forge.addon.maven.projects.MavenFacet;
import org.jboss.forge.addon.maven.projects.MavenPluginFacet;
import org.jboss.forge.addon.projects.Project;
import org.junit.Test;

public class AbstractPluginFacetTest extends BasePluginFacetTest {

  @Dependent
  public static class DefinitionOnly extends AbstractPluginFacet {
    public DefinitionOnly() {
      pluginArtifact = DependencyArtifact.Clean;
      configurations = Collections.emptyList();
      dependencies = Collections.emptyList();
      executions = Collections.emptyList();
    }
  }

  @Dependent
  public static class DependencyHavingPlugin extends AbstractPluginFacet {
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
  public static class ConfigHavingPlugin extends AbstractPluginFacet {
    public ConfigHavingPlugin() {
      pluginArtifact = DependencyArtifact.Clean;
      configurations = Arrays.asList(new ConfigurationElement[] {
          ConfigurationElementBuilder.create().setName("configName").setText("configText"),
          ConfigurationElementBuilder.create().setName("parent")
                  .addChild(ConfigurationElementBuilder.create().setName("child").setText("childText"))
                  .addChild(ConfigurationElementBuilder.create().setName("child").setText("otherChildText")) });
      executions = Collections.emptyList();
      dependencies = Collections.emptyList();
    }
  }

  @Dependent
  public static class ExecutionHavingPlugin extends AbstractPluginFacet {
    public ExecutionHavingPlugin() {
      pluginArtifact = DependencyArtifact.Clean;
      configurations = Collections.emptyList();
      executions = Arrays.asList(new Execution[] { ExecutionBuilder
              .create()
              .setId("testExec")
              .setPhase("compile")
              .setConfig(
                      ConfigurationBuilder
                              .create()
                              .addConfigurationElement(
                                      ConfigurationElementBuilder
                                              .create()
                                              .setName("parent")
                                              .addChild(
                                                      ConfigurationElementBuilder.create().setName("child")
                                                              .setText("childText")))
                              .addConfigurationElement(
                                      ConfigurationElementBuilder.create().setName("leaf").setText("leafText"))) });
      dependencies = Collections.emptyList();
    }
  }

  @Test
  public void testEmptyPluginDefinition() throws Exception {
    final Project project = initializeJavaProject();
    final DefinitionOnly facet = facetFactory.install(project, DefinitionOnly.class);

    checkPlugin(project, facet);
  }

  @Test
  public void testWithDependencies() throws Exception {
    final Project project = initializeJavaProject();
    final DependencyHavingPlugin facet = facetFactory.install(project,
            DependencyHavingPlugin.class);

    checkPlugin(project, facet);
  }

  @Test
  public void testIsInstalledWithDependencies() throws Exception {
    final Project project = initializeJavaProject();
    final DependencyHavingPlugin testFacet = facetFactory.create(project,
            DependencyHavingPlugin.class);

    assertFalse(testFacet.isInstalled());

    final DependencyHavingPlugin facet = facetFactory.install(project,
            DependencyHavingPlugin.class);
    // Precondition
    checkPlugin(project, facet);

    // Actual test
    assertTrue(testFacet.isInstalled());
  }

  @Test
  public void testWithConfigurations() throws Exception {
    final Project project = initializeJavaProject();
    final ConfigHavingPlugin facet = facetFactory.install(project,
            ConfigHavingPlugin.class);

    checkPlugin(project, facet);
  }

  @Test
  public void testIsInstalledWithConfigurations() throws Exception {
    final Project project = initializeJavaProject();
    final ConfigHavingPlugin testFacet = facetFactory.create(project,
            ConfigHavingPlugin.class);

    assertFalse(testFacet.isInstalled());

    final ConfigHavingPlugin facet = facetFactory.install(project,
            ConfigHavingPlugin.class);
    // Precondition
    checkPlugin(project, facet);

    assertTrue(testFacet.isInstalled());
  }

  @Test
  public void testWithExecutions() throws Exception {
    final Project project = initializeJavaProject();
    final ExecutionHavingPlugin facet = facetFactory.install(project,
            ExecutionHavingPlugin.class);

    checkPlugin(project, facet);
  }

  @Test
  public void testIsInstalledWithExecutions() throws Exception {
    final Project project = initializeJavaProject();
    final ExecutionHavingPlugin testFacet = facetFactory.create(project, ExecutionHavingPlugin.class);

    assertFalse(testFacet.isInstalled());

    final ExecutionHavingPlugin facet = facetFactory.install(project, ExecutionHavingPlugin.class);
    // Precondition
    checkPlugin(project, facet);

    assertTrue(testFacet.isInstalled());
  }

  @Test
  public void testUninstall() throws Exception {
    final Project project = initializeJavaProject();
    final ExecutionHavingPlugin facet = facetFactory.install(project,
            ExecutionHavingPlugin.class);

    // Precondition
    assertTrue(facet.isInstalled());

    facet.uninstall();

    final MavenPluginFacet pluginFacet = project.getFacet(MavenPluginFacet.class);
    assertFalse(pluginFacet.hasPlugin(DependencyBuilder.create(facet.getPluginArtifact().toString()).getCoordinate()));
  }

  private void checkPlugin(Project project, AbstractPluginFacet facet) {
    final String artifactDef = facet.getPluginArtifact().toString();
    final MavenPluginFacet pluginFacet = project.getFacet(MavenPluginFacet.class);
    final MavenFacet coreFacet = project.getFacet(MavenFacet.class);
    checkHasPlugin(project, facet, artifactDef);
    checkExecutions(pluginFacet.getPlugin(DependencyBuilder.create(artifactDef).getCoordinate()), facet.getExecutions());
    Build build = coreFacet.getModel().getBuild();
    if (build == null)
      build = new Build();
    checkDependencies(build, facet.getDependencies(), build.getPluginsAsMap().get(facet.getPluginArtifact().toString())
            .getDependencies(), facet.getPluginArtifact().toString());
    checkConfigurations(pluginFacet.getPlugin(DependencyBuilder.create(artifactDef).getCoordinate()).getConfig(),
            facet.getConfigurations());
  }

}
