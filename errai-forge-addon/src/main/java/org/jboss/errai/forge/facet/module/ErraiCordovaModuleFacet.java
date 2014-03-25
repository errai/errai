package org.jboss.errai.forge.facet.module;

import java.util.Arrays;

import org.jboss.errai.forge.config.ProjectConfig;
import org.jboss.errai.forge.constant.ModuleVault;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;

@FacetConstraint({ ProjectConfig.class })
public class ErraiCordovaModuleFacet extends AbstractModuleFacet {
  
  public ErraiCordovaModuleFacet() {
    modules = Arrays.asList(new ModuleVault.Module[] {
       ModuleVault.Module.ErraiCordova     
    });
  }

}
