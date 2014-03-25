package org.jboss.errai.forge.facet.module;

import java.util.Arrays;
import org.jboss.errai.forge.constant.ModuleVault.Module;

public class ErraiSecurityModuleFacet extends AbstractModuleFacet {

  public ErraiSecurityModuleFacet() {
    modules = Arrays.asList(new Module[] {
        Module.ErraiSecurity
    });
  }

}
