package org.jboss.errai.forge.facet.dependency;

import static org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact.ErraiIoc;

import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;

/**
 * This facet sets the Maven dependencies needed to use the errai-ioc project.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class ErraiIocDependencyFacet extends AbstractDependencyFacet {
  
  public ErraiIocDependencyFacet() {
    setCoreDependencies(DependencyBuilder.create(ErraiIoc.toString()));
  }

}
