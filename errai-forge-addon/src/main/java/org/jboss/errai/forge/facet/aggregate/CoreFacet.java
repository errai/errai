package org.jboss.errai.forge.facet.aggregate;

import org.jboss.errai.forge.config.ProjectConfig;
import org.jboss.errai.forge.facet.dependency.ErraiBuildDependencyFacet;
import org.jboss.errai.forge.facet.module.ModuleCoreFacet;
import org.jboss.errai.forge.facet.plugin.CleanPluginFacet;
import org.jboss.errai.forge.facet.plugin.CompilerPluginFacet;
import org.jboss.errai.forge.facet.plugin.DependencyPluginFacet;
import org.jboss.errai.forge.facet.plugin.GwtPluginFacet;
import org.jboss.errai.forge.facet.plugin.JbossPluginFacet;
import org.jboss.errai.forge.facet.plugin.WarPluginFacet;
import org.jboss.errai.forge.facet.resource.ErraiAppPropertiesFacet;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;

/**
 * Aggregates core facets required by all other facet aggregators. Installing
 * this facet will add all the necessary dependencies, profile, and plugin
 * configurations to run a GWT/Errai project in development mode or compile to
 * production mode.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
@FacetConstraint({ ProjectConfig.class, ErraiBuildDependencyFacet.class, CleanPluginFacet.class,
    CompilerPluginFacet.class,
    DependencyPluginFacet.class, GwtPluginFacet.class, JbossPluginFacet.class, WarPluginFacet.class,
    ModuleCoreFacet.class, ErraiAppPropertiesFacet.class })
public class CoreFacet extends BaseAggregatorFacet {

  @Override
  public String getFeatureName() {
    return "Errai Build Setup";
  }

  @Override
  public String getFeatureDescription() {
    return "The core build setup required for running development mode or compiling for deployment.";
  }

  @Override
  public String getShortName() {
    return "core";
  }
}
