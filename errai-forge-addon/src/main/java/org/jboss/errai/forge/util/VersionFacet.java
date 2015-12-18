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

package org.jboss.errai.forge.util;

import org.apache.maven.project.artifact.PluginArtifact;
import org.jboss.errai.forge.config.ProjectConfig;
import org.jboss.errai.forge.config.ProjectProperty;
import org.jboss.errai.forge.constant.ArtifactVault;
import org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact;
import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.forge.addon.dependencies.Dependency;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.facets.AbstractFacet;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFacet;
import org.jboss.forge.addon.projects.facets.DependencyFacet;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides versions for Maven dependencies.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@FacetConstraint({ DependencyFacet.class, ProjectConfig.class })
public class VersionFacet extends AbstractFacet<Project> implements ProjectFacet {

  private static final Map<String, VersionRule> versionMap = new ConcurrentHashMap<String, VersionRule>();

  static {
    // Plugin versions
    versionMap.put(DependencyArtifact.Clean.toString(), new ConstantVersion("2.4.1"));
    versionMap.put(DependencyArtifact.Compiler.toString(), new ConstantVersion("2.3.2"));
    versionMap.put(DependencyArtifact.Dependency.toString(), new ConstantVersion("2.8"));
    versionMap.put(DependencyArtifact.War.toString(), new ConstantVersion("2.2"));
    versionMap.put(DependencyArtifact.WildflyPlugin.toString(), new ConstantVersion("1.0.2.Final"));
    versionMap.put(DependencyArtifact.Surefire.toString(), new ConstantVersion("2.2"));
    versionMap.put(DependencyArtifact.EclipseMavenPlugin.toString(), new ConstantVersion("1.0.0"));

    // Dependencies for test generation
    versionMap.put(DependencyArtifact.GwtMockito.toString(), new ConstantVersion("1.1.3"));

    // App server version for dev mode
    versionMap.put(DependencyArtifact.WildflyDist.toString(), new ConstantVersion("8.1.0.Final"));

    // GWT plugin version depends on Errai version
    versionMap.put(DependencyArtifact.GwtPlugin.toString(), new VersionRule() {

      @Override
      public String getVersion(final Project project) {
        final ProjectConfig projectConfig = project.getFacet(ProjectConfig.class);
        final String erraiVersion = projectConfig.getProjectProperty(ProjectProperty.ERRAI_VERSION, String.class);

        /*
         * FIXME This should try and detect what managed version of gwt-user is being used. Currently there's no
         * guarantee that the dependencyManagement or gwt-user have been added when this is called.
         */
        if (erraiVersion.startsWith("3.0")) {
          return "2.5.1";
        }
        else if (erraiVersion.startsWith("3.1.0")) {
          return "2.6.1";
        }
        // Addon only supports 3.x, so this handles versions greater than 3.1.0
        else {
          return "2.7.0";
        }
      }
    });
  }

  /**
   * @see #resolveVersion(String, String)
   */
  public String resolveVersion(DependencyArtifact dependency) {
    return resolveVersion(dependency.getGroupId(), dependency.getArtifactId());
  }

  /**
   * @see #resolveVersion(String, String)
   */
  public String resolveVersion(PluginArtifact plugin) {
    return resolveVersion(plugin.getGroupId(), plugin.getArtifactId());
  }

  /**
   * Get a version for the given dependency. The following steps are performed in order to resolve the version:
   * <ul>
   * <li>If there is a static version mapped for the dependency, it is used.
   * <li>If the {@code}groupId{@code} begins with {@code}org.jboss.errai{@code} the project's Errai version is used.
   * <li>Otherwise the highest non-snapshot version is used.
   */
  public String resolveVersion(String groupId, String artifactId) {
    String staticVersion = getStaticVersion(groupId, artifactId);
    if (staticVersion != null)
      return staticVersion;
    else if (groupId.startsWith(ArtifactVault.ERRAI_GROUP_ID))
      return resolveErraiVersion();
    else
      return getHighestStableVersion(groupId, artifactId);
  }

  private String getHighestStableVersion(String groupId, String artifactId) {
    final Dependency dep = DependencyBuilder.create(groupId + ":" + artifactId);
    final DependencyFacet depFacet = getFaceted().getFacet(DependencyFacet.class);
    final List<Coordinate> availVersions = depFacet.resolveAvailableVersions(dep);

    String maxVersion = null;
    for (final Coordinate versionCoord : availVersions) {
      // FIXME needs a more reliable way of comparing versions
      if (!versionCoord.isSnapshot() && (maxVersion == null || versionCoord.getVersion().compareTo(maxVersion) > 0)) {
        maxVersion = versionCoord.getVersion();
      }
    }

    return maxVersion;
  }

  private String getStaticVersion(String groupId, String artifactId) {
    final VersionRule versionRule = versionMap.get(groupId + ":" + artifactId);

    return (versionRule != null) ? versionRule.getVersion(getFaceted()) : null;
  }

  /**
   * @return The most recent non-snapshot version of Errai.
   */
  public String resolveErraiVersion() {
    DependencyArtifact common = DependencyArtifact.ErraiCommon;
    return getHighestStableVersion(common.getGroupId(), common.getArtifactId());
  }

  public boolean isManaged(DependencyBuilder dep) {
    return getFaceted().getFacet(DependencyFacet.class).hasEffectiveManagedDependency(dep);
  }

  private static interface VersionRule {
    String getVersion(Project project);
  }

  private static class ConstantVersion implements VersionRule {

    private final String version;

    ConstantVersion(final String version) {
      this.version = version;
    }

    @Override
    public String getVersion(Project project) {
      return version;
    }

  }

  @Override
  public boolean install() {
    return true;
  }

  @Override
  public boolean isInstalled() {
    return true;
  }

}
