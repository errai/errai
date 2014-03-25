package org.jboss.errai.forge.facet.dependency;

import org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;

public class ErraiCordovaDependencyFacet extends AbstractDependencyFacet {
  public ErraiCordovaDependencyFacet() {
    setCoreDependencies(DependencyBuilder.create(DependencyArtifact.ErraiCordova.toString()));
  }
}
