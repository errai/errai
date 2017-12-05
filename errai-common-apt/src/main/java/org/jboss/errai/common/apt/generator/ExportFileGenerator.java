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
import org.jboss.errai.codegen.meta.impl.apt.APTClassUtil;
import org.jboss.errai.common.apt.AnnotatedSourceElementsFinder;
import org.jboss.errai.common.apt.exportfile.ExportFile;
import org.jboss.errai.common.apt.module.ErraiModule;
import org.jboss.errai.common.apt.strategies.ExportingStrategies;

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
  private final AnnotatedSourceElementsFinder annotatedSourceElementsFinder;
  private final ExportingStrategies exportingStrategies;
  private final Set<MetaClass> erraiModuleMetaClasses;

  public ExportFileGenerator(final String camelCaseErraiModuleName,
          final AnnotatedSourceElementsFinder annotatedSourceElementsFinder,
          final ExportingStrategies exportingStrategies,
          final Set<MetaClass> erraiModuleMetaClasses) {

    this.camelCaseErraiModuleName = camelCaseErraiModuleName;
    this.annotatedSourceElementsFinder = annotatedSourceElementsFinder;
    this.exportingStrategies = exportingStrategies;
    this.erraiModuleMetaClasses = erraiModuleMetaClasses;
  }

  void generateAndSaveExportFiles(final Filer filer, final Set<TypeElement> exportableAnnotations) {
    createExportFiles(exportableAnnotations).forEach(exportFile -> generateSourceAndSave(exportFile, filer));
  }

  public Set<ExportFile> createExportFiles(final Set<? extends TypeElement> exportableAnnotations) {
    return erraiModuleMetaClasses.stream()
            .map(this::newModule)
            .flatMap(erraiModule -> createExportFiles(erraiModule, exportableAnnotations))
            .collect(toSet());
  }

  private ErraiModule newModule(final MetaClass metaClass) {
    return new ErraiModule(camelCaseErraiModuleName, metaClass, annotatedSourceElementsFinder, exportingStrategies);
  }

  private Stream<ExportFile> createExportFiles(final ErraiModule erraiModule,
          final Set<? extends TypeElement> exportableAnnotations) {
    return erraiModule.createExportFiles(exportableAnnotations);
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
