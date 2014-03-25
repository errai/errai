package org.jboss.errai.forge.ui.features;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.jboss.errai.forge.facet.aggregate.AggregatorFacetReflections;
import org.jboss.errai.forge.facet.aggregate.AggregatorFacetReflections.Feature;
import org.jboss.forge.addon.convert.Converter;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.input.UISelectMany;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;

public abstract class AbstractFeatureCommand extends AbstractProjectCommand {

  protected static interface FeatureFilter {
    /**
     * True iff the feature should be displayed.
     */
    public boolean filter(final Feature feature, final Project project);
  }

  protected class FeatureValueConverter implements Converter<String, Feature> {

    @Override
    public Feature convert(final String longName) {
      return facetReflections.getFeatureLong(longName);
    }

  }

  protected class FeatureLabelConverter implements Converter<Feature, String> {

    @Override
    public String convert(final Feature feature) {
      return feature.getLongName().toLowerCase().replace(' ', '-');
    }

  }

  @Inject
  private ProjectFactory projectFactory;

  @Inject
  private UISelectMany<Feature> featureSelect;

  @Inject
  private AggregatorFacetReflections facetReflections;

  @Override
  public void initializeUI(UIBuilder builder) throws Exception {
    final Project project = getSelectedProject(builder.getUIContext());

    featureSelect.setLabel(getSelectionLabel());
    featureSelect.setDescription(getSelectionDescription());

    final List<Feature> features = new ArrayList<Feature>();
    final FeatureFilter filter = getFilter();
    for (final Feature feature : facetReflections.iterable()) {
      if (filter.filter(feature, project)) {
        features.add(feature);
      }
    }

    featureSelect.setValueChoices(features);
    featureSelect.setValueConverter(new FeatureValueConverter());
    // Workaround FORGE-1639
    if (!builder.getUIContext().getProvider().isGUI())
      featureSelect.setItemLabelConverter(new FeatureLabelConverter());

    builder.add(featureSelect);
  }

  protected abstract FeatureFilter getFilter();

  protected abstract String getSelectionDescription();

  protected abstract String getSelectionLabel();

  @Override
  public Result execute(UIExecutionContext context) throws Exception {
    final Project project = getSelectedProject(context.getUIContext());
    final Iterable<Feature> features = featureSelect.getValue();

    if (features != null) {
      for (final Feature feature : features) {
        try {
          performOperation(project, feature);
        }
        catch (Exception e) {
          return Results.fail(e.getMessage(), e);
        }
      }
    }

    return Results.success();
  }

  protected abstract void performOperation(Project project, Feature feature) throws Exception;

  @Override
  protected boolean isProjectRequired() {
    return true;
  }

  @Override
  public ProjectFactory getProjectFactory() {
    return projectFactory;
  }

}
