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

import static org.jboss.errai.processor.AnnotationProcessors.getAnnotationParamValueWithoutDefaults;

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
import javax.lang.model.util.Elements;
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
    final Elements elements = processingEnv.getElementUtils();

    for (TypeElement annotation : annotations) {
      for (Element target : roundEnv.getElementsAnnotatedWith(annotation)) {

        PackageElement packageElement = elements.getPackageOf(target);
        String templateRef = getReferencedTemplate(target);
        String templateRefError = null;
        try {
          FileObject resource = processingEnv.getFiler().getResource(StandardLocation.CLASS_PATH, packageElement.getQualifiedName(), templateRef);
          resource.getCharContent(true);
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
   * @param target
   *          a class that bears the {@code Templated} annotation.
   */
  private String getReferencedTemplate(Element target) {
    String templateRef = "";
    AnnotationValue paramValue = getAnnotationParamValueWithoutDefaults(target,
            TypeNames.TEMPLATED, "value");
    if (paramValue != null) {
      if (paramValue.getValue().toString().startsWith("#")) {
        // use simple name
      }
      else if (paramValue.getValue().toString().contains("#")) {
        String[] split = paramValue.getValue().toString().split("#");
        if (split != null && split.length > 0) {
          // use html part
          templateRef = split[0];
        }
      }
      else {
        templateRef = paramValue.getValue().toString();
      }
    }
    if (templateRef.equals("")) {
      templateRef = target.getSimpleName() + ".html";
    }
    return templateRef;
  }

}
