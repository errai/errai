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

package org.jboss.errai.processor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * An indiscriminate dumping ground for static methods that help paper over the
 * missing functionality in the Java 6 Annotation Processing API.
 * <p>
 * Do your worst! :-)
 *
 * @author jfuerth
 */
public class AnnotationProcessors {

  /** Prevents instantiation. */
  private AnnotationProcessors() {
  }

  public static boolean hasAnnotation(final Element target, final CharSequence annotationQualifiedName) {
    return getAnnotation(target, annotationQualifiedName) != null;
  }

  public static AnnotationMirror getAnnotation(final Element target, final CharSequence annotationQualifiedName) {
    return getAnnotation(target.getAnnotationMirrors(), annotationQualifiedName);
  }

  public static AnnotationMirror getAnnotation(final List<? extends AnnotationMirror> annotationMirrors, final CharSequence annotationQualifiedName) {
    for (final AnnotationMirror am : annotationMirrors) {
      final Name annotationClassName = ((TypeElement) am.getAnnotationType().asElement()).getQualifiedName();
      if (annotationClassName.contentEquals(annotationQualifiedName)) {
        return am;
      }
    }
    return null;
  }

  /**
   * Retrieves a parameter value from an annotation that targets the given
   * element. The returned value does not take defaults into consideration.
   *
   * @param target
   *          The element targeted by an instance of the given annotation. Could
   *          be a class, field, method, or anything else.
   * @param annotationQualifiedName
   *          The fully-qualified name of the annotation to retrieve the
   *          parameter value from.
   * @param paramName
   *          the name of the annotation parameter to retrieve.
   * @return the String value of the given annotation's parameter, or null if
   *         the parameter is not present on the annotation.
   */
  static AnnotationValue getAnnotationParamValueWithoutDefaults(final Element target, final CharSequence annotationQualifiedName,
          final CharSequence paramName) {
    final AnnotationMirror templatedAnnotation = getAnnotation(target, annotationQualifiedName);
    final Map<? extends ExecutableElement, ? extends AnnotationValue> annotationParams = templatedAnnotation
            .getElementValues();
    for (final Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> param : annotationParams.entrySet()) {
      if (param.getKey().getSimpleName().contentEquals(paramName)) {
        return param.getValue();
      }
    }
    return null;
  }

  /**
   * Retrieves a string parameter value from an annotation.
   *
   * @param elements
   *          Reference to the element utilities see
   *          {@link ProcessingEnvironment#getElementUtils()}.
   * @param annotation
   *          The annotation to retrieve the parameter value from.
   * @param paramName
   *          the name of the annotation parameter to retrieve.
   * @return the String value of the given annotation's parameter, or null if
   *         the parameter is not present on the annotation.
   */
  public static String extractAnnotationStringValue(final Elements elementUtils, final AnnotationMirror annotation,
          final CharSequence paramName) {

    final AnnotationValue av = extractAnnotationPropertyValue(elementUtils, annotation, paramName);
    if (av != null && av.getValue() != null) {
      return av.getValue().toString();
    }

    return null;
  }

  public static AnnotationValue extractAnnotationPropertyValue(final Elements elementUtils, final AnnotationMirror annotation,
          final CharSequence annotationProperty) {

    final Map<? extends ExecutableElement, ? extends AnnotationValue> annotationParams = elementUtils
            .getElementValuesWithDefaults(annotation);

    for (final Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> param : annotationParams.entrySet()) {
      if (param.getKey().getSimpleName().contentEquals(annotationProperty)) {
        return param.getValue();
      }
    }
    return null;
  }

  static Optional<Element> getDeclaredField(final TypeElement classElement, final CharSequence fieldName) {
    if (fieldName.charAt(0) == '\"') {
      throw new IllegalArgumentException("given field name begins with invalid character: " + fieldName.charAt(0));
    }
    for (final Element field : ElementFilter.fieldsIn(classElement.getEnclosedElements())) {
      if (field.getSimpleName().contentEquals(fieldName)) {
        return Optional.of(field);
      }
    }
    return Optional.empty();
  }

  static Optional<Element> getField(final TypeElement classElement, final CharSequence fieldName) {
    final Optional<Element> oField = getDeclaredField(classElement, fieldName);
    if (oField.isPresent()) {
      return oField;
    }
    else {
      final TypeMirror superclass = classElement.getSuperclass();
      if (superclass instanceof DeclaredType) {
        final TypeElement superclassElement = (TypeElement) ((DeclaredType) superclass).asElement();
        return getField(superclassElement, fieldName);
      }
      else {
        return Optional.empty();
      }
    }
  }

