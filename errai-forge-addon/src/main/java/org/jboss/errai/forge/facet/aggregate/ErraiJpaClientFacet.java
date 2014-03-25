package org.jboss.errai.forge.facet.aggregate;

import org.jboss.errai.forge.facet.dependency.ErraiJpaClientDependencyFacet;
import org.jboss.errai.forge.facet.module.ErraiJpaClientModuleFacet;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;

@FacetConstraint({ CoreFacet.class, ErraiIocFacet.class, ErraiDataBindingFacet.class,
    ErraiJpaClientDependencyFacet.class, ErraiJpaClientModuleFacet.class })
public class ErraiJpaClientFacet extends BaseAggregatorFacet {

  @Override
  public String getFeatureName() {
    return "Errai JPA Client";
  }

  @Override
  public String getFeatureDescription() {
    return "An implementation of JPA for GWT.";
  }

  @Override
  public String getShortName() {
    return "jpa-client";
  }
}
