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
import org.jboss.errai.codegen.meta.impl.apt.APTClassUtil;
import org.jboss.errai.common.apt.AnnotatedSourceElementsFinder;
import org.jboss.errai.common.apt.exportfile.ExportFile;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static javax.lang.model.element.ElementKind.CONSTRUCTOR;
import static javax.lang.model.element.ElementKind.METHOD;
import static javax.lang.model.element.ElementKind.PARAMETER;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class ErraiModule {

  private final String camelCaseErraiModuleName;
  private final Element erraiModuleClassElement;
  private final AnnotatedSourceElementsFinder annotatedSourceElementsFinder;
  private final String packageName;

  public ErraiModule(final String camelCaseErraiModuleName,
          final Element erraiModuleClassElement,
          final AnnotatedSourceElementsFinder annotatedSourceElementsFinder) {

    this.camelCaseErraiModuleName = camelCaseErraiModuleName;
    this.erraiModuleClassElement = erraiModuleClassElement;
    this.annotatedSourceElementsFinder = annotatedSourceElementsFinder;
    this.packageName = getPackageName();
  }

  public Stream<ExportFile> exportFiles(final Set<? extends TypeElement> exportableAnnotations) {
    return exportableAnnotations.stream().map(this::newExportFile).filter(Optional::isPresent).map(Optional::get);
  }

  Optional<ExportFile> newExportFile(final TypeElement annotation) {

    final Set<Element> exportedTypes = findAnnotatedElements(annotation);

    if (exportedTypes.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(new ExportFile(erraiModuleUniqueNamespace(), annotation, exportedTypes));
  }

  Set<Element> findAnnotatedElements(final TypeElement annotationTypeElement) {
    return annotatedSourceElementsFinder.findSourceElementsAnnotatedWith(annotationTypeElement)
            .stream()
            .filter(this::isPartOfModule)
            .flatMap(this::getTypeElement)
            .collect(toSet());
  }

  private Stream<? extends Element> getTypeElement(final Element element) {
    if (element.getKind().isClass() || element.getKind().isInterface()) {
      return Stream.of(element);
    } else if (element.getKind().isField()) {
      return Stream.of(APTClassUtil.types.asElement(element.asType()));
    } else if (element.getKind().equals(METHOD) || element.getKind().equals(CONSTRUCTOR)) {
      return ((ExecutableElement) element).getParameters().stream();
    } else if (element.getKind().equals(PARAMETER)) {
      return Stream.of(element);
    } else {
      return Stream.of();
    }
  }

  private boolean isPartOfModule(final Element element) {
    // Exporting only classes on subpackages of an @ErraiModule is not ideal, but works for now.
    if (element.getKind().isClass() || element.getKind().isInterface()) {
      return isUnderModulePackage((Symbol) element);
    } else if (element.getKind().isField()) {
      return isUnderModulePackage(((Symbol.VarSymbol) element).owner);
    } else if (element.getKind().equals(PARAMETER)) {
      return isUnderModulePackage(((Symbol) element).getEnclosingElement().getEnclosingElement());
    } else {
      return false;
    }
  }

  private boolean isUnderModulePackage(final Symbol element) {
    final String elementQualifiedName = element.getQualifiedName().toString();
    return !elementQualifiedName.contains(".server.") && elementQualifiedName.contains(packageName + ".");
  }

  String erraiModuleUniqueNamespace() {

    final String moduleFullName = ((TypeElement) erraiModuleClassElement).getQualifiedName()
            .toString()
            .replace(".", "_");

    return moduleFullName + "__" + camelCaseErraiModuleName;
  }

  private String getPackageName() {
    return getPackage(erraiModuleClassElement).getQualifiedName().toString();
  }

  private PackageElement getPackage(final Element element) {
    final Element enclosingElement = element.getEnclosingElement();

    if (enclosingElement.getKind().equals(ElementKind.PACKAGE)) {
      return ((PackageElement) enclosingElement);
    } else {
      return getPackage(enclosingElement);
    }
  }
}
