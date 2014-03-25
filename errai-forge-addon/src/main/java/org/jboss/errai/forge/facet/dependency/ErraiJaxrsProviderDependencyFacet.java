package org.jboss.errai.forge.facet.dependency;

import org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;

public class ErraiJaxrsProviderDependencyFacet extends AbstractDependencyFacet {

  public ErraiJaxrsProviderDependencyFacet() {
    setCoreDependencies(DependencyBuilder.create(DependencyArtifact.ErraiJaxrsProvider.toString()));
  }

}
