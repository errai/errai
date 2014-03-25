package org.jboss.errai.forge.facet.dependency;

import static org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact.ErraiCommon;

import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;

/**
 * This facet sets the Maven dependencies needed to use the errai-common project.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class ErraiCommonDepdencyFacet extends AbstractDependencyFacet {

  public ErraiCommonDepdencyFacet() {
    setCoreDependencies(
            DependencyBuilder.create(ErraiCommon.toString())
    );
  }
  
}
