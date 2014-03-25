package org.jboss.errai.forge.facet.aggregate;

import org.jboss.errai.forge.facet.dependency.ErraiDataBindingDependencyFacet;
import org.jboss.errai.forge.facet.module.ErraiDataBindingModuleFacet;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;

@FacetConstraint({ CoreFacet.class, ErraiIocFacet.class, ErraiDataBindingDependencyFacet.class,
    ErraiDataBindingModuleFacet.class })
public class ErraiDataBindingFacet extends BaseAggregatorFacet {

  @Override
  public String getFeatureName() {
    return "Errai Data Binding";
  }

  @Override
  public String getFeatureDescription() {
    return "Bind models to GWT Widgets to have changes in one automatically reflected in the other.";
  }

  @Override
  public String getShortName() {
    return "data-binding";
  }
}
