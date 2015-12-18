/*
 * Copyright (C) 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.forge.facet.dependency;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
import org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact;
import org.jboss.errai.forge.constant.PomPropertyVault.Property;
import org.jboss.errai.forge.test.base.ForgeTest;
import org.jboss.errai.forge.util.MavenConverter;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.maven.projects.MavenFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFacet;
import org.jboss.forge.addon.projects.facets.DependencyFacet;
import org.junit.Test;

import javax.enterprise.context.Dependent;
import java.util.*;

import static org.junit.Assert.*;

public class AbstractDependencyFacetTest extends ForgeTest {

  @Dependent
  public static class NoProfileDependencyFacet extends AbstractDependencyFacet {
    public NoProfileDependencyFacet() {
      coreDependencies = Arrays.asList(new DependencyBuilder[] { DependencyBuilder
              .create(DependencyArtifact.ErraiCommon.toString()) });
      profileDependencies = new HashMap<String, Collection<DependencyBuilder>>();
    }
  }

  @Dependent
  public static class ProfileDependencyFacet extends AbstractDependencyFacet {
    public ProfileDependencyFacet() {
      coreDependencies = Arrays.asList(new DependencyBuilder[0]);
      profileDependencies = new HashMap<String, Collection<DependencyBuilder>>();
      profileDependencies.put("myProfile", Arrays.asList(new DependencyBuilder[] { DependencyBuilder
              .create(DependencyArtifact.ErraiCommon.toString()) }));
    }
  }

  @Dependent
  public static class BlacklistedDependencyFacet extends AbstractDependencyFacet {
    public BlacklistedDependencyFacet() {
      coreDependencies = Arrays.asList(new DependencyBuilder[] { DependencyBuilder.create(DependencyArtifact.ErraiTools
              .toString()) });
    }
    // Allows dependencies to be set from this class.
    @Override
    public void setCoreDependencies(DependencyBuilder... deps) {
      super.setCoreDependencies(deps);
    }
    @Override
    public void setProfileDependencies(String name, DependencyBuilder... deps) {
      super.setProfileDependencies(name, deps);
    }
  }
 
  @Test
  public void testNoProfileEmptyInstall() throws Exception {
    final Project project = initializeJavaProject();

    prepareProjectPom(project);

    facetFactory.install(project, NoProfileDependencyFacet.class);

    assertTrue(project.hasFacet(NoProfileDependencyFacet.class.asSubclass(ProjectFacet.class)));
    assertTrue(project.getFacet(DependencyFacet.class).hasDirectDependency(
            DependencyBuilder.create(DependencyArtifact.ErraiCommon.toString())));
  }

  @Test
  public void testProfileEmptyInstall() throws Exception {
    final Project project = initializeJavaProject();
    prepareProjectPom(project);

    facetFactory.install(project, ProfileDependencyFacet.class);

    assertTrue(project.hasFacet(ProfileDependencyFacet.class.asSubclass(ProjectFacet.class)));
    List<Profile> profiles = project.getFacet(MavenFacet.class).getModel().getProfiles();
    assertEquals(1, profiles.size());
    assertEquals("myProfile", profiles.get(0).getId());
    assertEquals(1, profiles.get(0).getDependencies().size());
    assertEquals(DependencyArtifact.ErraiCommon.getArtifactId(), profiles.get(0).getDependencies().get(0)
            .getArtifactId());
  }

  @Test
  public void testProfileExistingProfile() throws Exception {
    final Project project = initializeJavaProject();
    MavenFacet coreFacet = project.getFacet(MavenFacet.class);
    prepareProjectPom(project);

    final Profile profile = new Profile();
    profile.setId("myProfile");
    profile.addDependency(MavenConverter.convert(DependencyBuilder.create("org.jboss.errai:errai-ui")));

    Model pom = coreFacet.getModel();
    pom.addProfile(profile);
    coreFacet.setModel(pom);

    facetFactory.install(project, ProfileDependencyFacet.class);

    assertTrue(project.hasFacet(ProfileDependencyFacet.class.asSubclass(ProjectFacet.class)));
    List<Profile> profiles = coreFacet.getModel().getProfiles();
    assertEquals(1, profiles.size());
    assertEquals("myProfile", profiles.get(0).getId());
    assertEquals(2, profiles.get(0).getDependencies().size());
    assertEquals("errai-ui", profiles.get(0).getDependencies().get(0).getArtifactId());
    assertEquals(DependencyArtifact.ErraiCommon.getArtifactId(), profiles.get(0).getDependencies().get(1)
            .getArtifactId());
  }

  @Test
  public void testProfileInstallNoDuplication() throws Exception {
    final Project project = initializeJavaProject();
    prepareProjectPom(project);
    MavenFacet coreFacet = project.getFacet(MavenFacet.class);

    final Profile profile = new Profile();
    profile.setId("myProfile");
    profile.addDependency(MavenConverter.convert(DependencyBuilder.create(DependencyArtifact.ErraiCommon.toString())));

    Model pom = coreFacet.getModel();
    pom.addProfile(profile);
    coreFacet.setModel(pom);

    facetFactory.install(project, ProfileDependencyFacet.class);

    assertTrue(project.hasFacet(ProfileDependencyFacet.class.asSubclass(ProjectFacet.class)));
    List<Profile> profiles = coreFacet.getModel().getProfiles();
    assertEquals(1, profiles.size());
    assertEquals("myProfile", profiles.get(0).getId());
    assertEquals(1, profiles.get(0).getDependencies().size());
    assertEquals(DependencyArtifact.ErraiCommon.getArtifactId(), profiles.get(0).getDependencies().get(0)
            .getArtifactId());
  }

  @Test
  public void testConflictingDependency() throws Exception {
    final Project project = initializeJavaProject();
    prepareProjectPom(project);

    final DependencyFacet depFacet = project.getFacet(DependencyFacet.class);
    depFacet.addDirectDependency(DependencyBuilder.create(DependencyArtifact.ErraiCommon.toString() + ":2.4.2.Final"));

    facetFactory.install(project, NoProfileDependencyFacet.class);

    assertTrue(project.hasFacet(NoProfileDependencyFacet.class.asSubclass(ProjectFacet.class)));
    assertTrue(depFacet.hasDirectDependency(DependencyBuilder.create(DependencyArtifact.ErraiCommon.toString())
            .setVersion(Property.ErraiVersion.invoke())));
    assertFalse(depFacet.hasDirectDependency(DependencyBuilder.create(DependencyArtifact.ErraiCommon.toString()
            + "2.4.2.Final")));
  }

  @Test
  public void testNoProfileUninstall() throws Exception {
    // Setup
    final Project project = initializeJavaProject();
    final MavenFacet coreFacet = project.getFacet(MavenFacet.class);

    prepareProjectPom(project);

    facetFactory.install(project, NoProfileDependencyFacet.class);

    assertTrue("Precondition failed.", project.hasFacet(NoProfileDependencyFacet.class.asSubclass(ProjectFacet.class)));
    assertTrue(
            "Precondition failed.",
            project.getFacet(DependencyFacet.class).hasDirectDependency(
                    DependencyBuilder.create(DependencyArtifact.ErraiCommon.toString())));

    // Actual test
    final ProjectFacet facet = project.getFacet(NoProfileDependencyFacet.class.asSubclass(ProjectFacet.class));
    assertTrue(facet.uninstall());
    // assertFalse(project.hasFacet(NoProfileDependencyFacet.class.asSubclass(ProjectFacet.class)));
    assertEquals(0, coreFacet.getModel().getDependencies().size());
  }

  @Test
  public void testProfileUninstall() throws Exception {
    // Setup
    final Project project = initializeJavaProject();
    prepareProjectPom(project);

    facetFactory.install(project, ProfileDependencyFacet.class);

    assertTrue(project.hasFacet(ProfileDependencyFacet.class.asSubclass(ProjectFacet.class)));
    List<Profile> profiles = project.getFacet(MavenFacet.class).getModel().getProfiles();
    assertEquals("Precondition failed.", 1, profiles.size());
    assertEquals("Precondition failed.", "myProfile", profiles.get(0).getId());
    assertEquals("Precondition failed.", 1, profiles.get(0).getDependencies().size());
    assertEquals("Precondition failed.", DependencyArtifact.ErraiCommon.getArtifactId(), profiles.get(0)
            .getDependencies().get(0).getArtifactId());

    // Actual test
    final ProjectFacet facet = project.getFacet(ProfileDependencyFacet.class.asSubclass(ProjectFacet.class));
    assertTrue(facet.uninstall());
    profiles = project.getFacet(MavenFacet.class).getModel().getProfiles();
    assertEquals(0, profiles.get(0).getDependencies().size());
  }

  @Test
  public void testNoProfileIsInstalled() throws Exception {
    // Setup
    final Project project = initializeJavaProject();
    NoProfileDependencyFacet facet = facetFactory.create(project,
            NoProfileDependencyFacet.class);

    prepareProjectPom(project);

    assertFalse(facet.isInstalled());

    facetFactory.install(project, NoProfileDependencyFacet.class);

    assertTrue("Precondition failed.", project.hasFacet(NoProfileDependencyFacet.class.asSubclass(ProjectFacet.class)));
    assertTrue(
            "Precondition failed.",
            project.getFacet(DependencyFacet.class).hasDirectDependency(
                    DependencyBuilder.create(DependencyArtifact.ErraiCommon.toString())));

    // Actual test
    facet = facetFactory.create(project, NoProfileDependencyFacet.class);
    assertTrue(facet.isInstalled());
  }

  @Test
  public void testProfileIsInstalled() throws Exception {
    // Setup
    final Project project = initializeJavaProject();
    ProfileDependencyFacet facet = facetFactory.create(project, ProfileDependencyFacet.class);
    prepareProjectPom(project);

    assertFalse(facet.isInstalled());

    facetFactory.install(project, ProfileDependencyFacet.class);

    assertTrue(project.hasFacet(ProfileDependencyFacet.class.asSubclass(ProjectFacet.class)));
    List<Profile> profiles = project.getFacet(MavenFacet.class).getModel().getProfiles();
    assertEquals("Precondition failed.", 1, profiles.size());
    assertEquals("Precondition failed.", "myProfile", profiles.get(0).getId());
    assertEquals("Precondition failed.", 1, profiles.get(0).getDependencies().size());
    assertEquals("Precondition failed.", DependencyArtifact.ErraiCommon.getArtifactId(), profiles.get(0)
            .getDependencies().get(0).getArtifactId());

    // Actual test
    facet = facetFactory.create(project, ProfileDependencyFacet.class);
    assertTrue(facet.isInstalled());
  }

  @Test
  public void testBlacklistedDependency() throws Exception {
    final Project project = initializeJavaProject();
    final MavenFacet coreFacet = project.getFacet(MavenFacet.class);
    prepareProjectPom(project);
    Model pom = coreFacet.getModel();

    final DependencyFacet depFacet = project.getFacet(DependencyFacet.class);

    facetFactory.install(project, BlacklistedDependencyFacet.class);
    pom = coreFacet.getModel();

    assertTrue(project.hasFacet(BlacklistedDependencyFacet.class.asSubclass(ProjectFacet.class)));
    assertTrue(depFacet.hasDirectDependency(DependencyBuilder.create(DependencyArtifact.ErraiTools.toString())
            .setVersion(Property.ErraiVersion.invoke())));
    // This dependency should have been transitively included through
    // errai-tools
    assertTrue(depFacet.hasEffectiveDependency(DependencyBuilder.create(DependencyArtifact.Hsq.toString())));

    assertEquals(1, pom.getProfiles().size());
    assertEquals(2, pom.getProfiles().get(0).getDependencies().size());

    final Set<String> providedClassifiers = getProvidedClassifiers(pom);

    assertTrue(providedClassifiers.contains(DependencyArtifact.ErraiTools.toString()));
    assertTrue(providedClassifiers.contains(DependencyArtifact.Hsq.toString()));
  }

  /**
   * Check that the facet won't doubly add a provided scope dependency that is
   * already set to be added in a profile within the facet.
   */
  @Test
  public void testBlacklistedDependencyNonDuplication1() throws Exception {
    final Project project = initializeJavaProject();
    final BlacklistedDependencyFacet facet = facetFactory.create(project,
            BlacklistedDependencyFacet.class);
    final MavenFacet coreFacet = project.getFacet(MavenFacet.class);
    prepareProjectPom(project);
    Model pom = coreFacet.getModel();

    final DependencyFacet depFacet = project.getFacet(DependencyFacet.class);

    /*
     * This is what makes this test different than the last: we want to check
     * that the facet won't add a dependency that is already scheduled to add.
     */
    facet.setProfileDependencies(AbstractDependencyFacet.MAIN_PROFILE,
            DependencyBuilder.create(DependencyArtifact.Hsq.toString()).setScopeType("provided"));

    facet.install();
    pom = coreFacet.getModel();

    assertTrue(depFacet.hasDirectDependency(DependencyBuilder.create(DependencyArtifact.ErraiTools.toString())
            .setVersion(Property.ErraiVersion.invoke())));
    // This dependency should have been transitively included through
    // errai-tools
    assertTrue(depFacet.hasEffectiveDependency(DependencyBuilder.create(DependencyArtifact.Hsq.toString())));

    assertEquals(1, pom.getProfiles().size());
    assertEquals(2, pom.getProfiles().get(0).getDependencies().size());

    final Set<String> providedClassifiers = getProvidedClassifiers(pom);

    assertTrue(providedClassifiers.contains(DependencyArtifact.ErraiTools.toString()));
    assertTrue(providedClassifiers.contains(DependencyArtifact.Hsq.toString()));
  }

  /**
   * Check that the facet won't double add a provided scope dependency if it is
   * already in the appropriate profile of the pom.
   */
  @Test
  public void testBlacklistedDependencyNonDuplication2() throws Exception {
    final Project project = initializeJavaProject();
    final MavenFacet coreFacet = project.getFacet(MavenFacet.class);
    final DependencyFacet depFacet = project.getFacet(DependencyFacet.class);
    prepareProjectPom(project);
    Model pom = coreFacet.getModel();

    /*
     * This is what makes this test different than the last: we want to check
     * that the facet won't add a provided scoped dependency if one has been
     * added already.
     */
    final Profile profile = new Profile();
    profile.setId(AbstractDependencyFacet.MAIN_PROFILE);
    profile.addDependency(MavenConverter.convert(DependencyBuilder.create(DependencyArtifact.Hsq.toString())
            .setScopeType("provided")));

    pom.addProfile(profile);
    coreFacet.setModel(pom);

    facetFactory.install(project, BlacklistedDependencyFacet.class);
    pom = coreFacet.getModel();

    assertTrue(project.hasFacet(BlacklistedDependencyFacet.class.asSubclass(ProjectFacet.class)));
    assertTrue(depFacet.hasDirectDependency(DependencyBuilder.create(DependencyArtifact.ErraiTools.toString())
            .setVersion(Property.ErraiVersion.invoke())));
    // This dependency should have been transitively included through
    // errai-tools
    assertTrue(depFacet.hasEffectiveDependency(DependencyBuilder.create(DependencyArtifact.Hsq.toString())));

    assertEquals(1, pom.getProfiles().size());
    assertEquals(2, pom.getProfiles().get(0).getDependencies().size());

    final Set<String> providedClassifiers = getProvidedClassifiers(pom);

    assertTrue(providedClassifiers.contains(DependencyArtifact.ErraiTools.toString()));
    assertTrue(providedClassifiers.contains(DependencyArtifact.Hsq.toString()));
  }

  @Test
  public void testBlacklistedDependencyUninstall() throws Exception {
    final Project project = initializeJavaProject();
    // Use a different facet to install and uninstall because this modifies
    // internal state
    final MavenFacet coreFacet = project.getFacet(MavenFacet.class);
    final DependencyFacet depFacet = project.getFacet(DependencyFacet.class);
    prepareProjectPom(project);
    Model pom = coreFacet.getModel();

    facetFactory.install(project,
            BlacklistedDependencyFacet.class);
    pom = coreFacet.getModel();

    /*
     * Preconditions
     */
    assertTrue(project.hasFacet(BlacklistedDependencyFacet.class.asSubclass(ProjectFacet.class)));
    assertTrue(depFacet.hasDirectDependency(DependencyBuilder.create(DependencyArtifact.ErraiTools.toString())
            .setVersion(Property.ErraiVersion.invoke())));
    // This dependency should have been transitively included through
    // errai-tools
    assertTrue(depFacet.hasEffectiveDependency(DependencyBuilder.create(DependencyArtifact.Hsq.toString())));

    assertEquals(1, pom.getProfiles().size());
    assertEquals(2, pom.getProfiles().get(0).getDependencies().size());

    final Set<String> providedClassifiers = getProvidedClassifiers(pom);

    assertTrue(providedClassifiers.contains(DependencyArtifact.ErraiTools.toString()));
    assertTrue(providedClassifiers.contains(DependencyArtifact.Hsq.toString()));

    /*
     * Actual Test
     */
    final BlacklistedDependencyFacet facet = facetFactory.create(project,
            BlacklistedDependencyFacet.class);
    facet.uninstall();
    pom = coreFacet.getModel();

    assertFalse(depFacet.hasDirectDependency(DependencyBuilder.create(DependencyArtifact.ErraiTools.toString())
            .setVersion(Property.ErraiVersion.invoke())));
    assertEquals(0, pom.getProfiles().get(0).getDependencies().size());
  }

  @Test
  public void testBlacklistedDependencyIsInstalledNegative() throws Exception {
    final Project project = initializeJavaProject();
    final BlacklistedDependencyFacet testFacet = facetFactory.create(project,
            BlacklistedDependencyFacet.class);
    final MavenFacet coreFacet = project.getFacet(MavenFacet.class);
    Model pom = coreFacet.getModel();
    prepareProjectPom(project);

    final DependencyFacet depFacet = project.getFacet(DependencyFacet.class);

    /*
     * Setup
     */
    facetFactory.install(project, BlacklistedDependencyFacet.class);
    pom = coreFacet.getModel();

    assertTrue(project.hasFacet(BlacklistedDependencyFacet.class.asSubclass(ProjectFacet.class)));
    assertTrue(depFacet.hasDirectDependency(DependencyBuilder.create(DependencyArtifact.ErraiTools.toString())
            .setVersion(Property.ErraiVersion.invoke())));
    // This dependency should have been transitively included through
    // errai-tools
    assertTrue(depFacet.hasEffectiveDependency(DependencyBuilder.create(DependencyArtifact.Hsq.toString())));

    assertEquals(1, pom.getProfiles().size());
    assertEquals(2, pom.getProfiles().get(0).getDependencies().size());

    final Set<String> providedClassifiers = getProvidedClassifiers(pom);

    assertTrue(providedClassifiers.contains(DependencyArtifact.ErraiTools.toString()));
    assertTrue(providedClassifiers.contains(DependencyArtifact.Hsq.toString()));

    /*
     * Actual test
     */
    for (final Dependency dep : pom.getProfiles().get(0).getDependencies()) {
      if (DependencyArtifact.Hsq.toString().equals(dep.getGroupId() + ":" + dep.getArtifactId())) {
        pom.getProfiles().get(0).removeDependency(dep);
        break;
      }
    }
    coreFacet.setModel(pom);

    assertFalse(testFacet.isInstalled());
  }
  
  @Test
  public void testBlacklistedDependencyIsRemovedOnUninstall() throws Exception {
    final Project project = initializeJavaProject();
    final MavenFacet coreFacet = project.getFacet(MavenFacet.class);
    Model pom = coreFacet.getModel();
    prepareProjectPom(project);

    final DependencyFacet depFacet = project.getFacet(DependencyFacet.class);

    /*
     * Setup
     */
    final BlacklistedDependencyFacet blacklistedFacet = facetFactory.install(project, BlacklistedDependencyFacet.class);
    pom = coreFacet.getModel();

    assertTrue(project.hasFacet(BlacklistedDependencyFacet.class.asSubclass(ProjectFacet.class)));
    assertTrue(depFacet.hasDirectDependency(DependencyBuilder.create(DependencyArtifact.ErraiTools.toString())
            .setVersion(Property.ErraiVersion.invoke())));
    // This dependency should have been transitively included through
    // errai-tools
    assertTrue(depFacet.hasEffectiveDependency(DependencyBuilder.create(DependencyArtifact.Hsq.toString())));

    assertEquals(1, pom.getProfiles().size());
    assertEquals(2, pom.getProfiles().get(0).getDependencies().size());

    Set<String> providedClassifiers = getProvidedClassifiers(pom);

    assertTrue(providedClassifiers.contains(DependencyArtifact.ErraiTools.toString()));
    assertTrue(providedClassifiers.contains(DependencyArtifact.Hsq.toString()));

    /*
     * Actual test: uninstall facet and check that transitive dependency is also gone.
     */
    blacklistedFacet.uninstall();
    pom = coreFacet.getModel();
    providedClassifiers = getProvidedClassifiers(pom);
    
    assertEquals(Collections.emptySet(), providedClassifiers);
    assertFalse(depFacet.hasEffectiveDependency(DependencyBuilder.create(DependencyArtifact.Hsq.toString())));
  }
  
  private Set<String> getProvidedClassifiers(final Model pom) {
    final Set<String> providedClassifiers = new HashSet<String>();
    for (final Dependency provided : pom.getProfiles().get(0).getDependencies())
      providedClassifiers.add(provided.getGroupId() + ":" + provided.getArtifactId());
    
    return providedClassifiers;
  }

  private void prepareProjectPom(final Project project) {
    final MavenFacet coreFacet = project.getFacet(MavenFacet.class);
    final DependencyFacet depFacet = project.getFacet(DependencyFacet.class);
    final Model pom = coreFacet.getModel();

    pom.addProperty(Property.ErraiVersion.getName(), ForgeTest.ERRAI_TEST_VERSION);
    coreFacet.setModel(pom);

    depFacet.addDirectManagedDependency(DependencyBuilder.create("org.jboss.errai.bom:errai-bom")
            .setVersion(Property.ErraiVersion.invoke()).setScopeType("import").setPackaging("pom"));
    depFacet.addDirectManagedDependency(DependencyBuilder.create(DependencyArtifact.ErraiJboss.toString())
            .setVersion(Property.ErraiVersion.invoke()));
  }
}
