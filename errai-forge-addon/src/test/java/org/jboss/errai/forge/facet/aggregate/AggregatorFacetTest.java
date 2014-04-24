package org.jboss.errai.forge.facet.aggregate;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import javax.inject.Inject;

import org.jboss.errai.forge.config.ProjectConfig;
import org.jboss.errai.forge.config.ProjectProperty;
import org.jboss.errai.forge.config.SerializableSet;
import org.jboss.errai.forge.facet.aggregate.BaseAggregatorFacet.UninstallationExecption;
import org.jboss.errai.forge.test.base.ForgeTest;
import org.jboss.forge.addon.facets.Facet;
import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.facets.MutableFaceted;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFacet;
import org.junit.Test;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class AggregatorFacetTest extends ForgeTest {

  @Inject
  private FacetFactory facetFactory;

  @Test
  public void testUninstallRemovesConstraints() throws Exception {
    final Project project = createTestProject();

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
    final Project project = createTestProject();

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
    final Project project = createTestProject();

    addFeature(project, ErraiIocFacet.class);
    addFeature(project, ErraiUiFacet.class);

    assertTrue(removeFeature(project, ErraiUiFacet.class));

    assertTrue("ErraiIocFacet was uninstalled.", project.hasFacet(ErraiIocFacet.class));
    assertTrue("ErraiIocFacet was uninstalled.", facetFactory.create(project, ErraiIocFacet.class).isInstalled());
  }

  @Test
  public void testUninstallDoesRemoveOtherNotDirectlyInstalled() throws Exception {
    final Project project = createTestProject();
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
    final Project project = createTestProject();

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
  
  private Project createTestProject() {
    final Project project = initializeJavaProject();
    final ProjectConfig projectConfig = facetFactory.install(project, ProjectConfig.class);

    projectConfig.setProjectProperty(ProjectProperty.ERRAI_VERSION, "3.0-SNAPSHOT");
    projectConfig.setProjectProperty(ProjectProperty.MODULE_LOGICAL, "org.jboss.errai.ForgeTest");
    projectConfig.setProjectProperty(ProjectProperty.MODULE_FILE, new File(project.getRootDirectory()
            .getUnderlyingResourceObject(), "src/main/java/org/jboss/errai/ForgeTest.gwt.xml"));
    projectConfig.setProjectProperty(ProjectProperty.MODULE_NAME, "test");
    projectConfig.setProjectProperty(ProjectProperty.INSTALLED_FEATURES, new SerializableSet());

    return project;
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
