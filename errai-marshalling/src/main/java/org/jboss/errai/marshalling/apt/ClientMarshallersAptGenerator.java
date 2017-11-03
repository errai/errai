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

import org.jboss.errai.common.apt.ErraiAptExportedTypes;
import org.jboss.errai.common.apt.ErraiAptGenerators;
import org.jboss.errai.common.configuration.ErraiGenerator;
import org.jboss.errai.marshalling.rebind.MarshallerGeneratorFactory;

import static org.jboss.errai.marshalling.rebind.MarshallerOutputTarget.GWT;
import static org.jboss.errai.marshalling.rebind.MarshallersGenerator.CLIENT_CLASS_NAME;
import static org.jboss.errai.marshalling.rebind.MarshallersGenerator.CLIENT_PACKAGE_NAME;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
@ErraiGenerator
public class ClientMarshallersAptGenerator extends ErraiAptGenerators.SingleFile {

  // IMPORTANT: Do not remove. ErraiAppAptGenerator depends on this constructor
  public ClientMarshallersAptGenerator(final ErraiAptExportedTypes exportedTypes) {
    super(exportedTypes);
  }

  @Override
  public String generate() {
    return MarshallerGeneratorFactory.getFor(null, GWT, erraiConfiguration(), metaClassFinder())
            .generate(getPackageName(), getResolvedClassSimpleName());
  }

  @Override
  public String getPackageName() {
    return CLIENT_PACKAGE_NAME;
  }

  @Override
  public String getClassSimpleName() {
    return CLIENT_CLASS_NAME;
  }
}
