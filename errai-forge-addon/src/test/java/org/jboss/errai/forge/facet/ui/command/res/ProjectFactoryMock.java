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
