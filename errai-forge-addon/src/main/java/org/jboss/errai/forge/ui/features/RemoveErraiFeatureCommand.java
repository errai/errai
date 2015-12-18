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

package org.jboss.errai.forge.ui.features;

import org.jboss.errai.forge.config.ProjectConfig;
import org.jboss.errai.forge.config.ProjectProperty;
import org.jboss.errai.forge.config.SerializableSet;
import org.jboss.errai.forge.facet.aggregate.AggregatorFacetReflections.Feature;
import org.jboss.errai.forge.facet.aggregate.BaseAggregatorFacet;
import org.jboss.errai.forge.facet.aggregate.CoreFacet;
import org.jboss.forge.addon.facets.MutableFaceted;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFacet;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;

@FacetConstraint({ CoreFacet.class })
public class RemoveErraiFeatureCommand extends AbstractFeatureCommand {

  @Override
  protected FeatureFilter getFilter() {
    return new FeatureFilter() {
      @Override
      public boolean filter(Feature feature, Project project) {
        final ProjectConfig projectConfig = project
                .getFacet(ProjectConfig.class);
        final SerializableSet installed = projectConfig.getProjectProperty(
                ProjectProperty.INSTALLED_FEATURES, SerializableSet.class);

        return project.hasFacet(feature.getFeatureClass())
                && installed != null && installed.contains(feature.getShortName());
      }
    };
  }

  @Override
  public UICommandMetadata getMetadata(UIContext context) {
    return Metadata.forCommand(RemoveErraiFeatureCommand.class)
            .name("Errai: Remove Features")
            .category(Categories.create("Project", "Errai"))
            .description("Remove Errai features that have been configured with the 'Add Errai Features' command.");
  }
  
  @Override
  protected String getSelectionDescription() {
    return "The selected Errai features will be removed from your project."
            + " Any additional features which were not explicitly installed and are no longer needed will also be removed.";
  }

  @Override
  protected String getSelectionLabel() {
    return "Select Errai Features to Remove";
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void performOperation(Project project, Feature feature) throws Exception {
    try {
      final MutableFaceted<ProjectFacet> mutable = (MutableFaceted<ProjectFacet>) project;
      final BaseAggregatorFacet facet = mutable.getFacet(feature.getFeatureClass());

      if (!facet.uninstallRequirements() || !mutable.uninstall(facet))
        throw new Exception(String.format("Could not uninstall %s from %s.", facet.getClass(), project));

      final ProjectConfig projectConfig = project.getFacet(ProjectConfig.class);
      SerializableSet installed = projectConfig.getProjectProperty(ProjectProperty.INSTALLED_FEATURES,
              SerializableSet.class);
      if (installed == null)
        installed = new SerializableSet();
      
      installed.remove(feature.getShortName());
      
      projectConfig.setProjectProperty(ProjectProperty.INSTALLED_FEATURES, installed);
    }
    catch (Exception e) {
      throw new Exception("Could not remove " + feature.getLongName(), e);
    }
  }

}
