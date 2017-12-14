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

import org.jboss.errai.codegen.InnerClass;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.impl.apt.APTClass;
import org.jboss.errai.common.apt.generator.ExportedTypesFromSource;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.jboss.errai.common.apt.exportfile.ErraiAptPackages.exportFilesPackagePath;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class ExportFile {

  private final ExportedTypesFromSource exportedTypesFromSource;
  private final String simpleClassName;

  public ExportFile(final String erraiModuleNamespace, final ExportedTypesFromSource exportedTypesFromSource) {
    this.exportedTypesFromSource = exportedTypesFromSource;
    this.simpleClassName = erraiModuleNamespace + "_ExportFile";
  }

  public String generateSource() {
    final ClassStructureBuilder<?> classBuilder = ClassBuilder.define(getFullClassName()).publicScope().body();

    exportedTypesFromSource.exportableAnnotations()
            .stream()
            .map(this::createInnerClassDefinition)
            .forEach(classBuilder::declaresInnerClass);

    return classBuilder.toJavaString();
  }

  private InnerClass createInnerClassDefinition(final TypeElement annotation) {
    final String classFqcn = annotation.getQualifiedName().toString().replace(".", "_");
    final ClassStructureBuilder<?> classBuilder = ClassBuilder.define(classFqcn).publicScope().body();

    exportedTypesFromSource.findAnnotatedSourceElements(annotation)
            .stream()
            .map(Element::asType)
            .map(APTClass::new)
            .map(APTClass::getErased)
            .distinct()
            .forEach(exportedType -> classBuilder.publicField(generateFieldName(exportedType), exportedType).finish());

    return new InnerClass(classBuilder.getClassDefinition());
  }

  private String generateFieldName(final MetaClass exportedType) {

    if (exportedType.isPrimitive()) {
      return exportedType.getName() + "_";
    }

    return exportedType.getCanonicalName().replace(".", "_");
  }

  public String getFullClassName() {
    return exportFilesPackagePath() + "." + simpleClassName;
  }

  public Set<TypeMirror> exportedTypes() {
    return exportedTypesFromSource.exportableAnnotations()
            .stream()
            .flatMap(s -> exportedTypesFromSource.findAnnotatedSourceElements(s).stream())
            .map(Element::asType)
            .collect(toSet());
  }
}
