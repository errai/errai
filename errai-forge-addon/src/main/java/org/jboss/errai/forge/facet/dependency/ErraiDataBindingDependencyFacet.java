package org.jboss.errai.forge.facet.dependency;

import org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;

public class ErraiDataBindingDependencyFacet extends AbstractDependencyFacet {

  public ErraiDataBindingDependencyFacet() {
    setCoreDependencies(DependencyBuilder.create(DependencyArtifact.ErraiDataBinding.toString()));
  }

}
