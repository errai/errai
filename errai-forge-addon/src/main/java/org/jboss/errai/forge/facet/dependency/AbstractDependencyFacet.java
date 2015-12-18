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

package org.jboss.errai.forge.facet.dependency;

import org.apache.maven.model.Activation;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
import org.jboss.errai.forge.constant.ArtifactVault;
import org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact;
import org.jboss.errai.forge.constant.PomPropertyVault.Property;
import org.jboss.errai.forge.facet.base.AbstractBaseFacet;
import org.jboss.errai.forge.util.MavenConverter;
import org.jboss.errai.forge.util.MavenModelUtil;
import org.jboss.errai.forge.util.VersionFacet;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.maven.projects.MavenFacet;
import org.jboss.forge.addon.projects.facets.DependencyFacet;

import java.util.*;
import java.util.Map.Entry;

/**
 * A base class for all facets that install Maven dependencies. Concrete
 * subclasses must provide values for
 * {@link AbstractDependencyFacet#coreDependencies} and
 * {@link AbstractDependencyFacet#profileDependencies}.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public abstract class AbstractDependencyFacet extends AbstractBaseFacet {

  /**
   * Dependencies to be added to the build in the Maven pom file. Versions of
   * these dependencies will be assigned from a {@link VersionFacet} if
   * unspecified.
   */
  protected Collection<DependencyBuilder> coreDependencies;
  /**
   * Dependencies to be added to the build of Maven profiles with names matching
   * the keys of this map. Versions of these dependencies will be assigned from
   * a {@link VersionFacet} if unspecified. Profiles that do not already exist
   * will be created.
   */
  protected Map<String, Collection<DependencyBuilder>> profileDependencies = new HashMap<String, Collection<DependencyBuilder>>();

  @Override
  public boolean install() {
    final DependencyFacet depFacet = getProject().getFacet(DependencyFacet.class);
    final MavenFacet coreFacet = getProject().getFacet(MavenFacet.class);
    final VersionFacet versionFacet = getProject().getFacet(VersionFacet.class);

    for (DependencyBuilder dep : coreDependencies) {
      depFacet.addDirectDependency(getDependencyWithVersion(dep, versionFacet));
    }

    for (Entry<String, Collection<DependencyBuilder>> entry : profileDependencies.entrySet()) {
      addDependenciesToProfile(entry.getKey(), entry.getValue(), versionFacet);
    }

    final Model pom = coreFacet.getModel();
    final Map<String, Collection<DependencyBuilder>> blacklistProfileDependencies = new HashMap<String, Collection<DependencyBuilder>>();
    for (String profileId : ArtifactVault.getBlacklistProfiles()) {
      final Profile profile = MavenModelUtil.getProfileById(profileId, pom.getProfiles());
      for (final DependencyArtifact artifact : ArtifactVault.getBlacklistedArtifacts(profileId)) {
        final DependencyBuilder dep = getDependency(artifact);
        if (depFacet.hasEffectiveDependency(dep)
                && !hasProvidedDependency(profile, dep)) {
          final org.jboss.forge.addon.dependencies.Dependency existing = depFacet.getEffectiveDependency(dep);
          if (!versionFacet.isManaged(dep))
            dep.setVersion(existing.getCoordinate().getVersion());
          dep.setScopeType("provided");
          if (!blacklistProfileDependencies.containsKey(profileId))
            blacklistProfileDependencies.put(profileId, new ArrayList<DependencyBuilder>());
          blacklistProfileDependencies.get(profileId).add(dep);
        }
      }
    }

    for (Entry<String, Collection<DependencyBuilder>> entry : blacklistProfileDependencies.entrySet()) {
      addDependenciesToProfile(entry.getKey(), entry.getValue(), versionFacet);
    }

    return true;
  }

  @Override
  public boolean uninstall() {
    final DependencyFacet depFacet = getProject().getFacet(DependencyFacet.class);
    final MavenFacet coreFacet = getProject().getFacet(MavenFacet.class);

    removeCoreDependencies(depFacet);

    // This line must come AFTER removeCoreDependencies is called
    final Model pom = coreFacet.getModel();

    // Set blacklist profiles to inactive for dependency resolution.
    final Map<String, Activation> profileActivationMap = setBlacklistProfilesToInactive(pom);
    coreFacet.setModel(pom);
    
    final Map<String, Collection<DependencyBuilder>> removableProfileDependencies = new HashMap<String, Collection<DependencyBuilder>>();

    removableProfileDependencies.putAll(getUneededBlacklistedDependencies(depFacet, pom));
    removableProfileDependencies.putAll(profileDependencies);

    removeProfileDependencies(pom, removableProfileDependencies);
    
    resetProfileActivations(pom, profileActivationMap);
    coreFacet.setModel(pom);

    return true;
  }

  private void resetProfileActivations(final Model pom, final Map<String, Activation> profileActivationMap) {
    for (final Profile profile : pom.getProfiles()) {
      if (profileActivationMap.containsKey(profile.getId())) {
        profile.setActivation(profileActivationMap.get(profile.getId()));
      }
    }
  }

  private Map<String, Collection<DependencyBuilder>> getUneededBlacklistedDependencies(final DependencyFacet depFacet, final Model pom) {
    final Map<String, Collection<DependencyBuilder>> retVal = new HashMap<String, Collection<DependencyBuilder>>();

    for (final Profile profile : pom.getProfiles()) {
      for (final DependencyArtifact artifact : ArtifactVault.getBlacklistedArtifacts(profile.getId())) {
        final DependencyBuilder dep = getDependency(artifact);
        if (!depFacet.hasEffectiveDependency(dep)) {
          if (!retVal.containsKey(profile.getId()))
            retVal.put(profile.getId(), new ArrayList<DependencyBuilder>());
          retVal.get(profile.getId()).add(dep.setScopeType("provided"));
        }
      }
    }
    
    return retVal;
  }
  
  /**
   * @return A map of the profile activations that were replaced.
   */
  private Map<String, Activation> setBlacklistProfilesToInactive(final Model pom) {
    final Map<String, Activation> profileActivationMap = new HashMap<String, Activation>(pom.getProfiles().size());
    final Activation inactive = new Activation();
    inactive.setActiveByDefault(false);

    for (final Profile profile : pom.getProfiles()) {
      if (ArtifactVault.getBlacklistProfiles().contains(profile.getId())) {
        profileActivationMap.put(profile.getId(), profile.getActivation());
        profile.setActivation(inactive);
      }
    }
    
    return profileActivationMap;
  }

  private void removeProfileDependencies(final Model pom,
          final Map<String, Collection<DependencyBuilder>> removableProfileDependencies) {
    for (Profile profile : pom.getProfiles()) {
      if (removableProfileDependencies.containsKey(profile.getId())) {
        for (DependencyBuilder dep : removableProfileDependencies.get(profile.getId())) {
          List<Dependency> profDeps = profile.getDependencies();
          for (int i = 0; i < profDeps.size(); i++) {
            if (MavenConverter.areSameArtifact(profDeps.get(i), dep)) {
              profDeps.remove(i);
              break;
            }
          }
        }
      }
    }
  }

  private void removeCoreDependencies(final DependencyFacet depFacet) {
    for (DependencyBuilder dep : coreDependencies) {
      if (depFacet.hasDirectDependency(dep)) {
        depFacet.removeDependency(dep);
      }
    }
  }

  @Override
  public boolean isInstalled() {
    final DependencyFacet depFacet = getProject().getFacet(DependencyFacet.class);
    final VersionFacet versionFacet = getProject().getFacet(VersionFacet.class);
    for (final DependencyBuilder dep : coreDependencies) {
      if (!depFacet.hasDirectDependency(getDependencyWithVersion(dep, versionFacet))) {
        return false;
      }
    }

    final MavenFacet coreFacet = getProject().getFacet(MavenFacet.class);
    Model pom = coreFacet.getModel();
    for (final String profName : profileDependencies.keySet()) {
      final Profile profile = MavenModelUtil.getProfileById(profName, pom.getProfiles());
      if (profile == null) {
        return false;
      }
      outer: for (final DependencyBuilder dep : profileDependencies.get(profName)) {
        for (final Dependency profDep : profile.getDependencies()) {
          if (MavenConverter.areSameArtifact(profDep, dep)) {
            continue outer;
          }
        }
        return false;
      }
    }

    /*
     * Check that all blacklisted dependencies that are transitively in the
     * project have been provided scoped. Note that we are not checking if these
     * blacklisted dependencies were pulled in transitively through a dependency
     * in this instance. This should not be an issue, since we don't really care
     * what sets the blacklisted dependencies to provided, as long as something
     * does it.
     */
    pom = coreFacet.getModel();
    for (final String profileId : ArtifactVault.getBlacklistProfiles()) {
      final Profile profile = MavenModelUtil.getProfileById(profileId, pom.getProfiles());
      for (final DependencyArtifact artifact : ArtifactVault.getBlacklistedArtifacts(profileId)) {
        final DependencyBuilder dep = getDependency(artifact);
        if (depFacet.hasEffectiveDependency(dep) && !hasProvidedDependency(profile, dep))
          return false;
      }
    }

    return true;
  }

  private DependencyBuilder getDependencyWithVersion(final DependencyBuilder dep, final VersionFacet versionFacet) {
    if (!versionFacet.isManaged(dep)) {
      if (dep.getGroupId().equals(ArtifactVault.ERRAI_GROUP_ID))
        dep.setVersion(Property.ErraiVersion.invoke());
      else
        dep.setVersion(versionFacet.resolveVersion(dep.getGroupId(), dep.getCoordinate().getArtifactId()));
    }

    return dep;
  }

  /**
   * A convenience method for setting
   * {@link AbstractDependencyFacet#coreDependencies}.
   * 
   * @param deps
   *          Dependencies to be put in a {@link Collection} and assigned to
   *          {@link AbstractDependencyFacet#coreDependencies}.
   */
  protected void setCoreDependencies(final DependencyBuilder... deps) {
    coreDependencies = Arrays.asList(deps);
  }

  /**
   * A convenience method for setting a key-value pair in
   * {@link AbstractDependencyFacet#profileDependencies}.
   * 
   * @param name
   *          The name of a Maven profile. If no profile with this name exists,
   *          one will be added to the pom file.
   * @param deps
   *          Dependencies to be put in a {@link Collection} and added to
   *          {@link AbstractDependencyFacet#profileDependencies}.
   */
  protected void setProfileDependencies(final String name, final DependencyBuilder... deps) {
    final List<DependencyBuilder> list = new ArrayList<DependencyBuilder>(deps.length);
    for (int i = 0; i < deps.length; i++) {
      list.add(deps[i]);
    }
    profileDependencies.put(name, list);
  }

}
