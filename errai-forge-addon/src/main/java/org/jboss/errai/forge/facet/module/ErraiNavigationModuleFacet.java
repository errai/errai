package org.jboss.errai.forge.facet.module;

import java.util.Arrays;

import org.jboss.errai.forge.config.ProjectConfig;
import org.jboss.errai.forge.constant.ModuleVault.Module;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;

@FacetConstraint({ ProjectConfig.class })
public class ErraiNavigationModuleFacet extends AbstractModuleFacet {

  public ErraiNavigationModuleFacet() {
    modules = Arrays.asList(new Module[] {
        Module.ErraiNavigation
    });
  }

}
