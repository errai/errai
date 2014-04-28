/**
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.forge.util;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.maven.project.artifact.PluginArtifact;
import org.jboss.errai.forge.constant.ArtifactVault;
import org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact;
import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.forge.addon.dependencies.Dependency;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.projects.facets.DependencyFacet;

/**
 * Provides versions for Maven dependencies.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class VersionOracle {

  private DependencyFacet depFacet;
  private static final Map<String, String> versionMap = new ConcurrentHashMap<String, String>();

  static {
    // Plugin versions
    versionMap.put(DependencyArtifact.Clean.toString(), "2.4.1");
    versionMap.put(DependencyArtifact.Compiler.toString(), "2.3.2");
    versionMap.put(DependencyArtifact.Dependency.toString(), "2.8");
    versionMap.put(DependencyArtifact.War.toString(), "2.2");
    versionMap.put(DependencyArtifact.JbossPlugin.toString(), "7.1.1.Final");
    versionMap.put(DependencyArtifact.GwtPlugin.toString(), "2.5.1");
    versionMap.put(DependencyArtifact.Surefire.toString(), "2.2");
    
    // Dependencies for test generation
    versionMap.put(DependencyArtifact.GwtMockito.toString(), "1.1.3");
  }
  
  public VersionOracle(DependencyFacet facet) {
    depFacet = facet;
  }

  /**
   * Get a version for the given dependency.
   */
  public String resolveVersion(DependencyArtifact dependency) {
    return resolveVersion(dependency.getGroupId(), dependency.getArtifactId());
  }

  /**
   * Get a version for the given dependency.
   */
  public String resolveVersion(PluginArtifact plugin) {
    return resolveVersion(plugin.getGroupId(), plugin.getArtifactId());
  }

  /**
   * Get a version for the given dependency.
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
    return versionMap.get(groupId + ":" + artifactId);
  }

  /**
   * @return The most recent non-snapshot version of Errai.
   */
  public String resolveErraiVersion() {
    DependencyArtifact common = DependencyArtifact.ErraiCommon;
    return getHighestStableVersion(common.getGroupId(), common.getArtifactId());
  }

  public boolean isManaged(DependencyBuilder dep) {
    return depFacet.hasEffectiveManagedDependency(dep);
  }

}
