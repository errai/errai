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

package org.jboss.errai.forge.facet.aggregate;

import org.jboss.errai.forge.config.ProjectConfig;
import org.jboss.errai.forge.config.ProjectProperty;
import org.jboss.errai.forge.facet.base.CoreBuildFacet;
import org.jboss.errai.forge.facet.dependency.ErraiBuildDependencyFacet;
import org.jboss.errai.forge.facet.module.ModuleCoreFacet;
import org.jboss.errai.forge.facet.plugin.*;
import org.jboss.errai.forge.facet.resource.ErraiAppPropertiesFacet;
import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.projects.ProjectFacet;

import javax.inject.Inject;

/**
 * Aggregates core facets required by all other facet aggregators. Installing
 * this facet will add all the necessary dependencies, profile, and plugin
 * configurations to run a GWT/Errai project in development mode or compile to
 * production mode.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
@FacetConstraint({ ErraiBuildDependencyFacet.class, ProjectConfig.class, CoreBuildFacet.class })
public class CoreFacet extends BaseAggregatorFacet {

  @Inject
  private FacetFactory facetFactory;

  /*
   * These core facets cannot be FacetConstraints because we want users to be
   * able to modify maven plugin configurations after running errai-setup
   * (ERRAI-715).
   */
  @SuppressWarnings({ "unchecked" })
  public static final Class<? extends ProjectFacet>[] coreFacets = new Class[] { 
      CleanPluginFacet.class, CompilerPluginFacet.class, DependencyPluginFacet.class, GwtPluginFacet.class,
      WildflyPluginFacet.class, WarPluginFacet.class, ModuleCoreFacet.class, ErraiAppPropertiesFacet.class };

  @Override
  public String getFeatureName() {
    return "Errai Build Setup";
  }

  @Override
  public String getFeatureDescription() {
    return "The core build setup required for running development mode or compiling for deployment.";
  }

  @Override
  public String getShortName() {
    return "core";
  }

  @Override
  public boolean install() {
    boolean installWasSuccessful = true;

    for (int i = 0; i < coreFacets.length && installWasSuccessful; i++) {
      try {
        facetFactory.install(getProject(), coreFacets[i]);
      }
      catch (IllegalStateException e) {
        installWasSuccessful = false;
      }
    }

    if (installWasSuccessful) {
      getProject().getFacet(ProjectConfig.class).setProjectProperty(ProjectProperty.CORE_IS_INSTALLED, true);
    }

    return installWasSuccessful;
  }

  @Override
  public boolean isInstalled() {
    if (!getProject().hasFacet(ProjectConfig.class))
      return false;

    final Boolean isCoreInstalled = getProject().getFacet(ProjectConfig.class).getProjectProperty(
            ProjectProperty.CORE_IS_INSTALLED, Boolean.class);

    return isCoreInstalled != null && isCoreInstalled;
  }
}
