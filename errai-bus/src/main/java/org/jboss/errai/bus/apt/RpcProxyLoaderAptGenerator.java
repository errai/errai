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

package org.jboss.errai.bus.apt;

import org.jboss.errai.bus.rebind.RpcProxyLoaderGenerator;
import org.jboss.errai.codegen.meta.MetaAnnotation;
import org.jboss.errai.common.apt.ErraiAptExportedTypes;
import org.jboss.errai.common.apt.ErraiAptGenerators;
import org.jboss.errai.common.configuration.ErraiGenerator;

import java.util.Collection;

import static java.util.stream.Collectors.toList;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
@ErraiGenerator
public final class RpcProxyLoaderAptGenerator extends ErraiAptGenerators.SingleFile {

  private final RpcProxyLoaderGenerator rpcProxyLoaderGenerator;

  // IMPORTANT: Do not remove. ErraiAppAptGenerator depends on this constructor
  public RpcProxyLoaderAptGenerator(final ErraiAptExportedTypes exportedTypes) {
    super(exportedTypes);
    this.rpcProxyLoaderGenerator = new RpcProxyLoaderGenerator();
  }

  @Override
  public String generate() {
    return rpcProxyLoaderGenerator.generate(metaClassFinder(), isIOCModuleInherited(), this::filterAnnotations,
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
    return annotations.stream().filter(s -> !s.annotationType().getPackageName().contains("server")).collect(toList());
  }

  @Override
  public String getPackageName() {
    return rpcProxyLoaderGenerator.getPackageName();
  }

  @Override
  public String getClassSimpleName() {
    return rpcProxyLoaderGenerator.getClassSimpleName();
  }
}
