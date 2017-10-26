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

import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.impl.apt.APTClass;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Set;

import static org.jboss.errai.common.apt.ErraiAptPackages.exportFilesPackagePath;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class ExportFile {

  private final String erraiModuleNamespace;
  private final TypeElement annotation;
  private final Set<TypeMirror> exportedTypes;
  private final String simpleClassName;

  public ExportFile(final String erraiModuleNamespace, final TypeElement annotation, final Set<TypeMirror> exportedTypes) {
    this.erraiModuleNamespace = erraiModuleNamespace;
    this.annotation = annotation;
    this.exportedTypes = exportedTypes;
    this.simpleClassName = ExportFileName.encodeAnnotationNameAsExportFileName(this);
  }

  public String generateSource() {
    final ClassStructureBuilder<?> classBuilder = ClassBuilder.define(getFullClassName()).publicScope().body();

    exportedTypes.stream()
            .map(APTClass::new)
            .map(APTClass::getErased)
            .distinct()
            .forEach(exportedType -> classBuilder.publicField(fieldName(exportedType), exportedType).finish());

    return classBuilder.toJavaString();
  }

  private String fieldName(final MetaClass exportedType) {

    if (exportedType.isPrimitive()) {
      return exportedType.getName() + "_";
    }

    return exportedType.getCanonicalName().replace(".", "_");
  }

  public String getFullClassName() {
    return exportFilesPackagePath() + "." + simpleClassName;
  }

  public String simpleClassName() {
    return simpleClassName;
  }

  public Set<TypeMirror> exportedTypes() {
    return exportedTypes;
  }

  public TypeElement annotation() {
    return annotation;
  }

  public String erraiModuleNamespace() {
    return erraiModuleNamespace;
  }
}
