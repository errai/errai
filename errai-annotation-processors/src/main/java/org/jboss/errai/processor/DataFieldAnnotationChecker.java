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
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class DataFieldAnnotationChecker extends AbstractProcessor {

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    final Types types = processingEnv.getTypeUtils();
    final Elements elements = processingEnv.getElementUtils();
    final TypeMirror gwtWidgetType = elements.getTypeElement(TypeNames.GWT_WIDGET).asType();
    
    for (TypeElement annotation : annotations) {
      for (Element target : roundEnv.getElementsAnnotatedWith(annotation)) {
        if (!types.isAssignable(target.asType(), gwtWidgetType)) {
          processingEnv.getMessager().printMessage(
                  Kind.ERROR, "Fields anotated with @DataField must be assignable to Widget", target);
        }
        
        Element enclosingClassElement = target.getEnclosingElement();
        if (!hasAnnotation(enclosingClassElement, TypeNames.TEMPLATED)) {
          processingEnv.getMessager().printMessage(
                  Kind.WARNING, "@DataField annotations have no effect outside of @Templated classes",
                  target, getAnnotation(target, TypeNames.DATA_FIELD));
        }
        
        // ideally, we would read the template file now and search it to be sure the element exists.
        // but unfortunately eclipse doesn't let annotation processors read files from the source directory
      }
    }
    return false;
  }
  
}
