package org.jboss.errai.processor;

import static org.jboss.errai.processor.AnnotationProcessors.*;

import java.io.IOException;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 * Evaluates usage of the ErraiUI Templated annotation and emits errors and warnings when
 * the annotation is not being used correctly.
 */
@SupportedAnnotationTypes(TypeNames.TEMPLATED)
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class TemplatedAnnotationChecker extends AbstractProcessor {

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    final Types types = processingEnv.getTypeUtils();
    final Elements elements = processingEnv.getElementUtils();
    final TypeMirror gwtCompositeType = elements.getTypeElement(TypeNames.GWT_COMPOSITE).asType();
    
    for (TypeElement annotation : annotations) {
      for (Element target : roundEnv.getElementsAnnotatedWith(annotation)) {
        if (!types.isAssignable(target.asType(), gwtCompositeType)) {
          processingEnv.getMessager().printMessage(
                  Kind.ERROR, "@Templated classes must be a direct or indirect subtype of Composite", target);
        }
        
        PackageElement packageElement = elements.getPackageOf(target);
        String templateRef = getReferencedTemplate(target);
        String templateRefError = null;
        try {
          FileObject resource = processingEnv.getFiler().getResource(StandardLocation.CLASS_PATH, packageElement.getQualifiedName(), templateRef);
          CharSequence charContent = resource.getCharContent(true);
          System.out.println("Contents of template: " + charContent);
        } catch (IllegalArgumentException e) {
          // unfortunately, Eclipse just throws IAE when we try to read files from CLASS_PATH
          // so the best we can do is ignore this error and skip validating the template reference
        } catch (IOException e) {
          templateRefError = "Could not access associated template " + templateRef;
        }
        if (templateRefError != null) {
          processingEnv.getMessager().printMessage(Kind.ERROR, templateRefError, annotation);
        }
      }
    }
    return false;
  }

  /**
   * Resolves the filename that the given class's {@code @Templated} annotation
   * points to, taking all default behaviour into account.
   * 
   * @param target a class that bears the {@code Templated} annotation.
   */
  private String getReferencedTemplate(Element target) {
    String templateRef = "";
    AnnotationValue paramValue = getAnnotationParamValueWithoutDefaults(target, TypeNames.TEMPLATED, "value");
    if (paramValue != null) {
      templateRef = paramValue.getValue().toString();
    }
    if (templateRef.equals("")) {
      templateRef = target.getSimpleName() + ".html";
    }
    return templateRef;
  }
  
}
