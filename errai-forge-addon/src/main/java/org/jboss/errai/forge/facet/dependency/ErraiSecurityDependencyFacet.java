package org.jboss.errai.forge.facet.dependency;

import org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;

public class ErraiSecurityDependencyFacet extends AbstractDependencyFacet {

  public ErraiSecurityDependencyFacet() {
    setCoreDependencies(
            DependencyBuilder.create(DependencyArtifact.ErraiSecurityServer.toString()),
            DependencyBuilder.create(DependencyArtifact.ErraiSecurityClient.toString()));
  }

}
