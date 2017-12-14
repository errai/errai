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

package org.jboss.errai.common.apt.exportfile;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.impl.apt.APTClass;
import org.jboss.errai.common.apt.generator.app.ResourceFilesFinder;
import org.jboss.errai.common.apt.configuration.AptErraiAppConfiguration;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static org.jboss.errai.common.apt.exportfile.ErraiAptPackages.exportFilesPackageElement;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public final class ExportedTypesFromExportFiles {

  private final Map<String, Set<TypeMirror>> exportedClassesByAnnotationClassName;

  private final ResourceFilesFinder resourcesFilesFinder;
  private final AptErraiAppConfiguration aptErraiAppConfiguration;
  private final Set<String> moduleNames;
  private final Elements elements;

  public ExportedTypesFromExportFiles(final MetaClass erraiAppMetaClass,
          final ResourceFilesFinder resourceFilesFinder,
          final Elements elements) {

    this.elements = elements;
    this.resourcesFilesFinder = resourceFilesFinder;
    this.aptErraiAppConfiguration = new AptErraiAppConfiguration(erraiAppMetaClass);
    this.moduleNames = aptErraiAppConfiguration.modules().stream().map(MetaClass::getCanonicalName).collect(toSet());
    this.exportedClassesByAnnotationClassName = this.getExportedTypesFromExportFilesInExportFilesPackage();
  }

  private Map<String, Set<TypeMirror>> getExportedTypesFromExportFilesInExportFilesPackage() {
    return exportFilesPackageElement(elements).map(this::getExportedTypesFromExportFiles).orElseGet(HashMap::new);
  }

  private Map<String, Set<TypeMirror>> getExportedTypesFromExportFiles(final PackageElement packageElement) {
    return packageElement.getEnclosedElements()
            .stream()
            .filter(e -> localErraiAppContainsModuleOfExportFileElement(e.getSimpleName().toString()))
            .flatMap(s -> s.getEnclosedElements().stream().filter(e -> e.getKind().isClass()))
            .collect(groupingBy(this::getAnnotationCanonicalNameFromExportFileInnerClass,
                    flatMapping(this::getExportedTypesFromExportFileElement, toSet())));
  }

  private boolean localErraiAppContainsModuleOfExportFileElement(final String exportFileClassSimpleName) {
    if (aptErraiAppConfiguration.local()) {
      return moduleNames.contains(exportFileClassSimpleName.split("__")[0].replace("_", "."));
    }
    return true;
  }

  private String getAnnotationCanonicalNameFromExportFileInnerClass(final Element e) {
    return e.getSimpleName().toString().replace("_", ".");
  }

  private Stream<TypeMirror> getExportedTypesFromExportFileElement(final Element exportFileInnerClassElement) {
    return exportFileInnerClassElement.getEnclosedElements().stream().filter(e -> e.getKind().isField()).map(Element::asType);
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
