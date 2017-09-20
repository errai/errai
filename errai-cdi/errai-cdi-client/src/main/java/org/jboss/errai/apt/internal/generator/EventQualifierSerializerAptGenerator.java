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
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.common.apt.ErraiAptExportedTypes;
import org.jboss.errai.common.apt.ErraiAptGenerators;
import org.jboss.errai.enterprise.client.cdi.EventQualifierSerializer;
import org.jboss.errai.enterprise.rebind.EventQualifierSerializerGenerator;

import javax.inject.Named;
import javax.inject.Qualifier;
import java.util.Set;

import static java.util.Collections.singleton;

/**
 * IMPORTANT: Do not move this class. ErraiAppAptGenerator depends on it being in this exact package.
 *
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class EventQualifierSerializerAptGenerator extends ErraiAptGenerators.SingleFile {

  private final EventQualifierSerializerGenerator eventQualifierSerializerGenerator;

  // IMPORTANT: Do not remove. ErraiAppAptGenerator depends on this constructor
  public EventQualifierSerializerAptGenerator(final ErraiAptExportedTypes exportedTypes) {
    super(exportedTypes);
    this.eventQualifierSerializerGenerator = new EventQualifierSerializerGenerator();
  }

  @Override
  public String generate() {
    return eventQualifierSerializerGenerator.generate(qualifiers());
  }

  private Set<MetaClass> qualifiers() {
    return metaClassFinder().extend(Qualifier.class, () -> singleton(MetaClassFactory.get(Named.class)))
            .findAnnotatedWith(Qualifier.class);
  }

  @Override
  public String getPackageName() {
    return EventQualifierSerializer.SERIALIZER_PACKAGE_NAME;
  }

  @Override
  public String getClassSimpleName() {
    return EventQualifierSerializer.SERIALIZER_CLASS_NAME;
  }
}
