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

import static org.jboss.errai.processor.AnnotationProcessors.getAnnotation;
import static org.jboss.errai.processor.AnnotationProcessors.getEnclosingTypeElement;
import static org.jboss.errai.processor.AnnotationProcessors.hasAnnotation;
import static org.jboss.errai.processor.AnnotationProcessors.isNativeJsType;
import static org.jboss.errai.processor.AnnotationProcessors.propertyNameOfMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class BoundAnnotationChecker extends AbstractProcessor {

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    final Types types = processingEnv.getTypeUtils();
    final Elements elements = processingEnv.getElementUtils();
    final TypeMirror gwtWidgetType = elements.getTypeElement(TypeNames.GWT_WIDGET).asType();
    final TypeMirror gwtElementType = elements.getTypeElement(TypeNames.GWT_ELEMENT).asType();

    Map<TypeElement, List<Element>> classesWithBoundThings = new HashMap<TypeElement, List<Element>>();
    for (TypeElement annotation : annotations) {
      for (Element target : roundEnv.getElementsAnnotatedWith(annotation)) {
        TypeMirror targetType;
        if (target.getKind() == ElementKind.METHOD) {
          targetType = ((ExecutableElement) target).getReturnType();
        }
        else {
          targetType = target.asType();
        }
        if (!types.isAssignable(targetType, gwtWidgetType) && !types.isAssignable(targetType, gwtElementType) && !isNativeJsType(targetType, elements)) {
          processingEnv.getMessager().printMessage(
                  Kind.ERROR, "@Bound must target a type assignable to Widget or Element, or be a JsType element wrapper.", target);
        }

        TypeElement enclosingClass = getEnclosingTypeElement(target);
        List<Element> boundThings = classesWithBoundThings.get(enclosingClass);
        if (boundThings == null) {
          boundThings = new ArrayList<Element>();
          classesWithBoundThings.put(enclosingClass, boundThings);
        }

        boundThings.add(target);
      }
    }

    for (Map.Entry<TypeElement, List<Element>> classWithItsBoundThings : classesWithBoundThings.entrySet()) {
      List<TypeMirror> modelTypes = findAllModelTypes(classWithItsBoundThings.getKey());
      if (modelTypes.size() == 0) {
        for (Element boundElement : classWithItsBoundThings.getValue()) {
          processingEnv.getMessager().printMessage(
                  Kind.ERROR, "@Bound requires that this class also contains a @Model or @AutoBound DataBinder",
                  boundElement, getAnnotation(boundElement, TypeNames.BOUND));
        }
      }
      else if (modelTypes.size() > 1) {
        // TODO mark everything annotated with @AutoBound or @Model with an error
      }
      else {
        TypeMirror modelType = modelTypes.get(0);
        for (Element boundElement : classWithItsBoundThings.getValue()) {
          String configuredProperty = AnnotationProcessors.
                  extractAnnotationStringValue(elements, getAnnotation(boundElement, TypeNames.BOUND), "property");

          final String boundProperty = (configuredProperty != null && !configuredProperty.isEmpty()) ?
                  configuredProperty : boundElement.getSimpleName().toString();

          switch (boundElement.getKind()) {
          case FIELD:
          case PARAMETER:
            if (!isValidPropertyChain(modelType, boundProperty)) {
              processingEnv.getMessager().printMessage(
                      Kind.ERROR, "The model type " + ((DeclaredType) modelType).asElement().getSimpleName() + " does not have property \"" + boundProperty + "\"",
                      boundElement, getAnnotation(boundElement, TypeNames.BOUND));
            }
            break;
          case METHOD:
            String propertyName = propertyNameOfMethod(boundElement);
            if (!isValidPropertyChain(modelType, propertyName)) {
              processingEnv.getMessager().printMessage(
                      Kind.ERROR, "The model type " + ((DeclaredType) modelType).asElement().getSimpleName() + " does not have property \"" + propertyName + "\"",
                      boundElement, getAnnotation(boundElement, TypeNames.BOUND));
            }
            break;
          default:
            break;
          }
        }
      }
    }

    return false;
  }

  /**
   * Returns the set of all bindable property names in the given model.
   */
  private Set<String> getPropertyNames(TypeMirror modelType) {
    final Elements elements = processingEnv.getElementUtils();
    final Types types = processingEnv.getTypeUtils();

    Set<String> result = new HashSet<String>();

    for (Element el : ElementFilter.methodsIn(elements.getAllMembers((TypeElement) types.asElement(modelType)))) {
      String propertyName = AnnotationProcessors.propertyNameOfMethod(el);
      if (propertyName != null) {
        result.add(propertyName);
      }
      // TODO extract type info from methods
//        for (VariableElement param : ((ExecutableElement) el).getParameters()) {
//          if (hasAnnotation(param, TypeNames.MODEL)) {
//            result.add(param.asType());
//          }
//          else if (hasAnnotation(param, TypeNames.AUTO_BOUND)) {
//            result.add(typeOfDataBinder(param.asType()));
//          }
//        }
    }
    return result;
  }

  /**
   * Returns the type of the provided property in the given model type.
   */
  private TypeMirror getPropertyType(TypeMirror modelType, String property) {
    final Elements elements = processingEnv.getElementUtils();
    final Types types = processingEnv.getTypeUtils();

    TypeMirror result = null;
    for (Element el : ElementFilter.methodsIn(elements.getAllMembers((TypeElement) types.asElement(modelType)))) {
      String methodName = el.getSimpleName().toString();
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
  private List<TypeMirror> findAllModelTypes(TypeElement classContainingBindableThings) {
    final List<TypeMirror> result = new ArrayList<TypeMirror>();
    final Elements elements = processingEnv.getElementUtils();

    for (Element el : elements.getAllMembers(classContainingBindableThings)) {
      switch (el.getKind()) {
      case METHOD:
      case CONSTRUCTOR:
        if (!hasAnnotation(el, TypeNames.JAVAX_INJECT)) continue;

        for (VariableElement param : ((ExecutableElement) el).getParameters()) {
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
  private TypeMirror typeOfDataBinder(TypeMirror dataBinderDeclaration) {
    // in a superclass, this could return a type variable or a wildcard
    return ((DeclaredType) dataBinderDeclaration).getTypeArguments().get(0);
  }

  /**
   * Returns true if and only if the given property chain is a valid property
   * expression rooted in the given bindable type.
   *
   * @param bindableType
   *          The root type the given property chain is resolved against. Not
   *          null.
   * @param propertyChain
   *          The data binding property chain to validate. Not null.
   * @return True if the given property chain is resolvable from the given
   *         bindable type.
   */
  private boolean isValidPropertyChain(TypeMirror bindableType, String propertyChain) {
    int dotPos = propertyChain.indexOf(".");
    if (dotPos <= 0) {
      return getPropertyNames(bindableType).contains(propertyChain);
    }
    else {
      String thisProperty = propertyChain.substring(0, dotPos);
      String moreProperties = propertyChain.substring(dotPos + 1);
      if (!getPropertyNames(bindableType).contains(thisProperty)) {
        return false;
      }

      TypeMirror propertyType = getPropertyType(bindableType, thisProperty);
      return isValidPropertyChain(propertyType, moreProperties);
    }
  }
}
