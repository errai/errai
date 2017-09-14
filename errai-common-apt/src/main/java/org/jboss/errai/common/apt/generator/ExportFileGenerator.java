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

import org.jboss.errai.common.apt.AnnotatedSourceElementsFinder;
import org.jboss.errai.common.apt.exportfile.ExportFile;
import org.jboss.errai.common.configuration.ErraiModule;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;

import static java.util.stream.Collectors.toSet;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
class ExportFileGenerator {

  private String camelCaseErraiModuleName;
  private final Set<? extends TypeElement> exportableAnnotations;
  private final AnnotatedSourceElementsFinder annotatedSourceElementsFinder;

  ExportFileGenerator(final String camelCaseErraiModuleName,
          final Set<? extends TypeElement> exportableAnnotations,
          final AnnotatedSourceElementsFinder annotatedSourceElementsFinder) {

    this.camelCaseErraiModuleName = camelCaseErraiModuleName;
    this.exportableAnnotations = exportableAnnotations;
    this.annotatedSourceElementsFinder = annotatedSourceElementsFinder;
  }

  void generateAndSaveExportFiles(final Filer filer) {
    buildExportFiles().forEach(exportFile -> generateSourceAndSave(exportFile, filer));
  }

  Set<ExportFile> buildExportFiles() {
    return exportableAnnotations.stream()
            .map(this::newExportFile)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(toSet());
  }

  Optional<ExportFile> newExportFile(final TypeElement annotation) {

    final Set<Element> exportedTypes = annotatedClassesAndInterfaces(annotation);

    if (exportedTypes.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(new ExportFile(exportFileNamespace(), annotation, exportedTypes));
  }

  private String exportFileNamespace() {
    final String erraiModuleClassName = annotatedSourceElementsFinder.findSourceElementsAnnotatedWith(ErraiModule.class)
            .stream()
            .collect(singleton())
            .map(this::erraiModuleFullQualifiedName)
            .orElseThrow(() -> {
              final String msg = "There must be one class, and one only, annotated with @ErraiModule";
              return new RuntimeException(msg);
            });

    return camelCaseErraiModuleName + "__" + erraiModuleClassName;
  }

  private String erraiModuleFullQualifiedName(final Element erraiModuleElement) {
    return ((TypeElement) erraiModuleElement).getQualifiedName().toString().replace(".", "_");
  }

  Set<Element> annotatedClassesAndInterfaces(final TypeElement annotationTypeElement) {
    return annotatedSourceElementsFinder.findSourceElementsAnnotatedWith(annotationTypeElement)
            .stream()
            .filter(e -> e.getKind().isClass() || e.getKind().isInterface())
            .collect(toSet());
  }

  private void generateSourceAndSave(final ExportFile exportFile, final Filer filer) {
    try {
      final Element[] originatingElements = exportFile.exportedTypes.toArray(new Element[0]);
      final JavaFileObject sourceFile = filer.createSourceFile(exportFile.getFullClassName(), originatingElements);

      try (Writer writer = sourceFile.openWriter()) {
        writer.write(exportFile.generateSource());
      }
      System.out.println("Successfully generated export file [" + exportFile.simpleClassName + "]");
    } catch (final IOException e) {
      throw new RuntimeException("Error writing generated export file", e);
    }
  }

  private static <T> Collector<T, List<T>, Optional<T>> singleton() {
    return Collector.of(ArrayList::new, List::add, (left, right) -> {
      left.addAll(right);
      return left;
    }, list -> {
      if (list.size() != 1) {
        return Optional.empty();
      } else {
        return Optional.of(list.get(0));
      }
    });
  }
}
