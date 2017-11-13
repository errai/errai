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

package org.jboss.errai.common.apt;

import org.jboss.errai.codegen.meta.MetaAnnotation;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.impl.apt.APTClass;
import org.jboss.errai.codegen.meta.impl.apt.APTClassUtil;
import org.jboss.errai.common.apt.configuration.AptErraiAppConfiguration;
import org.jboss.errai.common.apt.exportfile.ExportFile;
import org.jboss.errai.common.apt.exportfile.ExportFileName;
import org.jboss.errai.common.apt.generator.ExportFileGenerator;
import org.jboss.errai.common.apt.strategies.ErraiExportingStrategies;
import org.jboss.errai.common.apt.strategies.ErraiExportingStrategiesFactory;
import org.jboss.errai.common.apt.strategies.ErraiExportingStrategy;
import org.jboss.errai.common.apt.strategies.ExportingStrategies;
import org.jboss.errai.common.configuration.ErraiApp;
import org.jboss.errai.common.configuration.ErraiGenerator;
import org.jboss.errai.common.configuration.ErraiModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static org.jboss.errai.codegen.meta.impl.apt.APTClassUtil.getTypeElement;
import static org.jboss.errai.common.apt.ErraiAptPackages.exportFilesPackageElement;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public final class ErraiAptExportedTypes {

  private static final Logger log = LoggerFactory.getLogger(ErraiAptExportedTypes.class);

  private final Map<String, Set<TypeMirror>> exportedClassesByAnnotationClassName;

  private final Elements elements;
  private final AnnotatedSourceElementsFinder annotatedSourceElementsFinder;
  private final ResourceFilesFinder resourcesFilesFinder;
  private final AptErraiAppConfiguration aptErraiAppConfiguration;
  private final Set<String> moduleNames;

  public ErraiAptExportedTypes(final MetaClass erraiAppAnnotatedMetaClass,
          final Elements elements,
          final AnnotatedSourceElementsFinder annotatedSourceElementsFinder,
          final ResourceFilesFinder resourceFilesFinder) {

    this.elements = elements;
    this.resourcesFilesFinder = resourceFilesFinder;
    this.annotatedSourceElementsFinder = annotatedSourceElementsFinder;
    this.aptErraiAppConfiguration = new AptErraiAppConfiguration(erraiAppAnnotatedMetaClass);
    this.moduleNames = aptErraiAppConfiguration.modules().stream().map(MetaClass::getCanonicalName).collect(toSet());

    // Loads all exported types from ExportFiles
    exportedClassesByAnnotationClassName = getExportedTypesFromExportFilesInExportFilesPackage();

    // Because annotation processors execution order is random we have to look for local exportable types one more time
    addLocalExportableTypesWhichHaveNotBeenExported();
  }

  private Map<String, Set<TypeMirror>> getExportedTypesFromExportFilesInExportFilesPackage() {
    return exportFilesPackageElement(elements).map(this::getExportedTypesFromExportFiles).orElseGet(HashMap::new);
  }

  private Map<String, Set<TypeMirror>> getExportedTypesFromExportFiles(final PackageElement packageElement) {
    return packageElement.getEnclosedElements()
            .stream()
            .filter(e -> localErraiAppContainsModuleOfExportFileElement(e.getSimpleName().toString()))
            .collect(groupingBy(this::getAnnotationNameFromExportFileElement,
                    flatMapping(this::getExportedTypesFromExportFileElement, toSet())));
  }

  private boolean localErraiAppContainsModuleOfExportFileElement(final String exportFileClassSimpleName) {
    if (aptErraiAppConfiguration.local()) {
      return moduleNames.contains(
              ExportFileName.decodeModuleClassCanonicalNameFromExportFileSimpleName(exportFileClassSimpleName));
    }
    return true;
  }

  private String getAnnotationNameFromExportFileElement(final Element e) {
    final TypeElement typeElement = (TypeElement) e;
    return ExportFileName.decodeAnnotationClassNameFromExportFileName(typeElement.getQualifiedName().toString());
  }

  private Stream<TypeMirror> getExportedTypesFromExportFileElement(final Element exportFile) {
    return exportFile.getEnclosedElements().stream().filter(e -> e.getKind().isField()).map(Element::asType);
  }

  private void addLocalExportableTypesWhichHaveNotBeenExported() {
    log.info("Exporting local exportable types..");
    final Set<TypeElement> allExportableAnnotations = findAnnotatedMetaClasses(ErraiExportingStrategies.class).stream()
            .flatMap(c -> Arrays.stream(c.getDeclaredMethods()))
            .map(m -> m.getAnnotation(ErraiExportingStrategy.class))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(MetaAnnotation::<String>value)
            .map(APTClassUtil::getTypeElement)
            .collect(Collectors.toSet());

    allExportableAnnotations.add(getTypeElement(ErraiGenerator.class.getCanonicalName()));
    allExportableAnnotations.add(getTypeElement(ErraiModule.class.getCanonicalName()));
    allExportableAnnotations.add(getTypeElement(ErraiApp.class.getCanonicalName()));

    log.debug("Exporting local exportable types using annotations:");
    log.debug(allExportableAnnotations.stream().map(s -> s.getQualifiedName().toString()).collect(joining("\n")));

    this.addAllExportableTypes(allExportableAnnotations);
  }

  private void addAllExportableTypes(final Set<TypeElement> allExportableAnnotations) {
    getLocalExportableTypesByItsAnnotationName(allExportableAnnotations).entrySet()
            .stream()
            .filter(e -> !e.getValue().isEmpty())
            .forEach(this::addExportableLocalTypes);
  }

  private Map<String, Set<TypeMirror>> getLocalExportableTypesByItsAnnotationName(final Set<TypeElement> allExportableAnnotations) {
    return getLocalExportFileGenerator(allExportableAnnotations).createExportFiles()
            .stream()
            .filter(s -> localErraiAppContainsModuleOfExportFileElement(s.simpleClassName()))
            .collect(groupingBy(exportFile -> exportFile.annotation().getQualifiedName().toString(),
                    flatMapping(this::getExportedTypesFromExportFile, toSet())));
  }

  private ExportFileGenerator getLocalExportFileGenerator(final Set<TypeElement> allExportableAnnotations) {
    return new ExportFileGenerator("localExportableTypes", allExportableAnnotations, annotatedSourceElementsFinder,
            buildExportingStrategies());
  }

  private ExportingStrategies buildExportingStrategies() {
    return new ErraiExportingStrategiesFactory(elements).buildFrom(
            findAnnotatedMetaClasses(ErraiExportingStrategies.class));
  }

  private void addExportableLocalTypes(final Map.Entry<String, Set<TypeMirror>> entry) {
    final String annotationName = entry.getKey();
    final Set<TypeMirror> mappedTypes = entry.getValue();
    exportedClassesByAnnotationClassName.putIfAbsent(annotationName, new HashSet<>());
    exportedClassesByAnnotationClassName.get(annotationName).addAll(mappedTypes);
  }

  private Stream<TypeMirror> getExportedTypesFromExportFile(final ExportFile exportFile) {
    return exportFile.exportedTypes().stream();
  }

  public Set<MetaClass> findAnnotatedMetaClasses(final Class<? extends Annotation> annotation) {
    return exportedClassesByAnnotationClassName.getOrDefault(annotation.getName(), emptySet())
            .stream()
            .filter(s -> s.getKind().equals(TypeKind.DECLARED))
            .map(APTClass::new)
            .collect(toSet());
  }

  public ResourceFilesFinder resourceFilesFinder() {
    return resourcesFilesFinder;
  }

  public AptErraiAppConfiguration erraiAppConfiguration() {
    return aptErraiAppConfiguration;
  }

  // Java 9 will implement this method, so when it's released and we upgrade, this can be removed.
  private static <T, U, A, R> Collector<T, ?, R> flatMapping(Function<? super T, ? extends Stream<? extends U>> mapper,
          Collector<? super U, A, R> downstream) {

    BiConsumer<A, ? super U> downstreamAccumulator = downstream.accumulator();
    return Collector.of(downstream.supplier(), (r, t) -> {
              try (Stream<? extends U> result = mapper.apply(t)) {
                if (result != null) {
                  result.sequential().forEach(u -> downstreamAccumulator.accept(r, u));
                }
              }
            }, downstream.combiner(), downstream.finisher(),
            downstream.characteristics().toArray(new Collector.Characteristics[0]));
  }
}