  public static TypeElement getEnclosingTypeElement(final Element element) {
    Element currentElement = element;
    while (currentElement != null && currentElement.getKind() != ElementKind.CLASS) {
      currentElement = currentElement.getEnclosingElement();
    }

    if (currentElement == null) {
      throw new RuntimeException("No enclosing class for " + element);
    }

    return (TypeElement) currentElement;
  }

  /**
   * Returns the JavaBeans property name defined by the given method element, if
   * it does in fact define a property name. The name is calculated by stripping
   * off the prefix "is", "get", or "set" and then converting the new initial
   * character to lowercase.
   *
   * @param el
   *          The method element to extract a property name from.
   * @return the property name defined by the method according to JavaBeans
   *         convention, or null if the method does not define a JavaBeans
   *         property setter/getter.
   */
  static String propertyNameOfMethod(final Element el) {
    final Name methodName = el.getSimpleName();
    String propertyName = null;
    if (methodName.length() > 3 && "get".contentEquals(methodName.subSequence(0, 3))) {
      final StringBuilder sb = new StringBuilder(methodName.length() - 3);
      sb.append(Character.toLowerCase(methodName.charAt(3)));
      sb.append(methodName.subSequence(4, methodName.length()));
      propertyName = sb.toString();
    }
    else if (methodName.length() > 2 && "is".contentEquals(methodName.subSequence(0, 2))) {
      final StringBuilder sb = new StringBuilder(methodName.length() - 2);
      sb.append(Character.toLowerCase(methodName.charAt(2)));
      sb.append(methodName.subSequence(3, methodName.length()));
      propertyName = sb.toString();
    }
    else if (methodName.length() > 3 && "set".contentEquals(methodName.subSequence(0, 2))) {
      final StringBuilder sb = new StringBuilder(methodName.length() - 3);
      sb.append(Character.toLowerCase(methodName.charAt(3)));
      sb.append(methodName.subSequence(4, methodName.length()));
      propertyName = sb.toString();
    }
    return propertyName;
  }

  public static boolean isNativeJsType(final TypeMirror targetType, final Elements elements) {
    final AnnotationMirror am = getAnnotation(((DeclaredType) targetType).asElement(), TypeNames.JS_TYPE);
    final AnnotationValue isNativeValue = (am != null ? extractAnnotationPropertyValue(elements, am, "isNative") : null);

    return isNativeValue != null && (Boolean) isNativeValue.getValue();
  }

  public static boolean isBrowserEvent(final TypeMirror targetType, final Elements elements) {
    final AnnotationMirror am = getAnnotation(((DeclaredType) targetType).asElement(), TypeNames.BROWSER_EVENT);

    return am != null;
  }

  public static boolean isElementWrapper(final TypeMirror targetType, final Elements elements) {
    final AnnotationMirror am = getAnnotation(((DeclaredType) targetType).asElement(), TypeNames.NATIVE_ELEMENT);

    return am != null;
  }

  public static boolean isTemplated(final TypeMirror targetType, final Elements elements) {
    final AnnotationMirror am = getAnnotation(((DeclaredType) targetType).asElement(), TypeNames.TEMPLATED);

    return am != null;
  }

  public static Optional<TypeMirror> findSuperType(final TypeMirror targetType, final TypeMirror superType, final Types types) {
    final TypeMirror erasedSuperType = types.erasure(superType);
    return getAllSuperTypes(targetType, types)
            .filter(t -> types.isSameType(types.erasure(t), erasedSuperType))
            .findFirst();
  }

  public static Optional<TypeMirror> resolveSingleTypeArgumentForGenericSuperType(final TypeMirror targetType,
          final TypeMirror superTypeWithParameter, final Types types) {
    return findSuperType(targetType, superTypeWithParameter, types)
        .filter(t -> t instanceof DeclaredType)
        .map(t -> ((DeclaredType) t).getTypeArguments())
        .filter(typeArgs -> typeArgs.size() == 1)
        .map(typeArgs -> typeArgs.get(0));
  }

  public static Stream<TypeMirror> getAllSuperTypes(final TypeMirror type, final Types types) {
    return Stream.concat(Stream.of(type), types.directSupertypes(type).stream().flatMap(t -> getAllSuperTypes(t, types)));
  }
}
