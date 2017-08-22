/*
 * Copyright (C) 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.codegen.meta.impl.apt;

import org.jboss.errai.codegen.meta.HasMetaAnnotations;
import org.jboss.errai.codegen.meta.HasAnnotations;
import org.jboss.errai.codegen.meta.MetaAnnotation;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassMember;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Optional;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
interface APTMember extends MetaClassMember, HasAnnotations, HasMetaAnnotations {
  Element getMember();

  @Override
  default String getName() {
    return getMember().getSimpleName().toString();
  }

  @Override
  default MetaClass getDeclaringClass() {
    return new APTClass(getMember().getEnclosingElement().asType());
  }

  @Override
  default String getDeclaringClassName() {
    return ((TypeElement) getMember().getEnclosingElement()).getQualifiedName().toString();
  }

  @Override
  default boolean isAbstract() {
    return getMember().getModifiers().contains(Modifier.ABSTRACT);
  }

  @Override
  default boolean isPublic() {
    return getMember().getModifiers().contains(Modifier.PUBLIC);
  }

  @Override
  default boolean isPrivate() {
    return getMember().getModifiers().contains(Modifier.PRIVATE);
  }

  @Override
  default boolean isProtected() {
    return getMember().getModifiers().contains(Modifier.PROTECTED);
  }

  @Override
  default boolean isFinal() {
    return getMember().getModifiers().contains(Modifier.FINAL);
  }

  @Override
  default boolean isStatic() {
    return getMember().getModifiers().contains(Modifier.STATIC);
  }

  @Override
  default boolean isTransient() {
    return getMember().getModifiers().contains(Modifier.TRANSIENT);
  }

  @Override
  default boolean isSynthetic() {
    // TODO Verify that APT does not expose synthetic members
    return false;
  }

  @Override
  default boolean isVolatile() {
    return getMember().getModifiers().contains(Modifier.VOLATILE);
  }

  @Override
  default boolean isSynchronized() {
    return getMember().getModifiers().contains(Modifier.SYNCHRONIZED);
  }

  @Override
  default Collection<MetaAnnotation> getAnnotations() {
    return APTClassUtil.getAnnotations(getMember());
  }

  @Override
  default Optional<MetaAnnotation> getAnnotation(final Class<? extends Annotation> annotationClass) {
    return APTClassUtil.getAnnotation(getMember(), annotationClass);
  }

  @Override
  default boolean isAnnotationPresent(final MetaClass metaClass) {
    return APTClassUtil.isAnnotationPresent(getMember(), metaClass);
  }

  @Override
  default boolean unsafeIsAnnotationPresent(Class<? extends Annotation> annotation) {
    return APTClassUtil.unsafeIsAnnotationPresent();
  }

  @Override
  default Annotation[] unsafeGetAnnotations() {
    return APTClassUtil.unsafeGetAnnotations();
  }

  @Override
  default <A extends Annotation> A unsafeGetAnnotation(final Class<A> annotation) {
    return APTClassUtil.unsafeGetAnnotation();
  }
}
