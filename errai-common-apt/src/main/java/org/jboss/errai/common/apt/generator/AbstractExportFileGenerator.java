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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.impl.apt.APTClass;
import org.jboss.errai.codegen.meta.impl.apt.APTClassUtil;
import org.jboss.errai.common.apt.AnnotatedSourceElementsFinder;
import org.jboss.errai.common.apt.generator.app.ErraiAppAptGenerator;
import org.jboss.errai.common.apt.strategies.ErraiExportingStrategiesFactory;
import org.jboss.errai.common.apt.strategies.ExportingStrategies;
import org.jboss.errai.common.configuration.ErraiApp;
import org.jboss.errai.common.configuration.ErraiModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.jboss.errai.codegen.meta.impl.apt.APTClassUtil.elements;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public abstract class AbstractExportFileGenerator extends AbstractProcessor {

  private static final Logger log = LoggerFactory.getLogger(AbstractExportFileGenerator.class);

  protected abstract String getCamelCaseErraiModuleName();

  protected abstract Class<?> getExportingStrategiesClass();

  private ExportFileGenerator exportFileGenerator;

  @Override
  public synchronized void init(final ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    APTClassUtil.init(processingEnv.getTypeUtils(), processingEnv.getElementUtils());
  }

  private final Set<MetaClass> erraiModules = new HashSet<>();
  private final Set<MetaClass> erraiApps = new HashSet<>();
  private final Multimap<String, Element> exportedTypes = HashMultimap.create();

  @Override
  public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {

    try {
      generate(annotations, roundEnv);
    } catch (final Exception e) {
      log.error("Error generating export files");
      e.printStackTrace();
    }

    return false;
  }

  private void generate(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {

    if (!annotations.isEmpty()) {
      ExportFileGeneratorsControl.signalExistence(this);
    }

    erraiApps.addAll(findAnnotatedMetaClasses(roundEnv, ErraiApp.class));
    erraiModules.addAll(findAnnotatedMetaClasses(roundEnv, ErraiModule.class));

    for (final TypeElement annotation : annotations) {
      final String annotationName = annotation.getQualifiedName().toString();
      exportedTypes.putAll(annotationName, roundEnv.getElementsAnnotatedWith(annotation));
    }

    if (roundEnv.processingOver()) {
      ExportFileGeneratorsControl.signalReady(this);

      generateExportFiles();

      if (ExportFileGeneratorsControl.isReady()) {
        new ErraiAppAptGenerator(processingEnv).generateAndSaveSourceFiles(erraiApps);
      }
    }
  }

  private void generateExportFiles() {
    final AnnotatedSourceElementsFinder annotatedSourceElementsFinder = new IncrementalAnnotatedSourceElementsFinder(
            exportedTypes);

    final ExportingStrategies exportingStrategies = new ErraiExportingStrategiesFactory(processingEnv.getElementUtils())
            .buildFrom(getExportingStrategiesClass());

    final ExportFileGenerator exportFileGenerator = new ExportFileGenerator(getCamelCaseErraiModuleName(),
            annotatedSourceElementsFinder, exportingStrategies, erraiModules);

    exportFileGenerator.generateAndSaveExportFiles(processingEnv.getFiler(),
            exportedTypes.keys().stream().map(elements::getTypeElement).collect(toSet()));
  }

  private Set<MetaClass> findAnnotatedMetaClasses(final RoundEnvironment roundEnv,
          final Class<? extends Annotation> annotation) {

    return roundEnv.getElementsAnnotatedWith(annotation)
            .stream()
            .map(Element::asType)
            .map(APTClass::new)
            .collect(toSet());
  }

  private static class IncrementalAnnotatedSourceElementsFinder implements AnnotatedSourceElementsFinder {

    private final Multimap<String, Element> exportedTypes;

    private IncrementalAnnotatedSourceElementsFinder(final Multimap<String, Element> exportedTypes) {
      this.exportedTypes = exportedTypes;
    }

    @Override
    public Set<? extends Element> findSourceElementsAnnotatedWith(final TypeElement typeElement) {
      return new HashSet<>(exportedTypes.get(typeElement.getQualifiedName().toString()));
    }

    @Override
    public Set<? extends Element> findSourceElementsAnnotatedWith(final Class<? extends Annotation> annotationClass) {
      return new HashSet<>(exportedTypes.get(annotationClass.getCanonicalName()));
    }
  }
}
