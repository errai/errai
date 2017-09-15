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

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.impl.apt.APTClass;
import org.jboss.errai.common.apt.exportfile.ExportFile;
import org.jboss.errai.common.apt.exportfile.ExportFileName;
import org.jboss.errai.common.apt.generator.ExportFileGenerator;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;
import static org.jboss.errai.common.apt.ErraiAptPackages.exportFilesPackageElement;
import static org.jboss.errai.common.apt.ErraiAptPackages.exportedAnnotationsPackageElement;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public final class ErraiAptExportedTypes {

  private final Map<String, Set<TypeMirror>> exportedClassesByAnnotationClassName;

  private final Types types;
  private final Elements elements;
  private final AnnotatedSourceElementsFinder annotatedSourceElementsFinder;

  public ErraiAptExportedTypes(final Types types,
          final Elements elements,
          final AnnotatedSourceElementsFinder annotatedSourceElementsFinder) {

    this.types = types;
    this.elements = elements;
    this.annotatedSourceElementsFinder = annotatedSourceElementsFinder;

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
            .collect(groupingBy(this::getAnnotationNameFromExportFileElement,
                    flatMapping(this::getExportedTypesFromExportFileElement, toSet())));
  }

  private String getAnnotationNameFromExportFileElement(final Element e) {
    return ExportFileName.decodeAnnotationClassNameFromExportFileName(e.asType().toString());
  }

  private Stream<TypeMirror> getExportedTypesFromExportFileElement(final Element exportFile) {
    return exportFile.getEnclosedElements().stream().filter(e -> e.getKind().isField()).map(Element::asType);
  }

  private void addLocalExportableTypesWhichHaveNotBeenExported() {
    System.out.println("Exporting local exportable types..");
    exportedAnnotationsPackageElement(elements).map(this::getAllExportedAnnotations)
            .ifPresent(this::addAllExportableTypes);
  }

  private Set<TypeElement> getAllExportedAnnotations(final PackageElement exportedAnnotationsPackageElement) {
    return exportedAnnotationsPackageElement.getEnclosedElements()
            .stream()
            .flatMap(this::getExportedTypesFromExportFileElement)
            .map(types::asElement)
            .map(s -> (TypeElement) s)
            .collect(toSet());
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
            .collect(groupingBy(this::getAnnotationNameFromExportFile,
                    flatMapping(this::getExportedTypesFromExportFile, toSet())));
  }

  private ExportFileGenerator getLocalExportFileGenerator(final Set<TypeElement> allExportableAnnotations) {
    return new ExportFileGenerator("localExportableTypes", allExportableAnnotations, annotatedSourceElementsFinder);
  }

  private void addExportableLocalTypes(final Map.Entry<String, Set<TypeMirror>> entry) {
    final String annotationName = entry.getKey();
    final Set<TypeMirror> mappedTypes = entry.getValue();
    exportedClassesByAnnotationClassName.putIfAbsent(annotationName, new HashSet<>());
    exportedClassesByAnnotationClassName.get(annotationName).addAll(mappedTypes);
  }

  private String getAnnotationNameFromExportFile(final ExportFile exportFile) {
    return exportFile.annotation.getQualifiedName().toString();
  }

  private Stream<TypeMirror> getExportedTypesFromExportFile(final ExportFile exportFile) {
    return exportFile.exportedTypes.stream().map(Element::asType);
  }

  public Collection<MetaClass> findAnnotatedMetaClasses(final Class<? extends Annotation> annotation) {
    return exportedClassesByAnnotationClassName.getOrDefault(annotation.getName(), emptySet())
            .stream()
            .filter(s -> s.getKind().equals(TypeKind.DECLARED))
            .map(APTClass::new)
            .collect(toSet());
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
