package org.jboss.errai.forge.facet.module;

import java.util.Arrays;

import org.jboss.errai.forge.config.ProjectConfig;
import org.jboss.errai.forge.constant.ModuleVault.Module;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;

/**
 * This facet adds the errai-bus GWT module to a project.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@FacetConstraint({ ProjectConfig.class })
public class ErraiBusModuleFacet extends AbstractModuleFacet {
  
  public ErraiBusModuleFacet() {
    modules = Arrays.asList(new Module[] {
            Module.ErraiBus
    });
  }

}
