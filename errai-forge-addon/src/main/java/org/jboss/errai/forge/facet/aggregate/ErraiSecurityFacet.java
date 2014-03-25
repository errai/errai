package org.jboss.errai.forge.facet.aggregate;

import org.jboss.errai.forge.facet.dependency.ErraiSecurityDependencyFacet;
import org.jboss.errai.forge.facet.module.ErraiSecurityModuleFacet;
import org.jboss.errai.forge.facet.resource.SecurityBeansXmlFacet;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;

@FacetConstraint({ CoreFacet.class, ErraiMessagingFacet.class, ErraiIocFacet.class, ErraiUiFacet.class,
    ErraiNavigationFacet.class, ErraiCdiFacet.class, ErraiSecurityDependencyFacet.class,
    ErraiSecurityModuleFacet.class, SecurityBeansXmlFacet.class })
public class ErraiSecurityFacet extends BaseAggregatorFacet {

  @Override
  public String getFeatureName() {
    return "Errai Security";
  }

  @Override
  public String getShortName() {
    return "errai-security";
  }

  @Override
  public String getFeatureDescription() {
    return "A module providing support for security features such as"
            + " page redirection and secure RPC services.";
  }

}
