package org.jboss.errai.forge.facet.module;

import java.util.Arrays;

import org.jboss.errai.forge.config.ProjectConfig;
import org.jboss.errai.forge.constant.ModuleVault.Module;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;

@FacetConstraint({ ProjectConfig.class })
public class ErraiDataBindingModuleFacet extends AbstractModuleFacet {

  public ErraiDataBindingModuleFacet() {
    modules = Arrays.asList(new Module[] {
        Module.ErraiDataBinding
    });
  }

}
