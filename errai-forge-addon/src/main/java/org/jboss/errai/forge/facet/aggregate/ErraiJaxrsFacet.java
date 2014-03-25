package org.jboss.errai.forge.facet.aggregate;

import org.jboss.errai.forge.facet.dependency.ErraiJaxrsProviderDependencyFacet;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;

@FacetConstraint({ CoreFacet.class, ErraiJaxrsClientFacet.class, ErraiJaxrsProviderDependencyFacet.class })
public class ErraiJaxrsFacet extends BaseAggregatorFacet {

  @Override
  public String getFeatureName() {
    return "Errai JAX-RS";
  }

  @Override
  public String getFeatureDescription() {
    return "Use REST-ful JAX-RS endpoints with a simple declarative syntax.";
  }

  @Override
  public String getShortName() {
    return "jaxrs";
  }
}
