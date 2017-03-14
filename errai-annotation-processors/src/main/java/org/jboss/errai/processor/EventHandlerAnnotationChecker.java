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
import static org.jboss.errai.processor.AnnotationProcessors.getAnnotationParamValueWithoutDefaults;
import static org.jboss.errai.processor.AnnotationProcessors.getField;
import static org.jboss.errai.processor.AnnotationProcessors.hasAnnotation;
import static org.jboss.errai.processor.AnnotationProcessors.isBrowserEvent;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

/**
 * Evaluates usage of the ErraiUI EventHandler annotation and emits errors and warnings when
 * the annotation is not being used correctly.
 */
@SupportedAnnotationTypes(TypeNames.EVENT_HANDLER)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class EventHandlerAnnotationChecker extends AbstractProcessor {

  @Override
  public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
    final Types types = processingEnv.getTypeUtils();
    final Elements elements = processingEnv.getElementUtils();
    final TypeMirror gwtWidgetType = elements.getTypeElement(TypeNames.GWT_WIDGET).asType();
    final TypeMirror gwtElementType = elements.getTypeElement(TypeNames.GWT_ELEMENT).asType();

    annotations
      .stream()
      .flatMap(annotation -> roundEnv.getElementsAnnotatedWith(annotation).stream())
      .forEach(target -> {
        validateVoidReturnType(target);

        final AnnotationMirror eventHandlerAnnotation = getAnnotation(target, TypeNames.EVENT_HANDLER);
        final TypeElement enclosingClassElement = (TypeElement) target.getEnclosingElement();
        final boolean hasSinkNative = hasAnnotation(target, TypeNames.SINK_NATIVE);
        final Optional<? extends VariableElement> oParam = validateSingleParam((ExecutableElement) target);

        maybeWarnAboutMissingTemplated(target, eventHandlerAnnotation, enclosingClassElement);

        oParam.ifPresent(param -> {
          final boolean hasJsInteropEventParm = isBrowserEvent(param.asType(), elements);
          final boolean isNativeEvent = hasSinkNative || hasJsInteropEventParm;

          final AnnotationValue eventHandlerAnnotationValue =
                  getAnnotationParamValueWithoutDefaults(target, TypeNames.EVENT_HANDLER, "value");

          // if there is no annotation parameter value, this method handles events from the templated widget itself:
          // nothing more to check.
          // if the method is also annotated with @SinkNative, the values refer to template elements and we can't
          // (easily) check them
          if (eventHandlerAnnotationValue != null && !isNativeEvent) {
            validateFieldIsWidgetOrGwtElement(types, gwtWidgetType, gwtElementType, target, eventHandlerAnnotation,
                    enclosingClassElement, eventHandlerAnnotationValue);
          }

          if (hasSinkNative) {
            final TypeMirror requiredParamType = elements.getTypeElement(TypeNames.GWT_OPAQUE_DOM_EVENT).asType();
            if (!types.isAssignable(param.asType(), requiredParamType)) {
              processingEnv.getMessager().printMessage(Kind.ERROR,
                      "@SinkNative event handling methods must take exactly one argument of type " + requiredParamType,
                      target);
            }
          }
          else if (hasJsInteropEventParm) {
            // TODO add validation of event parameter type and @ForEvent
          }
          else {
            final TypeMirror gwtDomEvent = elements.getTypeElement(TypeNames.GWT_OPAQUE_DOM_EVENT).asType();
            final TypeMirror gwtEvent = types.erasure(elements.getTypeElement(TypeNames.GWT_EVENT).asType());
            if (!types.isAssignable(param.asType(), gwtEvent)) {
              processingEnv.getMessager().printMessage(Kind.ERROR,
                            String.format(
                                    "Event handling methods must take exactly one argument that is a [%s], [%s], or a native @BrowserEvent.",
                                    gwtDomEvent, gwtEvent),
                            target);
            }
          }

        });
        });

    return false;
  }

  private void maybeWarnAboutMissingTemplated(final Element target, final AnnotationMirror eventHandlerAnnotation,
          final TypeElement enclosingClassElement) {
    if (!hasAnnotation(enclosingClassElement, TypeNames.TEMPLATED)) {
      processingEnv.getMessager().printMessage(Kind.WARNING,
              "@EventHandler annotations have no effect outside of @Templated classes", target, eventHandlerAnnotation);
    }
  }

  private Optional<? extends VariableElement> validateSingleParam(final ExecutableElement method) {
    final List<? extends VariableElement> parameters = method.getParameters();
    if (parameters.size() != 1) {
      processingEnv.getMessager().printMessage(Kind.ERROR, "Event handling methods must take exactly one argument.", method);
      return Optional.empty();
    }
    else {
      return Optional.of(parameters.get(0));
    }
  }

  private void validateFieldIsWidgetOrGwtElement(final Types types, final TypeMirror gwtWidgetType,
          final TypeMirror gwtElementType, final Element target, final AnnotationMirror eventHandlerAnnotation,
          final TypeElement enclosingClassElement, final AnnotationValue eventHandlerAnnotationValue) {
    @SuppressWarnings("unchecked")
    final List<AnnotationValue> eventHandlerAnnotationValues = (List<AnnotationValue>) eventHandlerAnnotationValue
            .getValue();
    eventHandlerAnnotationValues.stream().forEach(av -> {
      final String referencedFieldName = (String) av.getValue();
      final Optional<Element> oReferencedField = getField(enclosingClassElement, referencedFieldName);
      oReferencedField
        .filter(field -> types.isAssignable(field.asType(), gwtWidgetType) || types.isAssignable(field.asType(), gwtElementType))
        .isPresent();
      if (!oReferencedField
              .filter(field -> types.isAssignable(field.asType(), gwtWidgetType)
                      || types.isAssignable(field.asType(), gwtElementType))
              .isPresent()) {
        processingEnv.getMessager().printMessage(Kind.ERROR,
                "\"" + referencedFieldName
                        + "\" must refer to a field of type Widget or GWT Element. To reference template elements directly, use @SinkNative or a @BrowserEvent.",
                target, eventHandlerAnnotation, av);
      }
    });
  }

  private void validateVoidReturnType(final Element target){
    if (((ExecutableElement) target).getReturnType().getKind() != TypeKind.VOID) {
      processingEnv.getMessager().printMessage(Kind.ERROR, "@EventHandler methods must return void", target);
    }
  }

}
