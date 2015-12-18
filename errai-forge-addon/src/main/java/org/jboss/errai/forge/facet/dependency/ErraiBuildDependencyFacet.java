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
import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
import org.jboss.errai.forge.facet.base.CoreBuildFacet;
import org.jboss.errai.forge.facet.base.DependencyManagementFacet;
import org.jboss.errai.forge.util.MavenModelUtil;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.maven.projects.MavenFacet;

import static org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact.*;

/**
 * This facet sets all the common Maven dependencies required to build or run in
 * development mode an application with Errai.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
@FacetConstraint({ CoreBuildFacet.class, DependencyManagementFacet.class })
public class ErraiBuildDependencyFacet extends AbstractDependencyFacet {

  public ErraiBuildDependencyFacet() {
    setCoreDependencies(DependencyBuilder.create(ErraiTools.toString()), DependencyBuilder.create(GwtUser.toString())
            .setScopeType("provided"), DependencyBuilder.create(ErraiJboss.toString()), DependencyBuilder
            .create(JUnit.toString()).setScopeType("test"),
            DependencyBuilder.create(JbossSupport.toString()));
  }

  @Override
  public boolean install() {
    if (super.install()) {
      // Set main profile to be active by default
      final MavenFacet coreFacet = getProject().getFacet(MavenFacet.class);
      final Model pom = coreFacet.getModel();
      Profile profile = MavenModelUtil.getProfileById(MAIN_PROFILE, pom.getProfiles());
      if (profile == null) {
        profile = new Profile();
        profile.setId(MAIN_PROFILE);
        pom.addProfile(profile);
      }
      if (profile.getActivation() == null)
        profile.setActivation(new Activation());
      profile.getActivation().setActiveByDefault(true);
      coreFacet.setModel(pom);

      return true;
    }
    else {
      return false;
    }
  }
}
