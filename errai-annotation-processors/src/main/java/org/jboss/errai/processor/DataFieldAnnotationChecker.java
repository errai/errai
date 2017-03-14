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

import static org.jboss.errai.processor.AnnotationProcessors.*;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

/**
 * Evaluates usage of the ErraiUI DataField annotation and emits errors and warnings when
 * the annotation is not being used correctly.
 */
@SupportedAnnotationTypes(TypeNames.DATA_FIELD)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class DataFieldAnnotationChecker extends AbstractProcessor {

  @Override
  public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
    final Types types = processingEnv.getTypeUtils();
    final Elements elements = processingEnv.getElementUtils();
    final TypeMirror gwtWidgetType = elements.getTypeElement(TypeNames.GWT_WIDGET).asType();
    final TypeMirror gwtElementType = elements.getTypeElement(TypeNames.GWT_ELEMENT).asType();
    final TypeMirror listChangeHandlerType = types.erasure(elements.getTypeElement(TypeNames.LIST_CHANGE_HANDLER).asType());

    annotations
      .stream()
      .flatMap(annotation -> roundEnv.getElementsAnnotatedWith(annotation).stream())
      .forEach(target -> {
        final TypeMirror targetType = target.asType();
        if (!types.isAssignable(targetType, gwtWidgetType)
                && !types.isAssignable(targetType, gwtElementType)
                && !types.isAssignable(targetType, listChangeHandlerType)
                && !isTemplated(targetType, elements)
                && !isNativeJsType(targetType, elements)) {
          processingEnv.getMessager().printMessage(
                  Kind.ERROR, "Fields anotated with @DataField must be assignable to Widget or Element, or be a native JsType element wrapper.", target);
        }

        final Element enclosingClassElement = target.getEnclosingElement();
        if (!hasAnnotation(enclosingClassElement, TypeNames.TEMPLATED)) {
          processingEnv.getMessager().printMessage(
                  Kind.WARNING, "@DataField annotations have no effect outside of @Templated classes",
                  target, getAnnotation(target, TypeNames.DATA_FIELD));
        }

        // ideally, we would read the template file now and search it to be sure the element exists.
        // but unfortunately eclipse doesn't let annotation processors read files from the source directory
      });

    return false;
  }

}
