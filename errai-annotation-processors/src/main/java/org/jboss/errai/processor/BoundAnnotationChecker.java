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

import static org.jboss.errai.processor.AnnotationProcessors.extractAnnotationStringValue;
import static org.jboss.errai.processor.AnnotationProcessors.getAnnotation;
import static org.jboss.errai.processor.AnnotationProcessors.hasAnnotation;
import static org.jboss.errai.processor.AnnotationProcessors.isElementWrapper;
import static org.jboss.errai.processor.AnnotationProcessors.isNativeJsType;
import static org.jboss.errai.processor.AnnotationProcessors.propertyNameOfMethod;
import static org.jboss.errai.processor.AnnotationProcessors.resolveSingleTypeArgumentForGenericSuperType;
import static org.jboss.errai.processor.TypeNames.GWT_ELEMENT;
import static org.jboss.errai.processor.TypeNames.LIST_CHANGE_HANDLER;
import static org.jboss.errai.processor.TypeNames.NATIVE_HAS_VALUE;
import static org.jboss.errai.processor.TypeNames.TAKES_VALUE;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

/**
 * Evaluates usage of the ErraiUI DataField annotation and emits errors and warnings when
 * the annotation is not being used correctly.
 */
@SupportedAnnotationTypes(TypeNames.BOUND)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class BoundAnnotationChecker extends AbstractProcessor {

  @Override
  public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
    final Types types = processingEnv.getTypeUtils();
    final Elements elements = processingEnv.getElementUtils();
    final TypeMirror gwtWidgetType = elements.getTypeElement(TypeNames.GWT_WIDGET).asType();
    final TypeMirror gwtElementType = elements.getTypeElement(TypeNames.GWT_ELEMENT).asType();
    final TypeMirror listChangeHandlerType = elements.getTypeElement(TypeNames.LIST_CHANGE_HANDLER).asType();

    final Map<TypeElement, List<Element>> classesWithBoundThings =
            annotations
            .stream()
            .flatMap(anno -> roundEnv.getElementsAnnotatedWith(anno).stream())
            .peek(target -> validateBoundType(types, elements, gwtWidgetType, gwtElementType, listChangeHandlerType, target))
            .collect(Collectors.groupingBy(AnnotationProcessors::getEnclosingTypeElement));

    classesWithBoundThings
      .entrySet()
      .stream()
      .forEach(entry -> {
        final List<TypeMirror> modelTypes = findAllModelTypes(entry.getKey());
        if (modelTypes.size() == 0) {
          printMissingModelErrorsForBoundElements(entry.getValue());
        }
        else if (modelTypes.size() > 1) {
          // TODO mark everything annotated with @AutoBound or @Model with an error
        }
        else {
          validateBoundPropertyChains(elements, entry.getValue(), modelTypes.get(0));
        }
      });

    return false;
  }

  private void validateBoundPropertyChains(final Elements elements, final List<Element> boundElements,
          final TypeMirror modelType) {
    boundElements
      .stream()
      .forEach(boundElement -> {
        final String configuredProperty =
                extractAnnotationStringValue(elements, getAnnotation(boundElement, TypeNames.BOUND), "property");

        final boolean configured = configuredProperty != null && !configuredProperty.isEmpty();
        final String boundProperty = configured ? configuredProperty
                : getDefaultPropertyName(boundElement).orElseThrow(() -> new IllegalStateException(
                        String.format("Found a %s element [%s] annotated with @Bound.", boundElement.getKind(),
                                boundElement.getSimpleName())));

        final TypeMirror boundComponentType = (boundElement instanceof ExecutableElement
                ? ((ExecutableElement) boundElement).getReturnType() : boundElement.asType());
        if (!isValidPropertyChain(modelType, boundComponentType, boundProperty, configured)) {
          processingEnv.getMessager().printMessage(Kind.ERROR,
                  "The model type " + ((DeclaredType) modelType).asElement().getSimpleName()
                          + " does not have property \"" + boundProperty + "\"",
                  boundElement, getAnnotation(boundElement, TypeNames.BOUND));
        }
      });
  }

  private Optional<String> getDefaultPropertyName(final Element boundElement) {
    switch (boundElement.getKind()) {
    case FIELD:
    case PARAMETER:
      return Optional.of(boundElement.getSimpleName().toString());
    case METHOD:
      return Optional.of(propertyNameOfMethod(boundElement));
    default:
      return Optional.empty();
    }
  }

  private void printMissingModelErrorsForBoundElements(final List<Element> value) {
    value
      .stream()
      .forEach(boundElement ->
        processingEnv
          .getMessager()
          .printMessage(Kind.ERROR,
                        "@Bound requires that this class also contains a @Model or @AutoBound DataBinder",
                        boundElement,
                        getAnnotation(boundElement, TypeNames.BOUND)));
  }

  private void validateBoundType(final Types types, final Elements elements, final TypeMirror gwtWidgetType,
          final TypeMirror gwtElementType, final TypeMirror listChangeHandlerType, final Element target) {
    final TypeMirror targetType = getTargetType(target);
    if (!types.isAssignable(targetType, gwtWidgetType)
            && !types.isAssignable(targetType, gwtElementType)
            && !isNativeJsType(targetType, elements)
            && !types.isAssignable(targetType, types.erasure(listChangeHandlerType))) {
      processingEnv.getMessager().printMessage(
              Kind.ERROR, "@Bound must target a type assignable to Widget or Element, or be a JsType element wrapper.", target);
    }
  }

  private TypeMirror getTargetType(final Element target) {
    TypeMirror targetType;
    if (target.getKind() == ElementKind.METHOD) {
      targetType = ((ExecutableElement) target).getReturnType();
    }
    else {
      targetType = target.asType();
    }
    return targetType;
  }

  /**
   * Returns the set of all bindable property names in the given model.
   */
  private Set<String> getPropertyNames(final TypeMirror modelType) {
    final Elements elements = processingEnv.getElementUtils();
    final Types types = processingEnv.getTypeUtils();

    final Set<String> result = new HashSet<>();

    for (final Element el : ElementFilter.methodsIn(elements.getAllMembers((TypeElement) types.asElement(modelType)))) {
      final String propertyName = AnnotationProcessors.propertyNameOfMethod(el);
      if (propertyName != null) {
        result.add(propertyName);
      }
    }
    return result;
  }

  /**
   * Returns the type of the provided property in the given model type.
   */
  private TypeMirror getPropertyType(final TypeMirror modelType, final String property) {
    final Elements elements = processingEnv.getElementUtils();
    final Types types = processingEnv.getTypeUtils();

    TypeMirror result = null;
    for (final Element el : ElementFilter.methodsIn(elements.getAllMembers((TypeElement) types.asElement(modelType)))) {
      final String methodName = el.getSimpleName().toString();
      if (methodName.toLowerCase().equals("get" + property.toLowerCase()) ||
              methodName.toLowerCase().equals("is" + property.toLowerCase())) {
        result = ((ExecutableElement) el).getReturnType();
        break;
      }
    }
    return result;
  }

  /**
   * Returns the bindable model type of all things annotated with {@code @Model}
   * and DataBinders annotated with {@code @AutoBound}. Legally, there should
   * only be one; we return all of them as Elements so the caller can attach
   * error/warning messages to them if we found multiples.
   *
   * @param classContainingBindableThings
   * @return
   */
  private List<TypeMirror> findAllModelTypes(final TypeElement classContainingBindableThings) {
    final List<TypeMirror> result = new ArrayList<>();
    final Elements elements = processingEnv.getElementUtils();

    for (final Element el : elements.getAllMembers(classContainingBindableThings)) {
      switch (el.getKind()) {
      case METHOD:
      case CONSTRUCTOR:
        if (!hasAnnotation(el, TypeNames.JAVAX_INJECT)) continue;

        for (final VariableElement param : ((ExecutableElement) el).getParameters()) {
          if (hasAnnotation(param, TypeNames.MODEL)) {
            result.add(param.asType());
          }
          else if (hasAnnotation(param, TypeNames.AUTO_BOUND)) {
            result.add(typeOfDataBinder(param.asType()));
          }
        }
        break;
      case FIELD:
        if (hasAnnotation(el, TypeNames.MODEL)) {
          result.add(el.asType());
        }
        else if (hasAnnotation(el, TypeNames.AUTO_BOUND)) {
          result.add(typeOfDataBinder(el.asType()));
        }
        break;
      default:
        break;
      }
    }
    return result;
  }

  /**
   * Returns the concrete type, type variable, or wildcard type of the given DataBinder declaration.
   *
   * @param dataBinderDeclaration
   * @return
   */
  private TypeMirror typeOfDataBinder(final TypeMirror dataBinderDeclaration) {
    // in a superclass, this could return a type variable or a wildcard
    return ((DeclaredType) dataBinderDeclaration).getTypeArguments().get(0);
  }

  private boolean isValidPropertyChain(final TypeMirror bindableType, final String propertyChain) {
    final int dotPos = propertyChain.indexOf(".");
    if (dotPos <= 0) {
      return getPropertyNames(bindableType).contains(propertyChain);
    }
    else {
      final String thisProperty = propertyChain.substring(0, dotPos);
      final String moreProperties = propertyChain.substring(dotPos + 1);
      if (!getPropertyNames(bindableType).contains(thisProperty)) {
        return false;
      }

      final TypeMirror propertyType = getPropertyType(bindableType, thisProperty);
      return isValidPropertyChain(propertyType, moreProperties);
    }
  }

  private boolean isValidPropertyChain(final TypeMirror bindableType, final TypeMirror boundElementType, final String propertyChain, final boolean configured) {
    return (!configured && bindsToType(boundElementType, bindableType))
            || isValidPropertyChain(bindableType, propertyChain);
  }

  private boolean bindsToType(final TypeMirror boundElementType, final TypeMirror bindableType) {
    final Types types = processingEnv.getTypeUtils();
    final Elements elements = processingEnv.getElementUtils();
    final TypeMirror takesValue;
    final TypeMirror nativeHasValue;
    final TypeMirror listChangeHandler;

    Optional<TypeMirror> oBoundPropertyType;
    if (types.isAssignable(boundElementType, takesValue = types.erasure(elements.getTypeElement(TAKES_VALUE).asType()))) {
      oBoundPropertyType = resolveSingleTypeArgumentForGenericSuperType(boundElementType, takesValue, types);
    }
    else if (types.isAssignable(boundElementType, nativeHasValue = types.erasure(elements.getTypeElement(NATIVE_HAS_VALUE).asType()))) {
      oBoundPropertyType = resolveSingleTypeArgumentForGenericSuperType(boundElementType, nativeHasValue, types);
    }
    else if (types.isAssignable(boundElementType, listChangeHandler = types.erasure(elements.getTypeElement(LIST_CHANGE_HANDLER).asType()))) {
      oBoundPropertyType =
              resolveSingleTypeArgumentForGenericSuperType(boundElementType, listChangeHandler, types)
              .map(listTypeArg -> types.getDeclaredType(elements.getTypeElement(List.class.getName()), listTypeArg));
    }
    else if (types.isAssignable(boundElementType, elements.getTypeElement(GWT_ELEMENT).asType())
            || isElementWrapper(boundElementType, elements)) {
      oBoundPropertyType = Optional.of(elements.getTypeElement(String.class.getName()).asType());
    }
    else {
      oBoundPropertyType = Optional.empty();
    }

    return oBoundPropertyType
            .filter(propertyType -> types.isSameType(propertyType, bindableType))
            .isPresent();
  }
}
