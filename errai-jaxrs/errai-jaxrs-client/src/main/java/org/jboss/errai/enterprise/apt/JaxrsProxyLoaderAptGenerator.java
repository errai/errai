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

package org.jboss.errai.enterprise.apt;

import org.jboss.errai.codegen.meta.MetaAnnotation;
import org.jboss.errai.common.apt.exportfile.ExportedTypesFromExportFiles;
import org.jboss.errai.common.apt.ErraiAptGenerators;
import org.jboss.errai.common.apt.generator.ErraiAptGeneratedSourceFile;
import org.jboss.errai.common.configuration.ErraiGenerator;
import org.jboss.errai.enterprise.rebind.JaxrsProxyLoaderGenerator;

import java.util.Collection;

import static java.util.stream.Collectors.toList;
import static org.jboss.errai.common.apt.generator.ErraiAptGeneratedSourceFile.Type.CLIENT;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
@ErraiGenerator
public final class JaxrsProxyLoaderAptGenerator extends ErraiAptGenerators.SingleFile {

  private final JaxrsProxyLoaderGenerator jaxrsProxyLoaderGenerator;

  // IMPORTANT: Do not remove. ErraiAppAptGenerator depends on this constructor
  public JaxrsProxyLoaderAptGenerator(final ExportedTypesFromExportFiles exportedTypes) {
    super(exportedTypes);
    this.jaxrsProxyLoaderGenerator = new JaxrsProxyLoaderGenerator();
  }

  @Override
  public String generate() {
    return jaxrsProxyLoaderGenerator.generate(metaClassFinder(), isIOCModuleInherited(), this::filterAnnotations,
            getResolvedFullyQualifiedClassName());
  }

  private Boolean isIOCModuleInherited() {
  /* Ideally we would parse GWT Module files to check if the IOC Module is inherited,
     but just 'true' does the job for now */
    return true;
  }

  private Collection<MetaAnnotation> filterAnnotations(final Collection<MetaAnnotation> annotations) {
    /* Ideally we would parse GWT Module files to check which classes are allowed
       in client code, but this hack does the job for now. */
    return annotations.stream().filter(s -> {
      String packageName = s.annotationType().getPackageName();
      return !packageName.contains("server");
    }).collect(toList());
  }

  @Override
  public String getPackageName() {
    return jaxrsProxyLoaderGenerator.getPackageName();
  }

  @Override
  public String getClassSimpleName() {
    return jaxrsProxyLoaderGenerator.getClassSimpleName();
  }

  @Override
  public ErraiAptGeneratedSourceFile.Type getType() {
    return CLIENT;
  }
}
