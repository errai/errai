package org.jboss.errai.forge.facet.aggregate;

import org.jboss.errai.forge.facet.dependency.ErraiDataBindingDependencyFacet;

import org.jboss.errai.forge.facet.dependency.ErraiUiDependencyFacet;
import org.jboss.errai.forge.facet.module.ErraiUiModuleFacet;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;

/*
 * NOTE: We must depend on the ErraiDataBindingDependencyFacet because the errai
 * ui module inherits from the databinding module, but the errai ui does not
 * have a dependency (via maven) on databinding.
 */

@FacetConstraint({ CoreFacet.class, ErraiIocFacet.class, ErraiCdiClientFacet.class, ErraiUiDependencyFacet.class,
    ErraiDataBindingDependencyFacet.class, ErraiUiModuleFacet.class })
public class ErraiUiFacet extends BaseAggregatorFacet {

  @Override
  public String getFeatureName() {
    return "Errai UI";
  }

  @Override
  public String getFeatureDescription() {
    return "Create your own custom Widgets using HTML5 templates. "
            + "Elements in the template can be bound to fields in Java classes with a simple declarative syntax.";
  }

  @Override
  public String getShortName() {
    return "ui";
  }
}
