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

import com.sun.tools.javac.code.Type;
import org.jboss.errai.codegen.meta.MetaAnnotation;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaType;

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Optional;

import static org.jboss.errai.codegen.meta.impl.apt.APTClassUtil.fromTypeMirror;
import static org.jboss.errai.codegen.meta.impl.apt.APTClassUtil.types;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class APTField extends MetaField implements APTMember {

  private final VariableElement field;
  private final APTClass declaringClass;

  public APTField(final VariableElement field, final APTClass declaringClass) {
    this.field = field;
    this.declaringClass = declaringClass;
  }

  @Override
  public Element getMember() {
    return field;
  }

  @Override
  public MetaClass getType() {
    final DeclaredType declaringClassType = (DeclaredType) declaringClass.getEnclosedMetaObject();
    final TypeMirror typeMirror = types.asMemberOf(declaringClassType, field);
    switch (typeMirror.getKind()) {
    case WILDCARD:
    case TYPEVAR:
      return new APTClass(((Type) typeMirror).getUpperBound());
    default:
      return new APTClass(typeMirror);
    }
  }

  @Override
  public MetaType getGenericType() {
    return fromTypeMirror(field.asType());
  }

  @Override
  public String getName() {
    return APTMember.super.getName();
  }

  @Override
  public Collection<MetaAnnotation> getAnnotations() {
    return APTMember.super.getAnnotations();
  }

  @Override
  public Optional<MetaAnnotation> getAnnotation(final Class<? extends Annotation> annotationClass) {
    return APTMember.super.getAnnotation(annotationClass);
  }
}
