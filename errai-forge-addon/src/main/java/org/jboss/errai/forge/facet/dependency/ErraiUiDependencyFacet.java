package org.jboss.errai.forge.facet.dependency;

import org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;

public class ErraiUiDependencyFacet extends AbstractDependencyFacet {
  
  public ErraiUiDependencyFacet() {
    setCoreDependencies(DependencyBuilder.create(DependencyArtifact.ErraiUi.toString()));
  }

}
