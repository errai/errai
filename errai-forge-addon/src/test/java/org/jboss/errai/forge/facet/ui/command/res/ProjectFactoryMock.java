package org.jboss.errai.forge.facet.ui.command.res;

import org.jboss.forge.addon.projects.*;
import org.jboss.forge.addon.resource.Resource;
import org.jboss.forge.furnace.spi.ListenerRegistration;
import org.jboss.forge.furnace.util.Predicate;

import javax.enterprise.inject.Alternative;

/**
 * Returns {@link #project} for all methods that return a {@link Project}.
 */
@Alternative
public class ProjectFactoryMock implements ProjectFactory {
  
  public Project project;

  public ProjectFactoryMock(final Project project) {
    this.project = project;
  }

  @Override
  public Project findProject(Resource<?> target) {
    return project;
  }

  @Override
  public Project findProject(Resource<?> target, Predicate<Project> filter) {
    return project;
  }

  @Override
  public Project findProject(Resource<?> target, ProjectProvider projectProvider) {
    return project;
  }

  @Override
  public Project findProject(Resource<?> target, ProjectProvider projectProvider, Predicate<Project> filter) {
    return project;
  }

  @Override
  public void invalidateCaches() {
  }

  @Override
  public Project createProject(Resource<?> projectDir, ProjectProvider buildSystem) {
    return project;
  }

  @Override
  public Project createProject(Resource<?> targetDir, ProjectProvider projectProvider,
          Iterable<Class<? extends ProjectFacet>> facetTypes) {
    return project;
  }

  @Override
  public boolean containsProject(Resource<?> target) {
    return false;
  }

  @Override
  public boolean containsProject(Resource<?> target, ProjectProvider projectProvider) {
    return false;
  }

  @Override
  public boolean containsProject(Resource<?> bound, Resource<?> target) {
    return false;
  }

  @Override
  public boolean containsProject(Resource<?> bound, Resource<?> target, ProjectProvider projectProvider) {
    return false;
  }

  @Override
  public Project createTempProject() throws IllegalStateException {
    return project;
  }

  @Override
  public Project createTempProject(Iterable<Class<? extends ProjectFacet>> facetTypes) throws IllegalStateException {
    return project;
  }

  @Override
  public Project createTempProject(ProjectProvider projectProvider) {
    return project;
  }

  @Override
  public Project createTempProject(ProjectProvider projectProvider, Iterable<Class<? extends ProjectFacet>> facetTypes) {
    return project;
  }

  @Override
  public ListenerRegistration<ProjectListener> addProjectListener(ProjectListener listener) {
    return null;
  }

}
