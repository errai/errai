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

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.impl.apt.APTClass;
import org.jboss.errai.codegen.meta.impl.apt.APTClassUtil;
import org.jboss.errai.common.apt.AnnotatedSourceElementsFinder;
import org.jboss.errai.common.apt.exportfile.ExportFile;
import org.jboss.errai.common.apt.module.ErraiModule;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class ExportFileGenerator {

  private final String camelCaseErraiModuleName;
  private final Set<? extends TypeElement> exportableAnnotations;
  private final AnnotatedSourceElementsFinder annotatedSourceElementsFinder;

  public ExportFileGenerator(final String camelCaseErraiModuleName,
          final Set<? extends TypeElement> exportableAnnotations,
          final AnnotatedSourceElementsFinder annotatedSourceElementsFinder) {

    this.camelCaseErraiModuleName = camelCaseErraiModuleName;
    this.exportableAnnotations = exportableAnnotations;
    this.annotatedSourceElementsFinder = annotatedSourceElementsFinder;
  }

  void generateAndSaveExportFiles(final Filer filer) {
    createExportFiles().forEach(exportFile -> generateSourceAndSave(exportFile, filer));
  }

  public Set<ExportFile> createExportFiles() {
    return annotatedSourceElementsFinder.findSourceElementsAnnotatedWith(
            org.jboss.errai.common.configuration.ErraiModule.class)
            .stream()
            .map(s -> new APTClass(s.asType()))
            .map(this::newModule)
            .flatMap(this::createExportFiles)
            .collect(toSet());
  }

  private ErraiModule newModule(final MetaClass metaClass) {
    return new ErraiModule(camelCaseErraiModuleName, metaClass, annotatedSourceElementsFinder);
  }

  private Stream<ExportFile> createExportFiles(final ErraiModule erraiModule) {
    return erraiModule.exportFiles(exportableAnnotations);
  }

  private void generateSourceAndSave(final ExportFile exportFile, final Filer filer) {
    try {

      final Element[] originatingElements = exportFile.exportedTypes()
              .stream()
              .map(APTClassUtil.types::asElement)
              .collect(toSet())
              .toArray(new Element[0]);

      final JavaFileObject sourceFile = filer.createSourceFile(exportFile.getFullClassName(), originatingElements);

      try (Writer writer = sourceFile.openWriter()) {
        writer.write(exportFile.generateSource());
      }
    } catch (final IOException e) {
      throw new RuntimeException("Error writing generated export file " + exportFile.getFullClassName(), e);
    }
  }
}
