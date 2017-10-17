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
import org.jboss.errai.common.apt.generator.AptGeneratedSourceFile;
import org.jboss.errai.config.ErraiConfiguration;
import org.jboss.errai.config.MarshallingConfiguration;
import org.jboss.errai.marshalling.rebind.MarshallerGenerator;
import org.jboss.errai.marshalling.rebind.MarshallerGeneratorFactory;

import java.util.Collection;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.jboss.errai.marshalling.rebind.MarshallerGenerator.PACKAGE_NAME;

/**
 * IMPORTANT: Do not move this class. ErraiAppAptGenerator depends on it being in this exact package.
 *
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class MarshallerAptGenerator extends ErraiAptGenerators.MultipleFiles {

  // IMPORTANT: Do not remove. ErraiAppAptGenerator depends on this constructor
  public MarshallerAptGenerator(final ErraiAptExportedTypes exportedTypes) {
    super(exportedTypes);
  }

  @Override
  public Collection<AptGeneratedSourceFile> files() {
    final ErraiConfiguration erraiConfiguration = new AptErraiConfiguration(metaClassFinder());
    return MarshallingConfiguration.allExposedPortableTypes(erraiConfiguration, metaClassFinder())
            .stream()
            .map(type -> getGeneratedFile(erraiConfiguration, type))
            .collect(toSet());
  }

  private AptGeneratedSourceFile getGeneratedFile(final ErraiConfiguration erraiConfiguration, final MetaClass type) {
    final String classSimpleName = MarshallerGeneratorFactory.getMarshallerImplClassName(type, true);
    final String generatedSource = generateSource(erraiConfiguration, type);
    return new AptGeneratedSourceFile(PACKAGE_NAME, classSimpleName, generatedSource);
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
    //Has to run after MarshallersGenerator because it sets the exposedClasses
    return 1;
  }
}