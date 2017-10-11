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

package org.jboss.errai.ioc.rebind.ioc.graph.impl;

import org.jboss.errai.codegen.meta.HasAnnotations;
import org.jboss.errai.codegen.meta.MetaAnnotation;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.graph.api.DependencyGraphBuilder;
import org.jboss.errai.ioc.rebind.ioc.graph.api.DependencyGraphBuilder.Dependency;
import org.jboss.errai.ioc.rebind.ioc.graph.api.DependencyGraphBuilder.DependencyType;
import org.jboss.errai.ioc.rebind.ioc.graph.api.Injectable;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Optional;

/**
 * Base implementation for all {@link Dependency dependencies}.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
abstract class BaseDependency implements Dependency {
  InjectableReference injectable;
  final DependencyType dependencyType;

  BaseDependency(final InjectableReference abstractInjectable, final DependencyType dependencyType) {
    this.injectable = abstractInjectable;
    this.dependencyType = dependencyType;
  }

  @Override
  public String toString() {
    return "[dependencyType=" + dependencyType.toString() + ", reference=" + injectable.toString() + "]";
  }

  @Override
  public Injectable getInjectable() {
    return injectable.resolution;
  }

  @Override
  public DependencyType getDependencyType() {
    return dependencyType;
  }

  @Override
  public Annotation[] unsafeGetAnnotations() {
    return getAnnotated().unsafeGetAnnotations();
  }

  @Override
  public Optional<MetaAnnotation> getAnnotation(final Class<? extends Annotation> annotationClass) {
    return getAnnotated().getAnnotation(annotationClass);
  }

  @Override
  public Boolean isAnnotationPresent(final MetaClass metaClass) {
    return getAnnotated().isAnnotationPresent(metaClass);
  }

  @Override
  public Collection<MetaAnnotation> getAnnotations() {
    return getAnnotated().getAnnotations();
  }

  @Override
  public Boolean isAnnotationPresent(final Class<? extends Annotation> annotation) {
    return getAnnotated().isAnnotationPresent(annotation);
  }

  protected abstract HasAnnotations getAnnotated();

  static BaseDependency as(final Dependency dep) {
    if (dep instanceof BaseDependency) {
      return (BaseDependency) dep;
    } else {
      throw new RuntimeException("Dependency was not an instance of " + BaseDependency.class.getSimpleName()
              + ". Make sure you only create dependencies using methods using the "
              + DependencyGraphBuilder.class.getSimpleName() + ".");
    }
  }
}
