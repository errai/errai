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

package org.jboss.errai.common.apt.module;

import com.sun.tools.javac.code.Symbol;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.impl.apt.APTClass;
import org.jboss.errai.codegen.meta.impl.apt.APTClassUtil;
import org.jboss.errai.common.apt.exportfile.ExportFile1;
import org.jboss.errai.common.apt.exportfile.ExportFile2;
import org.jboss.errai.common.apt.generator.AnnotatedSourceElementsFinder;
import org.jboss.errai.common.apt.configuration.AptErraiModulesConfiguration;
import org.jboss.errai.common.apt.exportfile.ExportFile;
import org.jboss.errai.common.apt.generator.ExportedTypesFromSource;
import org.jboss.errai.common.apt.strategies.ExportedElement;
import org.jboss.errai.common.apt.strategies.ExportingStrategies;
import org.jboss.errai.common.apt.strategies.ExportingStrategy;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Collections.singleton;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toSet;
import static javax.lang.model.element.ElementKind.CONSTRUCTOR;
import static javax.lang.model.element.ElementKind.METHOD;
import static javax.lang.model.element.ElementKind.PACKAGE;
import static javax.lang.model.element.ElementKind.PARAMETER;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class ErraiModule {

  private final String camelCaseErraiModuleName;
  private final MetaClass erraiModuleMetaClass;
  private final AnnotatedSourceElementsFinder annotatedSourceElementsFinder;
  private final String packageName;
  private final ExportingStrategies exportingStrategies;
  private final AptErraiModulesConfiguration moduleConfiguration;

  public ErraiModule(final String camelCaseErraiModuleName,
          final MetaClass erraiModuleMetaClass,
          final AnnotatedSourceElementsFinder annotatedSourceElementsFinder,
          final ExportingStrategies exportingStrategies) {

    this.camelCaseErraiModuleName = camelCaseErraiModuleName;
    this.erraiModuleMetaClass = erraiModuleMetaClass;
    this.annotatedSourceElementsFinder = annotatedSourceElementsFinder;
    this.exportingStrategies = exportingStrategies;

    this.packageName = erraiModuleMetaClass.getPackageName();
    this.moduleConfiguration = new AptErraiModulesConfiguration(singleton(erraiModuleMetaClass));
  }

  String erraiModuleUniqueNamespace() {
    return erraiModuleMetaClass.getCanonicalName().replace(".", "_") + "__" + camelCaseErraiModuleName;
  }

  public Stream<ExportFile> createExportFiles(final Set<? extends TypeElement> exportableAnnotations) {
    final ExportedTypesFromSource exportedTypesFromSource = new ExportedTypesFromSource();

    exportableAnnotations.stream()
            .flatMap(s -> this.findExportedElements(s).stream())
            .collect(groupingBy(ExportedElement::getAnnotation, mapping(ExportedElement::getTypeMirror, toSet())))
            .forEach((a, types) -> exportedTypesFromSource.putAll(a, types.stream().map(APTClassUtil.types::asElement).collect(toSet())));

    final Stream<ExportFile2> exportFile2 = Stream.of(
            new ExportFile2(erraiModuleUniqueNamespace(), exportedTypesFromSource));

    final Stream<ExportFile> exportFileStream = exportableAnnotations.stream()
            .flatMap(s -> this.findExportedElements(s).stream())
            .collect(groupingBy(ExportedElement::getAnnotation, mapping(ExportedElement::getTypeMirror, toSet())))
            .entrySet()
            .stream()
            .map(e -> this.newExportFile(e.getKey(), e.getValue()))
            .filter(Optional::isPresent)
            .map(Optional::get);

    return Stream.concat(exportFileStream, exportFile2);
  }

  private Optional<ExportFile> newExportFile(final TypeElement annotation, final Set<TypeMirror> types) {
    return Optional.of(types)
            .filter(s -> !s.isEmpty())
            .map(t -> new ExportFile1(erraiModuleUniqueNamespace(), annotation, types));
  }

  Set<ExportedElement> findExportedElements(final TypeElement annotationTypeElement) {
    final ExportingStrategy strategy = exportingStrategies.getStrategy(annotationTypeElement);
    return annotatedSourceElementsFinder.findSourceElementsAnnotatedWith(annotationTypeElement)
            .stream()
            .filter(this::isPartOfModule)
            .flatMap(strategy::getExportedElements)
            .filter(this::isPublic)
            .collect(toSet());
  }

  private boolean isPartOfModule(final Element element) {
    final MetaClass prospectType = new APTClass(getTypeElementOf(element).asType());
    return isIncluded(prospectType) && !isExcluded(prospectType) || prospectType.equals(erraiModuleMetaClass);
  }

  private Element getTypeElementOf(final Element element) {
    if (element.getKind().isClass() || element.getKind().isInterface()) {
      return element;
    }

    if (element.getKind().isField()) {
      return ((Symbol.VarSymbol) element).owner;
    }

    if (element.getKind().equals(METHOD) || element.getKind().equals(CONSTRUCTOR)) {
      return element.getEnclosingElement();
    }

    if (element.getKind().equals(PARAMETER)) {
      return element.getEnclosingElement().getEnclosingElement();
    }

    throw new RuntimeException("ElementKind." + element.getKind() + " is not supported.");
  }

  private boolean isIncluded(final MetaClass metaClass) {
    return matches(metaClass, moduleConfiguration.includes());
  }

  private boolean isExcluded(final MetaClass metaClass) {
    return matches(metaClass, moduleConfiguration.excludes());
  }

  private boolean matches(final MetaClass metaClass, final Set<String> patterns) {
    return patterns.stream().<Predicate<String>>map(pattern -> {
      if (pattern.endsWith(".*")) {
        // Pattern is a package name relative to the module package
        final String relativePackageName = pattern.substring(0, pattern.length() - 2);
        return a -> a.startsWith(packageName + "." + relativePackageName);
      } else {
        // Pattern is a canonical type name
        return a -> a.equals(pattern);
      }
    }).anyMatch(p -> p.test(metaClass.getCanonicalName()));
  }

  private boolean isPublic(final ExportedElement exportedElement) {

    if (exportedElement.getTypeMirror().getKind().isPrimitive()) {
      return true;
    }

    final Element element = APTClassUtil.types.asElement(exportedElement.getTypeMirror());

    if (element.getEnclosingElement().getKind().isInterface()) {
      // Inner classes of interfaces are public if its outer class is public
      return element.getEnclosingElement().getModifiers().contains(PUBLIC);
    }

    if (element.getEnclosingElement().getKind().isClass()) {
      // Inner classes of non-inner classes have to contain the public modifier to be public
      return element.getModifiers().contains(PUBLIC) && element.getEnclosingElement().getModifiers().contains(PUBLIC);
    }

    if (element.getEnclosingElement().getKind().equals(PACKAGE)) {
      // Non-inner classes and interfaces have to contain the public modifier to be public
      return element.getModifiers().contains(PUBLIC);
    }

    return false;
  }
}
