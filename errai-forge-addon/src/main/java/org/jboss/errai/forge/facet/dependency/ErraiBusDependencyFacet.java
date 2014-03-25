package org.jboss.errai.forge.facet.dependency;

import static org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact.ErraiBus;

import java.util.Collection;
import java.util.HashMap;

import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;

/**
 * This facet sets the Maven dependencies necessary for using the errai-bus project.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class ErraiBusDependencyFacet extends AbstractDependencyFacet {

  public ErraiBusDependencyFacet() {
    setCoreDependencies(DependencyBuilder.create(ErraiBus.toString()));
    profileDependencies = new HashMap<String, Collection<DependencyBuilder>>();
  }

}
