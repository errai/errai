package org.jboss.errai.forge.facet.aggregate;

import org.jboss.errai.forge.facet.dependency.ErraiNavigationDependencyFacet;
import org.jboss.errai.forge.facet.module.ErraiNavigationModuleFacet;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;

@FacetConstraint({ CoreFacet.class, ErraiUiFacet.class, ErraiNavigationDependencyFacet.class,
    ErraiNavigationModuleFacet.class })
public class ErraiNavigationFacet extends BaseAggregatorFacet {

  @Override
  public String getFeatureName() {
    return "Errai Navigation";
  }

  @Override
  public String getFeatureDescription() {
    return "Allows the use of Errai UI Templated Widgets as pages which can be navigated between.";
  }

  @Override
  public String getShortName() {
    return "navigation";
  }
}
