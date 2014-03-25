package org.jboss.errai.forge.facet.aggregate;

import org.jboss.errai.forge.facet.dependency.ErraiJaxrsClientDependencyFacet;
import org.jboss.errai.forge.facet.module.ErraiJaxrsClientModuleFacet;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;

@FacetConstraint({ CoreFacet.class, ErraiJaxrsClientDependencyFacet.class, ErraiJaxrsClientModuleFacet.class })
public class ErraiJaxrsClientFacet extends BaseAggregatorFacet {

  @Override
  public String getFeatureName() {
    return "Errai JAX-RS";
  }

  @Override
  public String getFeatureDescription() {
    return "A simple API for accessing server-side JAX-RS endpoints.";
  }

  @Override
  public String getShortName() {
    return "jaxrs-client";
  }
}
