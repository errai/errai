package org.jboss.errai.forge.facet.dependency;

import org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;

/**
 * This facet sets the Maven dependencies needed to use the errai-weld-integration project.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class ErraiWeldIntegrationDependencyFacet extends AbstractDependencyFacet {
  
  public ErraiWeldIntegrationDependencyFacet() {
    setCoreDependencies(DependencyBuilder.create(DependencyArtifact.ErraiWeldIntegration.toString()));
  }

}
