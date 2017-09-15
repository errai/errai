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

package org.jboss.errai.common.apt.configuration;

import org.jboss.errai.common.apt.AnnotatedSourceElementsFinder;
import org.jboss.errai.common.apt.exportfile.ExportFile;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

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

    final Set<Element> exportedTypes = findAnnotatedClassesAndInterfaces(annotation);

    if (exportedTypes.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(new ExportFile(erraiModuleUniqueNamespace(), annotation, exportedTypes));
  }

  Set<Element> findAnnotatedClassesAndInterfaces(final TypeElement annotationTypeElement) {
    return annotatedSourceElementsFinder.findSourceElementsAnnotatedWith(annotationTypeElement)
            .stream()
            .filter(e -> e.getKind().isClass() || e.getKind().isInterface())
            .filter(this::isPartOfModule)
            .collect(toSet());
  }

  private boolean isPartOfModule(final Element element) {
    //FIXME: improve restrictions
    // Here is where the .gwt.xml file processing will be.
    // For now, we're restricting classes under the package of
    // an errai module (located on the same package as an .gwt.xml file)
    return ((TypeElement) element).getQualifiedName().toString().contains(packageName + ".");
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

  private PackageElement getPackage(final Element erraiModuleClassElement) {
    final Element enclosingElement = erraiModuleClassElement.getEnclosingElement();

    if (enclosingElement.getKind().equals(ElementKind.PACKAGE)) {
      return ((PackageElement) enclosingElement);
    } else {
      return getPackage(enclosingElement);
    }
  }
}
