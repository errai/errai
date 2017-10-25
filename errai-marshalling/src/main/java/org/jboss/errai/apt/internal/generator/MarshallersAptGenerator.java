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

import org.jboss.errai.common.apt.ErraiAptExportedTypes;
import org.jboss.errai.common.apt.ErraiAptGenerators;
import org.jboss.errai.common.apt.configuration.AptErraiConfiguration;
import org.jboss.errai.common.apt.generator.ErraiAptGeneratedSourceFile;
import org.jboss.errai.config.ErraiConfiguration;
import org.jboss.errai.marshalling.rebind.MarshallerGeneratorFactory;

import java.util.Arrays;
import java.util.Collection;

import static org.jboss.errai.marshalling.rebind.MarshallerOutputTarget.GWT;
import static org.jboss.errai.marshalling.rebind.MarshallerOutputTarget.Java;
import static org.jboss.errai.marshalling.rebind.MarshallersGenerator.CLIENT_CLASS_NAME;
import static org.jboss.errai.marshalling.rebind.MarshallersGenerator.CLIENT_PACKAGE_NAME;
import static org.jboss.errai.marshalling.rebind.MarshallersGenerator.SERVER_CLASS_NAME;
import static org.jboss.errai.marshalling.rebind.MarshallersGenerator.SERVER_PACKAGE_NAME;

/**
 * IMPORTANT: Do not move this class. ErraiAppAptGenerator depends on it being in this exact package.
 *
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class MarshallersAptGenerator extends ErraiAptGenerators.MultipleFiles {

  // IMPORTANT: Do not remove. ErraiAppAptGenerator depends on this constructor
  public MarshallersAptGenerator(final ErraiAptExportedTypes exportedTypes) {
    super(exportedTypes);
  }

  @Override
  public Collection<ErraiAptGeneratedSourceFile> files() {

    final ErraiAptGeneratedSourceFile server = new ErraiAptGeneratedSourceFile(SERVER_PACKAGE_NAME, SERVER_CLASS_NAME,
            MarshallerGeneratorFactory.getFor(null, Java, erraiConfiguration(), metaClassFinder())
                    .generate(SERVER_PACKAGE_NAME, SERVER_CLASS_NAME));

    final ErraiAptGeneratedSourceFile client = new ErraiAptGeneratedSourceFile(CLIENT_PACKAGE_NAME, CLIENT_CLASS_NAME,
            MarshallerGeneratorFactory.getFor(null, GWT, erraiConfiguration(), metaClassFinder())
                    .generate(CLIENT_PACKAGE_NAME, CLIENT_CLASS_NAME));

    return Arrays.asList(client, server);
  }

}
