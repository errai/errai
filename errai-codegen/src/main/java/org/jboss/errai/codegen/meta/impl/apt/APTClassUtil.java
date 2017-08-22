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
import com.sun.tools.javac.util.List;
import org.jboss.errai.codegen.meta.MetaAnnotation;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.codegen.meta.MetaType;
import org.jboss.errai.codegen.meta.MetaTypeVariable;
import org.jboss.errai.codegen.meta.impl.AbstractMetaClass;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Parameterizable;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
public final class APTClassUtil {

  public static Types types;
  public static Elements elements;

  private APTClassUtil() {
  }

  public static void init(final Types types, final Elements elements) {
    APTClassUtil.types = types;
    APTClassUtil.elements = elements;
  }

  static MetaType fromTypeMirror(final TypeMirror mirror) {
    switch (mirror.getKind()) {
    case BOOLEAN:
    case BYTE:
    case CHAR:
    case DOUBLE:
    case FLOAT:
    case INT:
    case LONG:
    case SHORT:
    case VOID:
      return new APTClass(mirror);
    case DECLARED:
      final DeclaredType dType = (DeclaredType) mirror;
      if (dType.getTypeArguments().isEmpty()) {
        return new APTClass(dType);
      } else {
        return new APTParameterizedType(dType);
      }
    case TYPEVAR:
      return new APTMetaTypeVariable((TypeParameterElement) ((TypeVariable) mirror).asElement());
    case WILDCARD:
      return new APTWildcardType((WildcardType) mirror);
    default:
      throw new UnsupportedOperationException(
              format("Don't know how to get a MetaType for %s [%s].", mirror.getKind(), mirror));
    }
  }

  static MetaTypeVariable[] getTypeParameters(final Parameterizable target) {
    return target.getTypeParameters().stream().map(APTMetaTypeVariable::new).toArray(MetaTypeVariable[]::new);
  }

  static MetaParameter[] getParameters(final ExecutableElement target, DeclaredType enclosedMetaObject) {

    final TypeMirror typeMirror = types.asMemberOf(enclosedMetaObject, target);

    if (typeMirror instanceof Type.MethodType) {
      final AtomicInteger i = new AtomicInteger(0);
      final List<Type> parameterTypes = ((Type.MethodType) typeMirror).getParameterTypes();
      return target.getParameters()
              .stream()
              .map(parameter -> new APTParameter(parameter, parameterTypes.get(i.getAndIncrement())))
              .toArray(MetaParameter[]::new);
    }

    return target.getParameters()
            .stream()
            .map(parameter -> new APTParameter(parameter, parameter.asType()))
            .toArray(MetaParameter[]::new);
  }

  static MetaType[] getGenericParameterTypes(final ExecutableElement target) {
    return target.getParameters()
            .stream()
            .map(Element::asType)
            .map(APTClassUtil::fromTypeMirror)
            .toArray(MetaType[]::new);
  }

  static MetaClass[] getCheckedExceptions(final ExecutableElement target) {
    return target.getThrownTypes().stream().map(APTClass::new).toArray(APTClass[]::new);
  }

  static <T> T throwUnsupportedTypeError(final TypeMirror type) {
    throw new UnsupportedOperationException(format("Unsupported TypeMirror %s [%s].", type.getKind(), type));
  }

  static boolean sameTypes(final Iterator<? extends TypeMirror> iter1, final Iterator<? extends TypeMirror> iter2) {
    while (iter1.hasNext() && iter2.hasNext()) {
      if (!APTClassUtil.types.isSameType(iter1.next(), iter2.next())) {
        return false;
      }
    }

    return iter1.hasNext() == iter2.hasNext();
  }

  static String getSimpleName(final TypeMirror mirror) {
    switch (mirror.getKind()) {
    case DECLARED:
    case TYPEVAR:
      final Element element = types.asElement(mirror);
      return element.getSimpleName().toString();
    case ARRAY:
      return getSimpleName(((ArrayType) mirror).getComponentType()) + "[]";
    case BOOLEAN:
    case BYTE:
    case CHAR:
    case DOUBLE:
    case FLOAT:
    case INT:
    case LONG:
    case SHORT:
    case VOID:
      return mirror.getKind().toString().toLowerCase();
    default:
      return throwUnsupportedTypeError(mirror);
    }
  }

  public static Collection<MetaAnnotation> getAnnotations(final Element element) {
    return element.getAnnotationMirrors().stream().map(APTAnnotation::new).collect(toSet());
  }

  public static Collection<MetaAnnotation> getAnnotations(final TypeMirror mirror) {
    return getAnnotations(types.asElement(mirror));
  }

  public static Optional<MetaAnnotation> getAnnotation(final Element element,
          final Class<? extends Annotation> annotationClass) {

    return element.getAnnotationMirrors()
            .stream()
            .filter(s -> s.getAnnotationType().toString().equals(annotationClass.getCanonicalName()))
            .map(annotationMirror -> (MetaAnnotation) new APTAnnotation(annotationMirror))
            .findFirst();
  }

  public static Optional<MetaAnnotation> getAnnotation(final TypeMirror typeMirror,
          final Class<? extends Annotation> annotationClass) {
    return getAnnotation(types.asElement(typeMirror), annotationClass);
  }

  public static boolean isAnnotationPresent(final Element element, final MetaClass annotationMetaClass) {
    return element.getAnnotationMirrors()
            .stream()
            .map(annotationMirror -> annotationMirror.getAnnotationType().toString())
            .anyMatch(
                    type -> type.equals(((AbstractMetaClass) annotationMetaClass).getEnclosedMetaObject().toString()));
  }

  @Deprecated
  public static Annotation[] unsafeGetAnnotations() {
    throw new RuntimeException("Unsafe methods should not be called in APT environment");
  }

  @Deprecated
  public static Class<?> unsafeAsClass() {
    throw new RuntimeException("Unsafe methods should not be called in APT environment");
  }

  @Deprecated
  public static <A extends Annotation> A unsafeGetAnnotation() {
    throw new RuntimeException("Unsafe methods should not be called in APT environment");
  }

  @Deprecated
  public static boolean unsafeIsAnnotationPresent() {
    throw new RuntimeException("Unsafe methods should not be called in APT environment");
  }
}
