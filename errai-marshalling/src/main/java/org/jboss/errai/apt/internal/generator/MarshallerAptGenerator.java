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

package org.jboss.errai.apt.internal.generator;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.common.apt.ErraiAptExportedTypes;
import org.jboss.errai.common.apt.ErraiAptGenerators;
import org.jboss.errai.common.apt.configuration.AptErraiConfiguration;
import org.jboss.errai.common.apt.generator.ErraiAptGeneratedSourceFile;
import org.jboss.errai.config.ErraiConfiguration;
import org.jboss.errai.marshalling.rebind.MarshallerGenerator;
import org.jboss.errai.marshalling.rebind.MarshallerGeneratorFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.jboss.errai.marshalling.rebind.MarshallerGenerator.PACKAGE_NAME;

/**
 * IMPORTANT: Do not move this class. ErraiAppAptGenerator depends on it being in this exact package.
 *
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class MarshallerAptGenerator extends ErraiAptGenerators.MultipleFiles {

  private static final List<MetaClass> exposedClasses = new ArrayList<>();

  // IMPORTANT: Do not remove. ErraiAppAptGenerator depends on this constructor
  public MarshallerAptGenerator(final ErraiAptExportedTypes exportedTypes) {
    super(exportedTypes);
  }

  @Override
  public Collection<ErraiAptGeneratedSourceFile> files() {
    final Set<ErraiAptGeneratedSourceFile> files = new HashSet<>();

    // We need to iterate using an index here because we add elements to exposedClasses during iteration
    for (int i = 0; i < exposedClasses.size(); i++) {
      final MetaClass metaClass = exposedClasses.get(i);
      files.add(getGeneratedFile(metaClass));
    }

    return files;
  }

  private ErraiAptGeneratedSourceFile getGeneratedFile(final MetaClass type) {
    final String classSimpleName = MarshallerGeneratorFactory.getMarshallerImplClassName(type, true);
    final String generatedSource = generateSource(erraiConfiguration(), type);
    return new ErraiAptGeneratedSourceFile(erraiConfiguration(), PACKAGE_NAME, classSimpleName, generatedSource);
  }

  private String generateSource(final ErraiConfiguration erraiConfiguration, final MetaClass type) {
    try {
      return new MarshallerGenerator().generateMarshaller(null, type, erraiConfiguration);
    } catch (final Exception e) {
      System.out.println("Error generating " + type.toString());
      e.printStackTrace();
      return "";
    }
  }

  @Override
  public int priority() {
    //Has to run after MarshallersGenerator because it sets part of the exposedClasses
    return 1;
  }

  public static void addExposedClass(final MetaClass type) {
    if (!exposedClasses.contains(type)) {
      exposedClasses.add(type);
    }
  }
}
