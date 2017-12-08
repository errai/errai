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

package org.jboss.errai.marshalling.apt;

import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.common.apt.ErraiAptGenerators;
import org.jboss.errai.common.apt.exportfile.ExportedTypesFromExportFiles;
import org.jboss.errai.common.apt.generator.ErraiAptGeneratedSourceFile;
import org.jboss.errai.common.configuration.ErraiGenerator;
import org.jboss.errai.config.MetaClassFinder;
import org.jboss.errai.marshalling.client.api.annotations.ServerMarshaller;
import org.jboss.errai.marshalling.rebind.DefinitionsFactorySingleton;
import org.jboss.errai.marshalling.rebind.MarshallerGeneratorFactory;
import org.jboss.errai.marshalling.server.marshallers.ServerClassMarshaller;

import static java.util.Collections.singleton;
import static org.jboss.errai.common.apt.generator.ErraiAptGeneratedSourceFile.Type.SHARED;
import static org.jboss.errai.common.configuration.Target.JAVA;
import static org.jboss.errai.marshalling.rebind.MarshallerOutputTarget.Java;
import static org.jboss.errai.marshalling.rebind.MarshallersGenerator.SERVER_CLASS_NAME;
import static org.jboss.errai.marshalling.rebind.MarshallersGenerator.SERVER_PACKAGE_NAME;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
@ErraiGenerator(targets = { JAVA })
public class ServerMarshallersAptGenerator extends ErraiAptGenerators.SingleFile {

  // IMPORTANT: Do not remove. ErraiAppAptGenerator depends on this constructor
  public ServerMarshallersAptGenerator(final ExportedTypesFromExportFiles exportedTypes) {
    super(exportedTypes);
  }

  @Override
  public String generate() {

    DefinitionsFactorySingleton.reset();

    final MetaClassFinder metaClassFinder = metaClassFinder().extend(ServerMarshaller.class,
            () -> singleton(MetaClassFactory.get(ServerClassMarshaller.class)));

    return MarshallerGeneratorFactory.getFor(null, Java, erraiConfiguration(), metaClassFinder)
            .generate(getPackageName(), getResolvedClassSimpleName());
  }

  @Override
  public String getPackageName() {
    return SERVER_PACKAGE_NAME;
  }

  @Override
  public String getClassSimpleName() {
    return SERVER_CLASS_NAME;
  }

  @Override
  public ErraiAptGeneratedSourceFile.Type getType() {
    return SHARED;
  }

  @Override
  public int priority() {
    //Has to run after the MarshallerAptGenerator
    return 2;
  }
}
