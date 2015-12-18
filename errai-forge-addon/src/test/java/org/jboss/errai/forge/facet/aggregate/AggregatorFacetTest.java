/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.forge.facet.aggregate;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
import org.jboss.errai.forge.config.ProjectConfig;
import org.jboss.errai.forge.config.ProjectProperty;
import org.jboss.errai.forge.config.SerializableSet;
import org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact;
import org.jboss.errai.forge.facet.aggregate.BaseAggregatorFacet.UninstallationExecption;
import org.jboss.errai.forge.facet.base.AbstractBaseFacet;
import org.jboss.errai.forge.test.base.ForgeTest;
import org.jboss.forge.addon.facets.Facet;
import org.jboss.forge.addon.facets.MutableFaceted;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.maven.projects.MavenFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFacet;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class AggregatorFacetTest extends ForgeTest {

  @Test
  public void testUninstallRemovesConstraints() throws Exception {
    final Project project = createErraiTestProject();

    addFeature(project, ErraiIocFacet.class);
    assertTrue(removeFeature(project, ErraiIocFacet.class));

    final Set<Class<? extends Facet>> coreFacetTypes = getCoreFacetConstraints();

    for (final Class<? extends Facet> facetType : ErraiIocFacet.class.getAnnotation(FacetConstraint.class).value()) {
      if (!coreFacetTypes.contains(facetType)) {
        assertFalse(facetType.getSimpleName() + " was not uninstalled.",
                project.hasFacet((Class<? extends ProjectFacet>) facetType));
        assertFalse(facetType.getSimpleName() + " was not uninstalled.", facetFactory.create(project, facetType)
                .isInstalled());
      }
      else {
        assertTrue(facetType.getSimpleName() + " was uninstalled.",
                project.hasFacet((Class<? extends ProjectFacet>) facetType));
        assertTrue(facetType.getSimpleName() + " was uninstalled.", facetFactory.create(project, facetType)
                .isInstalled());
      }
    }
  }

  @Test
  public void testAggregatorDoesNotUninstallCore() throws Exception {
    final Project project = createErraiTestProject();

    final CoreFacet coreFacet = facetFactory.install(project, CoreFacet.class);
    addFeature(project, ErraiMessagingFacet.class);

    // Precondition
    assertTrue(removeFeature(project, ErraiMessagingFacet.class));

    // Actual test
    assertTrue(project.hasFacet(CoreFacet.class));
    assertTrue(coreFacet.isInstalled());

    for (final Class<? extends ProjectFacet> facetType : CoreFacet.coreFacets) {
      assertTrue(facetType.getSimpleName() + " was uninstalled.", project.hasFacet(facetType));
      assertTrue(facetType.getSimpleName() + " was uninstalled.", facetFactory.create(project, facetType).isInstalled());
    }

    for (final Class<? extends ProjectFacet> facetType : (Class<? extends ProjectFacet>[]) CoreFacet.class
            .getAnnotation(FacetConstraint.class).value()) {
      assertTrue(facetType.getSimpleName() + " was uninstalled.", project.hasFacet(facetType));
      assertTrue(facetType.getSimpleName() + " was uninstalled.", facetFactory.create(project, facetType).isInstalled());
    }
  }

  @Test
  public void testUninstallDoesNotRemoveOtherDirectlyInstalled() throws Exception {
    final Project project = createErraiTestProject();

    addFeature(project, ErraiIocFacet.class);
    addFeature(project, ErraiUiFacet.class);

    assertTrue(removeFeature(project, ErraiUiFacet.class));

    assertTrue("ErraiIocFacet was uninstalled.", project.hasFacet(ErraiIocFacet.class));
    assertTrue("ErraiIocFacet was uninstalled.", facetFactory.create(project, ErraiIocFacet.class).isInstalled());
  }

  @Test
  public void testUninstallDoesRemoveOtherNotDirectlyInstalled() throws Exception {
    final Project project = createErraiTestProject();
    addFeature(project, ErraiUiFacet.class);
    final ErraiIocFacet iocFacet = project.getFacet(ErraiIocFacet.class);

    // Precondition
    assertTrue(iocFacet.isInstalled());

    assertTrue(removeFeature(project, ErraiUiFacet.class));
    assertFalse(project.hasFacet(ErraiIocFacet.class));
    assertFalse(iocFacet.isInstalled());
  }
  
  @Test
  public void testCanInstallThenUninstallTwoRelatedFeatures() throws Exception {
    final Project project = createErraiTestProject();

    final ErraiIocFacet iocFacet = addFeature(project, ErraiIocFacet.class);
    final ErraiUiFacet uiFacet = addFeature(project, ErraiUiFacet.class);

    assertTrue(removeFeature(project, ErraiUiFacet.class));

    assertTrue("ErraiIocFacet was uninstalled.", project.hasFacet(ErraiIocFacet.class));
    assertTrue("ErraiIocFacet was uninstalled.", iocFacet.isInstalled());
    assertFalse("ErraiUiFacet was not uninstalled.", project.hasFacet(ErraiUiFacet.class));
    assertFalse("ErraiUiFacet was not uninstalled.", uiFacet.isInstalled());
    
    assertTrue(removeFeature(project, ErraiIocFacet.class));
    
    assertFalse("ErraiUiFacet was not uninstalled.", project.hasFacet(ErraiUiFacet.class));
    assertFalse("ErraiUiFacet was not uninstalled.", uiFacet.isInstalled());
    assertFalse("ErraiIocFacet was not uninstalled.", project.hasFacet(ErraiIocFacet.class));
    assertFalse("ErraiIocFacet was not uninstalled.", iocFacet.isInstalled());
  }
  
  @Test
  public void regressionTestForErrai726() throws Exception {
    final Project project = createErraiTestProject();

    final ErraiIocFacet iocFacet = addFeature(project, ErraiIocFacet.class);
    final ErraiUiFacet uiFacet = addFeature(project, ErraiUiFacet.class);

    assertTrue(removeFeature(project, ErraiUiFacet.class));

    // Preconditions
    assertTrue("ErraiIocFacet was uninstalled.", project.hasFacet(ErraiIocFacet.class));
    assertTrue("ErraiIocFacet was uninstalled.", iocFacet.isInstalled());
    assertFalse("ErraiUiFacet was not uninstalled.", project.hasFacet(ErraiUiFacet.class));
    assertFalse("ErraiUiFacet was not uninstalled.", uiFacet.isInstalled());
    
    final MavenFacet mavenFacet = project.getFacet(MavenFacet.class);
    final Model pom = mavenFacet.getModel();
    Profile profile = null;
    for (final Profile p : pom.getProfiles()) {
      if (p.getId() != null && p.getId().equals(AbstractBaseFacet.MAIN_PROFILE)) {
        profile = p;
        break;
      }
    }
    
    assertNotNull(profile);
    
    for (final Dependency dep : profile.getDependencies()) {
      assertNotEquals(DependencyArtifact.ErraiUi.getArtifactId(), dep.getArtifactId());
      assertNotEquals(DependencyArtifact.ErraiDataBinding.getArtifactId(), dep.getArtifactId());
    }
  }
  
  private boolean removeFeature(final Project project, Class<? extends BaseAggregatorFacet> featureType) throws IllegalStateException, UninstallationExecption {
    final BaseAggregatorFacet facet = project.getFacet(featureType);

    final ProjectConfig projectConfig = project.getFacet(ProjectConfig.class);
    final SerializableSet installedFeatures = projectConfig.getProjectProperty(ProjectProperty.INSTALLED_FEATURES,
            SerializableSet.class);
    installedFeatures.remove(facet.getShortName());
    projectConfig.setProjectProperty(ProjectProperty.INSTALLED_FEATURES, installedFeatures);
    
    boolean success = facet.uninstallRequirements();
    
    if (!success)
      return false;
    
    success = ((MutableFaceted<ProjectFacet>)project).uninstall(facet);
    
    return success;
  }

  private <T extends BaseAggregatorFacet> T addFeature(final Project project, Class<T> facetType) {
    final T aggregatorFacet = facetFactory.install(project, facetType);
    final ProjectConfig projectConfig = project.getFacet(ProjectConfig.class);

    final SerializableSet installedFeatures = projectConfig.getProjectProperty(ProjectProperty.INSTALLED_FEATURES,
            SerializableSet.class);
    installedFeatures.add(aggregatorFacet.getShortName());
    projectConfig.setProjectProperty(ProjectProperty.INSTALLED_FEATURES, installedFeatures);

    return aggregatorFacet;
  }
  
  private Set<Class<? extends Facet>> getCoreFacetConstraints() {
    final Set<Class<? extends Facet>> coreFacetTypes = new HashSet();
    coreFacetTypes.add(CoreFacet.class);

    final Queue<Class<? extends Facet>> toVisit = new LinkedList<Class<? extends Facet>>();
    toVisit.addAll((Collection) Arrays.asList(CoreFacet.class.getAnnotation(FacetConstraint.class).value()));
    toVisit.addAll(Arrays.asList(CoreFacet.coreFacets));

    while (!toVisit.isEmpty()) {
      final Class<? extends Facet> facetType = toVisit.poll();
      if (facetType.isAnnotationPresent(FacetConstraint.class)) {
        for (final Class<? extends Facet> foundFacetType : facetType.getAnnotation(FacetConstraint.class).value()) {
          if (!coreFacetTypes.contains(foundFacetType)) {
            toVisit.add(foundFacetType);
          }
        }
      }
      coreFacetTypes.add(facetType);
    }

    return coreFacetTypes;
  }

}
