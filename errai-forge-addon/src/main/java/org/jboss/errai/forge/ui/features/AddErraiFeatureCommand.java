package org.jboss.errai.forge.ui.features;

import javax.inject.Inject;

import org.jboss.errai.forge.config.ProjectConfig;
import org.jboss.errai.forge.config.ProjectConfig.ProjectProperty;
import org.jboss.errai.forge.config.SerializableSet;
import org.jboss.errai.forge.facet.aggregate.CoreFacet;
import org.jboss.errai.forge.facet.aggregate.AggregatorFacetReflections.Feature;
import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;

@FacetConstraint({ CoreFacet.class })
public class AddErraiFeatureCommand extends AbstractFeatureCommand {

  @Inject
  private FacetFactory facetFactory;

  @Override
  protected FeatureFilter getFilter() {
    return new FeatureFilter() {
      @Override
      public boolean filter(Feature feature, Project project) {
        return !project.hasFacet(feature.getFeatureClass());
      }
    };
  }
  
  @Override
  public UICommandMetadata getMetadata(UIContext context) {
    return Metadata.forCommand(AddErraiFeatureCommand.class)
            .name("Errai: Add Features")
            .category(Categories.create("Project", "Errai"))
            .description("Add the necessary configurations for using Errai features in a Maven project.");
  }

  protected void performOperation(Project project, Feature feature) throws Exception {
    try {
      facetFactory.install(project, feature.getFeatureClass());

      final ProjectConfig projectConfig = project.getFacet(ProjectConfig.class);
      SerializableSet installed = projectConfig.getProjectProperty(ProjectProperty.INSTALLED_FEATURES,
              SerializableSet.class);
      if (installed == null)
        installed = new SerializableSet();

      installed.add(feature.getShortName());

      projectConfig.setProjectProperty(ProjectProperty.INSTALLED_FEATURES, installed);
    }
    catch (Exception e) {
      throw new Exception("Could not install " + feature.getLongName(), e);
    }
  }

  @Override
  protected String getSelectionDescription() {
    return "The selected Errai Features will be added to your project,"
            + " including any necessary configurations and other required features.";
  }

  @Override
  protected String getSelectionLabel() {
    return "Select Errai Features to Add";
  }

}
