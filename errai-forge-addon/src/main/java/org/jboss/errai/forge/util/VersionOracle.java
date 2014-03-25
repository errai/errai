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
