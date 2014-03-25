package org.jboss.errai.forge.facet.dependency;

import static org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact.ErraiJboss;
import static org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact.ErraiTools;
import static org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact.GwtUser;
import static org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact.JUnit;
import static org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact.JbossSupport;

import org.apache.maven.model.Activation;
import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
import org.jboss.errai.forge.facet.base.CoreBuildFacet;
import org.jboss.errai.forge.facet.base.DependencyManagementFacet;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.maven.projects.MavenFacet;

/**
 * This facet sets all the common Maven dependencies required to build or run in
 * development mode an application with Errai.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
@FacetConstraint({ CoreBuildFacet.class, DependencyManagementFacet.class })
public class ErraiBuildDependencyFacet extends AbstractDependencyFacet {

  public ErraiBuildDependencyFacet() {
    setCoreDependencies(DependencyBuilder.create(ErraiTools.toString()), DependencyBuilder.create(GwtUser.toString())
            .setScopeType("provided"), DependencyBuilder.create(ErraiJboss.toString()), DependencyBuilder
            .create(JUnit.toString()).setScopeType("test"),
            DependencyBuilder.create(JbossSupport.toString()));
  }

  @Override
  public boolean install() {
    if (super.install()) {
      // Set main profile to be active by default
      final MavenFacet coreFacet = getProject().getFacet(MavenFacet.class);
      final Model pom = coreFacet.getModel();
      Profile profile = getProfile(MAIN_PROFILE, pom.getProfiles());
      if (profile == null) {
        profile = new Profile();
        profile.setId(MAIN_PROFILE);
        pom.addProfile(profile);
      }
      if (profile.getActivation() == null)
        profile.setActivation(new Activation());
      profile.getActivation().setActiveByDefault(true);
      coreFacet.setModel(pom);

      return true;
    }
    else {
      return false;
    }
  }
}
