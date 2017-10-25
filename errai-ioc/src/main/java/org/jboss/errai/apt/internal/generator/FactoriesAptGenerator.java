/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.apt.internal.generator;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.util.GWTPrivateMemberAccessor;
import org.jboss.errai.codegen.util.PrivateAccessUtil;
import org.jboss.errai.common.apt.ErraiAptExportedTypes;
import org.jboss.errai.common.apt.ErraiAptGenerators;
import org.jboss.errai.common.apt.generator.ErraiAptGeneratedSourceFile;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.FactoryGenerator;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.ioc.graph.api.DependencyGraph;
import org.jboss.errai.ioc.rebind.ioc.graph.api.Injectable;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

/**
 * IMPORTANT: Do not move this class. ErraiAppAptGenerator depends on it being in this exact package.
 *
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class FactoriesAptGenerator extends ErraiAptGenerators.MultipleFiles {

  private static final Logger log = LoggerFactory.getLogger(FactoriesAptGenerator.class);

  private static IOCProcessingContext processingContext;

  private final FactoryGenerator factoryGenerator;

  // IMPORTANT: Do not remove. ErraiAppAptGenerator depends on this constructor
  public FactoriesAptGenerator(final ErraiAptExportedTypes exportedTypes) {
    super(exportedTypes);
    this.factoryGenerator = new FactoryGenerator();
    PrivateAccessUtil.registerPrivateMemberAccessor("jsni", new GWTPrivateMemberAccessor());
  }

  public static void setProcessingContext(final IOCProcessingContext processingContext) {
    FactoriesAptGenerator.processingContext = processingContext;
  }

  @Override
  public Collection<ErraiAptGeneratedSourceFile> files() {
    log.info("Generating Factories...");

    final DependencyGraph dependencyGraph = FactoryGenerator.assertGraphSetAndGet();
    final List<ErraiAptGeneratedSourceFile> generatedSources = StreamSupport.stream(dependencyGraph.spliterator(),
            false).map(this::generatedFactoryClass).collect(toList());

    log.info("Generated {} factories", generatedSources.size());
    return generatedSources;
  }

  private ErraiAptGeneratedSourceFile generatedFactoryClass(final Injectable injectable) {
    final MetaClass factoryMetaClass = processingContext.buildFactoryMetaClass(injectable);
    final String generatedSource = generateSources(factoryMetaClass, injectable);
    final String classSimpleName = factoryMetaClass.getName();

    return new ErraiAptGeneratedSourceFile(erraiConfiguration(), FactoryGenerator.GENERATED_PACKAGE, classSimpleName,
            generatedSource);
  }

  private String generateSources(final MetaClass factoryMetaClass, final Injectable injectable) {
    final InjectionContext injectionContext = FactoryGenerator.assertInjectionContextSetAndGet();
    try {
      return factoryGenerator.generate(factoryMetaClass.getFullyQualifiedName(), injectable, injectionContext);
    } catch (final Exception e) {
      System.out.println("Error generating " + factoryMetaClass.toString());
      e.printStackTrace();
      return "";
    }
  }

  @Override
  public int priority() {
    //Has to run after IocAptGenerator because it builds the DependencyGraph
    return 1;
  }
}
