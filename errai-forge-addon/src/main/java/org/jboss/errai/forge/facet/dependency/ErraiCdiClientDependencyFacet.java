package org.jboss.errai.forge.facet.dependency;

import org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;

/**
 * This facet sets the Maven dependencies needed to use the errai-cdi-client project.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class ErraiCdiClientDependencyFacet extends AbstractDependencyFacet {

  public ErraiCdiClientDependencyFacet() {
    setCoreDependencies(DependencyBuilder.create(DependencyArtifact.ErraiCdiClient.toString()));
  }

}
