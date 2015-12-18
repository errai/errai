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

package org.jboss.errai.forge.facet.aggregate;

import org.jboss.errai.forge.config.ProjectConfig;
import org.jboss.errai.forge.config.ProjectProperty;
import org.jboss.errai.forge.config.SerializableSet;
import org.jboss.errai.forge.facet.base.AbstractBaseFacet;
import org.jboss.forge.addon.facets.Facet;
import org.jboss.forge.addon.facets.MutableFacet;
import org.jboss.forge.addon.facets.MutableFaceted;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFacet;

import javax.inject.Inject;
import java.util.*;

/**
 * Acts as top-level aggregator for pulling in other facet dependencies.
 * Concrete subclasses should be used to simplify the process of installing
 * complex combinations of facets.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public abstract class BaseAggregatorFacet implements ProjectFacet, MutableFacet<Project> {

  @Override
  public void setFaceted(Project origin) {
    project = origin;
  }

  protected Project project;

  @Override
  public Project getFaceted() {
    return project;
  }

  @SuppressWarnings("serial")
  public static class UninstallationExecption extends Exception {

    private UninstallationExecption(final Class<? extends ProjectFacet> dependentFacetType, final Project project,
            final BaseAggregatorFacet toUninstall) {
      super(generateMessage(dependentFacetType, project, toUninstall));
    }

    private static String generateMessage(final Class<? extends ProjectFacet> facetType, final Project project,
            final BaseAggregatorFacet toUninstall) {
      if (BaseAggregatorFacet.class.isAssignableFrom(facetType) && project.hasFacet(facetType)) {
        final BaseAggregatorFacet facet = BaseAggregatorFacet.class.cast(project.getFacet(facetType));

        return String.format("%s (%s) still requires %s.", facet.getFeatureName(), facet.getShortName(),
                toUninstall.getFeatureName());
      }
      else {
        return String.format("The facet %s still requires %s.", facetType.getSimpleName(), toUninstall.getFeatureName());
      }
    }
  }

  @Inject
  private AggregatorFacetReflections reflections;

  @Override
  public boolean install() {
    return true;
  }

  protected Project getProject() {
    return project;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean isInstalled() {
    /*
     * An aggregator facet is installed if all of its required facets are
     * installed. There is no need to do a recursive traversal, as the presence
     * of direct dependencies in the project means that forge has already
     * verified the installation of transitively required facets.
     */
    @SuppressWarnings("rawtypes")
    final Class<? extends Facet>[] constraints = getClass().getAnnotation(FacetConstraint.class).value();
    for (int i = 0; i < constraints.length; i++) {
      if (!getProject().hasFacet((Class<? extends ProjectFacet>) constraints[i]))
        return false;
    }

    return true;
  }

  @Override
  public boolean uninstall() {
    return true;
  }

  /**
   * Uninstall this facet and all required facets which will not be otherwise
   * required after this facet is removed.
   * 
   * @return True on successful uninstallation.
   * @throws UninstallationExecption
   *           Thrown if this class is still required by another facet.
   */
  @SuppressWarnings("unchecked")
  public boolean uninstallRequirements() throws UninstallationExecption, IllegalStateException {
    final ProjectConfig config = getProject().getFacet(ProjectConfig.class);
    final SerializableSet installedFeatureNames = config.getProjectProperty(ProjectProperty.INSTALLED_FEATURES,
            SerializableSet.class);
    final Set<Class<? extends ProjectFacet>> directlyInstalled = new HashSet<Class<? extends ProjectFacet>>();

    for (final String featureName : installedFeatureNames) {
      directlyInstalled.add(reflections.getFeatureShort(featureName).getFeatureClass());
    }
    directlyInstalled.remove(getClass());
    directlyInstalled.add(CoreFacet.class);
    directlyInstalled.addAll(Arrays.asList(CoreFacet.coreFacets));

    final Set<Class<? extends ProjectFacet>> toUninstall = traverseUninstallable(directlyInstalled);

    keepRequired(directlyInstalled, toUninstall);

    for (final Class<? extends ProjectFacet> facetType : toUninstall) {
      if (getProject().hasFacet(facetType)) {
        if (getProject() instanceof MutableFaceted)
          ((MutableFaceted<ProjectFacet>) getProject()).uninstall(getProject().getFacet(facetType));
        else
          throw new IllegalStateException(String.format(
                  "Cannot uninstall facets from project type %s that does not implement %s", getProject().getClass()
                          .getCanonicalName(), MutableFaceted.class.getCanonicalName()));
      }
    }

    return true;
  }

  /**
   * Traverse the required facets of the featureClasses, and remove all of the
   * traversed facets from the removable set.
   * 
   * @throws UninstallationExecption
   *           Thrown if this feature is still required by another facet.
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  private void keepRequired(final Collection<Class<? extends ProjectFacet>> featureClasses,
          final Set<Class<? extends ProjectFacet>> removable) throws UninstallationExecption {
    final Set<Class<? extends ProjectFacet>> traversed = new HashSet<Class<? extends ProjectFacet>>();
    final Queue<Class<? extends ProjectFacet>> toVisit = new LinkedList<Class<? extends ProjectFacet>>();
    toVisit.addAll(featureClasses);

    while (!toVisit.isEmpty()) {
      final Class<? extends ProjectFacet> cur = toVisit.poll();
      if (!traversed.contains(cur)) {
        traversed.add(cur);

        if (cur.isAnnotationPresent(FacetConstraint.class)) {
          final Class<? extends Facet>[] requirements = cur.getAnnotation(FacetConstraint.class).value();
          for (int i = 0; i < requirements.length; i++) {
            if (!traversed.contains(requirements[i])) {
              // Some other feature still depends on this class...
              if (requirements[i].equals(getClass()))
                throw new UninstallationExecption(cur, getProject(), this);

              toVisit.add((Class<? extends ProjectFacet>) requirements[i]);
              removable.remove(requirements[i]);
            }
          }
        }
      }
    }
  }

  /**
   * Traverse the required facets of this class and add them to collection. But
   * ignore required facets in the intentionally installed facet.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  private Set<Class<? extends ProjectFacet>> traverseUninstallable(
          final Set<Class<? extends ProjectFacet>> intentionallyInstalled) {
    final Set<Class<? extends ProjectFacet>> traversed = new HashSet<Class<? extends ProjectFacet>>();

    final Queue<Class<? extends ProjectFacet>> toVisit = new LinkedList<Class<? extends ProjectFacet>>();
    toVisit.add((Class<? extends ProjectFacet>) getClass());

    while (!toVisit.isEmpty()) {
      final Class<? extends ProjectFacet> cur = toVisit.poll();
      if (!traversed.contains(cur)
              && !intentionallyInstalled.contains(cur)
              // Only add errai facets to be uninstalled
              && (BaseAggregatorFacet.class.isAssignableFrom(cur) || AbstractBaseFacet.class.isAssignableFrom(cur))) {
        traversed.add(cur);

        if (cur.isAnnotationPresent(FacetConstraint.class)) {
          final Class<? extends Facet>[] requirements = cur.getAnnotation(FacetConstraint.class).value();
          for (int i = 0; i < requirements.length; i++) {
            if (!traversed.contains(requirements[i])) {
              toVisit.add((Class<? extends ProjectFacet>) requirements[i]);
            }
          }
        }
      }
    }

    return traversed;
  }

  /**
   * @return The name of the feature managed by this facet.
   */
  public abstract String getFeatureName();

  /**
   * @return The short name of the feature managed by this facet, used for
   *         referencing it through the shell.
   */
  public abstract String getShortName();

  /**
   * @return A short description of the feature managed by this facet.
   */
  public abstract String getFeatureDescription();
}
