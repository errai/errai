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

import java.util.List;
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
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class EventHandlerAnnotationChecker extends AbstractProcessor {

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    final Types types = processingEnv.getTypeUtils();
    final Elements elements = processingEnv.getElementUtils();
    final TypeMirror gwtWidgetType = elements.getTypeElement(TypeNames.GWT_WIDGET).asType();
    final TypeMirror gwtElementType = elements.getTypeElement(TypeNames.GWT_ELEMENT).asType();
    
    for (TypeElement annotation : annotations) {
      for (Element target : roundEnv.getElementsAnnotatedWith(annotation)) {
        if (((ExecutableElement) target).getReturnType().getKind() != TypeKind.VOID) {
          processingEnv.getMessager().printMessage(
                  Kind.ERROR, "@EventHandler methods must return void", target);
        }
        
        AnnotationMirror eventHandlerAnnotation = getAnnotation(target, TypeNames.EVENT_HANDLER);
        TypeElement enclosingClassElement = (TypeElement) target.getEnclosingElement();
        boolean hasSinkNative = hasAnnotation(target, TypeNames.SINK_NATIVE);
        
        AnnotationValue eventHandlerAnnotationValue = getAnnotationParamValueWithoutDefaults(target, TypeNames.EVENT_HANDLER, "value");
        
        // if there is no annotation parameter value, this method handles events from the templated widget itself: nothing more to check.
        // if the method is also annotated with @SinkNative, the values refer to template elements and we can't (easily) check them
        if (eventHandlerAnnotationValue != null && !hasSinkNative) {
          @SuppressWarnings("unchecked")
          List<AnnotationValue> eventHandlerAnnotationValues = (List<AnnotationValue>) eventHandlerAnnotationValue.getValue();
          for (AnnotationValue av : eventHandlerAnnotationValues) {
            String referencedFieldName = (String) av.getValue();
            Element referencedField = getField(enclosingClassElement, referencedFieldName);
            if (referencedField == null || (!types.isAssignable(referencedField.asType(), gwtWidgetType) &&!types.isAssignable(referencedField.asType(), gwtElementType))) {
              processingEnv.getMessager().printMessage(
                      Kind.ERROR, "\"" + referencedFieldName + "\" must refer to a field of type Widget or Element. To reference template elements directly, use @SinkNative.",
                      target, eventHandlerAnnotation, av);
            }
          }
        }
        
        List<? extends VariableElement> methodParams = ((ExecutableElement) target).getParameters();
        TypeMirror requiredArgType = hasSinkNative ?
                elements.getTypeElement(TypeNames.GWT_OPAQUE_DOM_EVENT).asType() :
                types.getDeclaredType(elements.getTypeElement(TypeNames.GWT_EVENT), types.getWildcardType(null, null));
        if (methodParams.size() != 1 || !types.isAssignable(methodParams.get(0).asType(), requiredArgType)) {
          if (hasSinkNative) {
            processingEnv.getMessager().printMessage(
                    Kind.ERROR, "Native event handling methods must take exactly one argument of type " + requiredArgType,
                    target);
          }
          else {
            processingEnv.getMessager().printMessage(
                    Kind.ERROR, "Event handling methods must take exactly one argument of a concrete subtype of " + requiredArgType,
                    target);
          }
        }
        
        if (!hasAnnotation(enclosingClassElement, TypeNames.TEMPLATED)) {
          processingEnv.getMessager().printMessage(
                  Kind.WARNING, "@EventHandler annotations have no effect outside of @Templated classes",
                  target, eventHandlerAnnotation);
        }
        
      }
    }
    return false;
  }
  
}
