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
import org.jboss.errai.common.apt.AnnotatedSourceElementsFinder;
import org.jboss.errai.common.apt.AptAnnotatedSourceElementsFinder;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Set;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public abstract class AbstractErraiModuleExportFileGenerator extends AbstractProcessor {

  protected abstract String getCamelCaseErraiModuleName();

  @Override
  public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {

    final Types types = processingEnv.getTypeUtils();
    final Elements elements = processingEnv.getElementUtils();
    final Filer filer = processingEnv.getFiler();

    process(annotations, types, elements, filer, new AptAnnotatedSourceElementsFinder(roundEnv));

    return false;
  }

  public void process(final Set<? extends TypeElement> annotations,
          final Types types,
          final Elements elements,
          final Filer filer,
          final AnnotatedSourceElementsFinder annotatedSourceElementsFinder) {
    try {
      APTClassUtil.init(types, elements);
      generateAndSaveExportFiles(annotations, annotatedSourceElementsFinder, filer);
    } catch (final Exception e) {
      System.out.println("Error generating export files");
      e.printStackTrace();
    }
  }

  void generateAndSaveExportFiles(final Set<? extends TypeElement> exportableAnnotations,
          final AnnotatedSourceElementsFinder annotatedSourceElementsFinder,
          final Filer filer) {

    final String camelCaseErraiModuleName = getCamelCaseErraiModuleName();
    final ExportFileGenerator generator = new ExportFileGenerator(camelCaseErraiModuleName, exportableAnnotations,
            annotatedSourceElementsFinder);

    generator.generateAndSaveExportFiles(filer);
  }
}
