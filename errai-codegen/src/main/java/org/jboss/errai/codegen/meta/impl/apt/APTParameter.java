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
import org.jboss.errai.codegen.meta.MetaClassMember;
import org.jboss.errai.codegen.meta.MetaParameter;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Optional;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class APTParameter extends MetaParameter {

  private final VariableElement parameter;
  private final TypeMirror actualParameterType;

  public APTParameter(final VariableElement parameter, final TypeMirror actualParameterType) {
    this.parameter = parameter;
    this.actualParameterType = actualParameterType;
  }

  @Override
  public String getName() {
    return parameter.getSimpleName().toString();
  }

  @Override
  public MetaClass getType() {
    return new APTClass(actualParameterType).getErased();
  }

  @Override
  public MetaClassMember getDeclaringMember() {
    final APTClass metaClass = new APTClass(parameter.getEnclosingElement().getEnclosingElement().asType());
    final ExecutableElement method = (ExecutableElement) parameter.getEnclosingElement();

    if (method.getKind().equals(ElementKind.CONSTRUCTOR)) {
      return new APTConstructor(method, metaClass);
    } else {
      return new APTMethod(method, metaClass);
    }
  }

  @Override
  public Collection<MetaAnnotation> getAnnotations() {
    return APTClassUtil.getAnnotations(parameter);
  }

  @Override
  public Optional<MetaAnnotation> getAnnotation(final Class<? extends Annotation> annotationClass) {
    return APTClassUtil.getAnnotation(parameter, annotationClass);
  }

  @Override
  public Boolean isAnnotationPresent(final MetaClass metaClass) {
    return APTClassUtil.isAnnotationPresent(parameter, metaClass);
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

  public Element getParameter() {
    return parameter;
  }
}
