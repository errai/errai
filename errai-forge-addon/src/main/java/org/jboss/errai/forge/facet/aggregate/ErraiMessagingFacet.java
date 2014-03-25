package org.jboss.errai.forge.facet.aggregate;

import org.jboss.errai.forge.facet.dependency.ErraiBuildDependencyFacet;
import org.jboss.errai.forge.facet.dependency.ErraiBusDependencyFacet;
import org.jboss.errai.forge.facet.module.ErraiBusModuleFacet;
import org.jboss.errai.forge.facet.resource.ErraiBusServletConfigFacet;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;

/**
 * Aggregator facet for Errai Messaging. Installing this facet adds the
 * necessary dependencies and web.xml configurations to immediately use the
 * Errai Message Bus in a project.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
@FacetConstraint({ CoreFacet.class, ErraiBusDependencyFacet.class, ErraiBuildDependencyFacet.class,
    ErraiBusModuleFacet.class, ErraiBusServletConfigFacet.class })
public class ErraiMessagingFacet extends BaseAggregatorFacet {

  @Override
  public String getFeatureName() {
    return "Errai Messaging";
  }

  @Override
  public String getFeatureDescription() {
    return "Enables communication between services, either locally or between client and server,"
            + " through a simple Message Bus API.";
  }

  @Override
  public String getShortName() {
    return "messaging";
  }
}
