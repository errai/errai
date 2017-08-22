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

import org.jboss.errai.codegen.meta.MetaAnnotation;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaConstructor;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.codegen.meta.MetaType;
import org.jboss.errai.codegen.meta.MetaTypeVariable;
import org.jboss.errai.codegen.meta.impl.AbstractMetaClass;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Optional;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class APTConstructor extends MetaConstructor implements APTMember {

  private final ExecutableElement ctor;
  private final DeclaredType enclosedMetaObject;

  @SuppressWarnings("unchecked")
  public APTConstructor(final ExecutableElement ctor, final MetaClass metaClass) {
    this.ctor = ctor;

    //We know for sure that every MetaClass is an AbstractMetaClass
    final AbstractMetaClass<TypeMirror> abstractMetaClass = (AbstractMetaClass<TypeMirror>) metaClass;
    this.enclosedMetaObject = (DeclaredType) abstractMetaClass.getEnclosedMetaObject();
  }

  @Override
  public MetaTypeVariable[] getTypeParameters() {
    return APTClassUtil.getTypeParameters(ctor);
  }

  @Override
  public MetaClass getReturnType() {
    return new APTClass(ctor.getReturnType());
  }

  @Override
  public MetaType getGenericReturnType() {
    return APTClassUtil.fromTypeMirror(ctor.getReturnType());
  }

  @Override
  public MetaType[] getGenericParameterTypes() {
    return APTClassUtil.getGenericParameterTypes(ctor);
  }

  @Override
  public MetaParameter[] getParameters() {
    return APTClassUtil.getParameters(ctor, enclosedMetaObject);
  }

  @Override
  public MetaClass[] getCheckedExceptions() {
    return APTClassUtil.getCheckedExceptions(ctor);
  }

  @Override
  public boolean isVarArgs() {
    return ctor.isVarArgs();
  }

  @Override
  public Element getMember() {
    return ctor;
  }

  @Override
  public Optional<MetaAnnotation> getAnnotation(final Class<? extends Annotation> annotationClass) {
    return APTClassUtil.getAnnotation(ctor, annotationClass);
  }

  @Override
  public Collection<MetaAnnotation> getAnnotations() {
    return APTClassUtil.getAnnotations(ctor);
  }

  @Override
  public boolean isAnnotationPresent(final MetaClass metaClass) {
    return APTClassUtil.isAnnotationPresent(ctor, metaClass);
  }

  @Override
  public boolean unsafeIsAnnotationPresent(Class<? extends Annotation> annotation) {
    return APTClassUtil.unsafeIsAnnotationPresent();
  }

  @Override
  public Annotation[] unsafeGetAnnotations() {
    return APTClassUtil.unsafeGetAnnotations();
  }

  @Override
  public <A extends Annotation> A unsafeGetAnnotation(final Class<A> annotation) {
    return APTClassUtil.unsafeGetAnnotation();
  }

}
