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
package org.jboss.errai.forge.facet.base;

import java.util.ArrayList;
import java.util.Collection;

import org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact;
import org.jboss.errai.forge.constant.PomPropertyVault.Property;
import org.jboss.errai.forge.util.VersionFacet;
import org.jboss.errai.forge.util.VersionOracle;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.projects.facets.DependencyFacet;

@FacetConstraint({ CoreBuildFacet.class, VersionFacet.class })
public class DependencyManagementFacet extends AbstractBaseFacet {

  protected Collection<DependencyBuilder> dependencies = new ArrayList<DependencyBuilder>();

  public DependencyManagementFacet() {
    dependencies.add(DependencyBuilder.create(DependencyArtifact.ErraiBom.toString())
            .setVersion(Property.ErraiVersion.invoke()).setScopeType("import").setPackaging("pom"));
    dependencies.add(DependencyBuilder.create(DependencyArtifact.ErraiVersionMaster.toString())
            .setVersion(Property.ErraiVersion.invoke()).setScopeType("import").setPackaging("pom"));
    dependencies.add(DependencyBuilder.create(DependencyArtifact.ErraiParent.toString())
            .setVersion(Property.ErraiVersion.invoke()).setScopeType("import").setPackaging("pom"));
  }

  @Override
  public boolean install() {
    final DependencyFacet depFacet = getProject().getFacet(DependencyFacet.class);
    final VersionOracle oracle = new VersionOracle(depFacet);

    for (final DependencyBuilder dep : dependencies) {
      if (dep.getCoordinate().getVersion() == null || dep.getCoordinate().getVersion().equals("")) {
        dep.setVersion(oracle.resolveVersion(dep.getGroupId(), dep.getCoordinate().getArtifactId()));
      }
      if (!depFacet.hasDirectManagedDependency(dep)) {
        depFacet.addDirectManagedDependency(dep);
      }
    }

    return true;
  }

  @Override
  public boolean uninstall() {
    final DependencyFacet depFacet = getProject().getFacet(DependencyFacet.class);

    for (final DependencyBuilder dep : dependencies) {
      if (depFacet.hasDirectManagedDependency(dep)) {
        depFacet.removeManagedDependency(dep);
      }
    }

    return true;
  }

  @Override
  public boolean isInstalled() {
    final DependencyFacet depFacet = getProject().getFacet(DependencyFacet.class);

    for (final DependencyBuilder dep : dependencies) {
      if (!depFacet.hasDirectManagedDependency(dep)) {
        return false;
      }
    }
    return true;
  }

}
