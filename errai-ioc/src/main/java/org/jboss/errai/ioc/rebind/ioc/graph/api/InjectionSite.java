/**
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

package org.jboss.errai.ioc.rebind.ioc.graph.api;

import org.jboss.errai.codegen.meta.HasAnnotations;
import org.jboss.errai.codegen.meta.MetaAnnotation;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassMember;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaParameter;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Contains metadata for a single injection point.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class InjectionSite implements HasAnnotations {

  private final MetaClass enclosingType;
  private final HasAnnotations annotated;
  private final Collection<Injectable> otherResolvedInjectables;

  public InjectionSite(final MetaClass enclosingType, final HasAnnotations annotated, final Collection<Injectable> otherResolvedInjectables) {
    this.enclosingType = enclosingType;
    this.annotated = annotated;
    this.otherResolvedInjectables = Collections.unmodifiableCollection(otherResolvedInjectables);
  }

  private String annotatedName() {
    if (annotated instanceof MetaClassMember) {
      return ((MetaClassMember) annotated).getDeclaringClassName() + "_" + ((MetaClassMember) annotated).getName();
    } else if (annotated instanceof MetaParameter) {
      final MetaClassMember declaringMember = ((MetaParameter) annotated).getDeclaringMember();
      return declaringMember.getDeclaringClassName() + "_" + declaringMember.getName() + "_" + ((MetaParameter) annotated).getName();
    } else {
      throw new RuntimeException("Not yet implemented!");
    }
  }

  /**
   * @return A unique name for this injection site.
   */
  public String getUniqueName() {
    return enclosingType.getName() + "_" + annotatedName();
  }

  /**
   * @return The enclosing type for this injection site.
   */
  public MetaClass getEnclosingType() {
    return enclosingType;
  }

  @Override
  @Deprecated
  public Annotation[] unsafeGetAnnotations() {
    return annotated.unsafeGetAnnotations();
  }

  @Override
  @Deprecated
  public boolean unsafeIsAnnotationPresent(final Class<? extends Annotation> annotation) {
    return annotated.unsafeIsAnnotationPresent(annotation);
  }

  @Override
  public <A extends Annotation> A unsafeGetAnnotation(final Class<A> annotation) {
    return annotated.unsafeGetAnnotation(annotation);
  }

  @Override
  public Optional<MetaAnnotation> getAnnotation(final Class<? extends Annotation> annotationClass) {
    return annotated.getAnnotation(annotationClass);
  }

  @Override
  public Boolean isAnnotationPresent(final MetaClass metaClass) {
    return annotated.isAnnotationPresent(metaClass);
  }

  @Override
  public Collection<MetaAnnotation> getAnnotations() {
    return annotated.getAnnotations();
  }

  @Override
  public Boolean isAnnotationPresent(final Class<? extends Annotation> annotationClass) {
    return annotated.isAnnotationPresent(annotationClass);
  }

  /**
   * @return The exact type of this injection site.
   */
  public MetaClass getExactType() {
    if (annotated instanceof MetaField) {
      return ((MetaField) annotated).getType();
    } else if (annotated instanceof MetaParameter) {
      return ((MetaParameter) annotated).getType();
    } else {
      throw new RuntimeException("Not yet implemented for annotated of type " + annotated.getClass().getName());
    }
  }

  /**
   * This method exists primarily to allow extensions to give more useful error messages.
   *
   * @return A collection of other {@link Injectable injectables} that were
   *         resolved for this site but were of lower priority.
   */
  public Collection<Injectable> getOtherResolvedInjectables() {
    return otherResolvedInjectables;
  }

}