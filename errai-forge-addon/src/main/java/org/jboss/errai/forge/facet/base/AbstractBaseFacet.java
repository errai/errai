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

package org.jboss.errai.forge.facet.base;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
import org.jboss.errai.forge.constant.ArtifactVault;
import org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact;
import org.jboss.errai.forge.constant.PomPropertyVault.Property;
import org.jboss.errai.forge.util.MavenConverter;
import org.jboss.errai.forge.util.MavenModelUtil;
import org.jboss.errai.forge.util.VersionFacet;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.facets.AbstractFacet;
import org.jboss.forge.addon.maven.projects.MavenFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFacet;

import java.util.Collection;

/**
 * A base class for Errai-related facets providing some basic routines.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public abstract class AbstractBaseFacet extends AbstractFacet<Project> implements ProjectFacet {

  /**
   * The name of the primary profile used to configure an Errai project.
   */
  public static final String MAIN_PROFILE = "jboss7";
  
  protected Project getProject() {
    return Project.class.cast(getFaceted());
  }

  /**
   * Add dependencies to a Maven profile.
   * 
   * @param name
   *          The name of the Maven profile to which dependencies will be added.
   *          If no profile with this name exists, one will be created.
   * @param deps
   *          Dependencies to be added. Note that the versions of these
   *          dependencies will be ignored, and instead provided by the
   *          {@link VersionFacet}.
   * @param versionFacet
   *          Used to determine the version of dependencies.
   * @return True iff the dependencies were successfully added.
   */
  protected boolean addDependenciesToProfile(final String name, final Collection<DependencyBuilder> deps,
          final VersionFacet versionFacet) {
    final MavenFacet coreFacet = getProject().getFacet(MavenFacet.class);
    final Model pom = coreFacet.getModel();

    Profile profile = MavenModelUtil.getProfileById(name, pom.getProfiles());

    if (profile == null) {
      profile = new Profile();
      profile.setId(name);
      pom.addProfile(profile);
    }

    for (DependencyBuilder dep : deps) {
      if (!hasDependency(profile, dep)) {
        if (!versionFacet.isManaged(dep)) {
          if (dep.getCoordinate().getVersion() == null || dep.getCoordinate().getVersion().equals("")) {
            if (dep.getGroupId().equals(ArtifactVault.ERRAI_GROUP_ID))
              dep.setVersion(Property.ErraiVersion.invoke());
            else
              dep.setVersion(versionFacet.resolveVersion(dep.getGroupId(), dep.getCoordinate().getArtifactId()));
          }
        }
        profile.addDependency(MavenConverter.convert(dep));
      }
    }

    coreFacet.setModel(pom);

    return true;
  }

  /**
   * Returns true iff the given profile as the given dependency (with provided
   * scope).
   */
  protected boolean hasProvidedDependency(final Profile profile, final DependencyBuilder dep) {
    final Dependency profDep = getDependency(profile, dep);

    return profDep != null && profDep.getScope() != null && profDep.getScope().equalsIgnoreCase("provided");
  }

  /**
   * Returns true iff the given profile as the given dependency.
   */
  protected boolean hasDependency(final Profile profile, final DependencyBuilder dep) {
    final Dependency profDep = getDependency(profile, dep);

    return profDep != null;
  }

  /**
   * Get a dependency if it exists in the given profile, or null.
   */
  protected Dependency getDependency(final Profile profile, final DependencyBuilder dep) {
    if (profile == null)
      return null;

    for (final Dependency profDep : profile.getDependencies()) {
      if (MavenConverter.areSameArtifact(profDep, dep))
        return profDep;
    }

    return null;
  }
  
  protected DependencyBuilder getDependency(final DependencyArtifact artifact) {
    final DependencyBuilder dep = DependencyBuilder.create(artifact.toString());
    if (artifact.getClassifier() != null)
      dep.setClassifier(artifact.getClassifier());
    
    return dep;
  }
  
  protected void error(final String message, final Throwable throwable) {
    // TODO implement
  }
  
  protected void warning(final String message, final Throwable throwable) {
    // TODO implement
  }
}
