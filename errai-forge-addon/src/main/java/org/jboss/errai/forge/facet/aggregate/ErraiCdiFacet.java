package org.jboss.errai.forge.facet.aggregate;

import org.jboss.errai.forge.facet.dependency.ErraiWeldIntegrationDependencyFacet;
import org.jboss.errai.forge.facet.resource.BeansXmlFacet;
import org.jboss.errai.forge.facet.resource.CdiWebXmlFacet;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;

/**
 * An aggregator facet for Errai CDI. This enables CDI events to be observed
 * between the client and server.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
@FacetConstraint({ CoreFacet.class, ErraiCdiClientFacet.class, ErraiWeldIntegrationDependencyFacet.class,
    CdiWebXmlFacet.class, BeansXmlFacet.class })
public class ErraiCdiFacet extends BaseAggregatorFacet {

  @Override
  public String getFeatureName() {
    return "Errai CDI Integration";
  }

  @Override
  public String getFeatureDescription() {
    return "Integrates server-side CDI Events and Observers with Errai Client CDI.";
  }

  @Override
  public String getShortName() {
    return "cdi";
  }
}
