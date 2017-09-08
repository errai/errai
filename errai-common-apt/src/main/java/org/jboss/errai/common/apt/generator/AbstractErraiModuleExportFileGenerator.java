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

package org.jboss.errai.common.apt.generator;

import org.jboss.errai.codegen.meta.impl.apt.APTClassUtil;
import org.jboss.errai.common.apt.AnnotatedElementsFinder;
import org.jboss.errai.common.apt.AptAnnotatedElementsFinder;
import org.jboss.errai.common.apt.exportfile.ExportFile;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public abstract class AbstractErraiModuleExportFileGenerator extends AbstractProcessor {

  @Override
  public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {

    try {
      APTClassUtil.init(processingEnv.getTypeUtils(), processingEnv.getElementUtils());
      generateAndSaveExportFiles(annotations, new AptAnnotatedElementsFinder(roundEnv));
    } catch (final Exception e) {
      System.out.println("Error generating export files");
      e.printStackTrace();
    }

    return false;
  }

  void generateAndSaveExportFiles(final Set<? extends TypeElement> annotations,
          final AnnotatedElementsFinder annotatedElementsFinder) {

    buildExportFiles(annotations, annotatedElementsFinder).forEach(this::generateSourceAndSave);
  }

  Set<ExportFile> buildExportFiles(final Set<? extends TypeElement> annotations,
          final AnnotatedElementsFinder annotatedElementsFinder) {

    return annotations.stream()
            .map(annotation -> newExportFile(annotatedElementsFinder, annotation))
            .filter(ExportFile::hasExportedTypes)
            .collect(toSet());
  }

  ExportFile newExportFile(final AnnotatedElementsFinder annotatedElementsFinder, final TypeElement annotation) {
    final Set<Element> exportedTypes = annotatedClassesAndInterfaces(annotatedElementsFinder, annotation);
    return new ExportFile(getModuleName(), annotation, exportedTypes);
  }

  Set<Element> annotatedClassesAndInterfaces(final AnnotatedElementsFinder annotatedElementsFinder,
          final TypeElement annotationTypeElement) {

    return annotatedElementsFinder.getElementsAnnotatedWith(annotationTypeElement)
            .stream()
            .filter(e -> e.getKind().isClass() || e.getKind().isInterface())
            .collect(toSet());
  }

  private void generateSourceAndSave(final ExportFile exportFile) {
    try {
      final Element[] originatingElements = exportFile.exportedTypes.toArray(new Element[0]);
      final JavaFileObject sourceFile = processingEnv.getFiler()
              .createSourceFile(exportFile.getFullClassName(), originatingElements);

      try (Writer writer = sourceFile.openWriter()) {
        writer.write(exportFile.generateSource());
      }
      System.out.println("Successfully generated export file [" + exportFile.simpleClassName + "]");
    } catch (final IOException e) {
      throw new RuntimeException("Error writing generated export file", e);
    }
  }

  protected abstract String getModuleName();
}
