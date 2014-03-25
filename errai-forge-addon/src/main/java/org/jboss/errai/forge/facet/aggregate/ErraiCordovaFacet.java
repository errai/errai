package org.jboss.errai.forge.facet.aggregate;

import org.jboss.errai.forge.facet.dependency.ErraiCordovaDependencyFacet;
import org.jboss.errai.forge.facet.module.ErraiCordovaModuleFacet;
import org.jboss.errai.forge.facet.plugin.CordovaPluginFacet;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;

@FacetConstraint({ ErraiCordovaDependencyFacet.class, ErraiCordovaModuleFacet.class, CordovaPluginFacet.class })
public class ErraiCordovaFacet extends BaseAggregatorFacet {

  @Override
  public String getFeatureName() {
    return "Errai Cordova";
  }

  @Override
  public String getShortName() {
    return "cordova";
  }

  @Override
  public String getFeatureDescription() {
    return "CDI components and build tools for compiling Errai applications natively to mobile devices.";
  }

}
