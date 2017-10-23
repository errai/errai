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

package org.jboss.errai.marshalling.rebind;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.marshalling.client.marshallers.BigDecimalMarshaller;
import org.jboss.errai.marshalling.client.marshallers.BigIntegerMarshaller;
import org.jboss.errai.marshalling.client.marshallers.BooleanMarshaller;
import org.jboss.errai.marshalling.client.marshallers.ByteMarshaller;
import org.jboss.errai.marshalling.client.marshallers.CharacterMarshaller;
import org.jboss.errai.marshalling.client.marshallers.DateMarshaller;
import org.jboss.errai.marshalling.client.marshallers.DoubleMarshaller;
import org.jboss.errai.marshalling.client.marshallers.FloatMarshaller;
import org.jboss.errai.marshalling.client.marshallers.IntegerMarshaller;
import org.jboss.errai.marshalling.client.marshallers.LinkedHashSetMarshaller;
import org.jboss.errai.marshalling.client.marshallers.LinkedListMarshaller;
import org.jboss.errai.marshalling.client.marshallers.LinkedMapMarshaller;
import org.jboss.errai.marshalling.client.marshallers.ListMarshaller;
import org.jboss.errai.marshalling.client.marshallers.LongMarshaller;
import org.jboss.errai.marshalling.client.marshallers.MapMarshaller;
import org.jboss.errai.marshalling.client.marshallers.ObjectMarshaller;
import org.jboss.errai.marshalling.client.marshallers.OptionalMarshaller;
import org.jboss.errai.marshalling.client.marshallers.PriorityQueueMarshaller;
import org.jboss.errai.marshalling.client.marshallers.QueueMarshaller;
import org.jboss.errai.marshalling.client.marshallers.SQLDateMarshaller;
import org.jboss.errai.marshalling.client.marshallers.SetMarshaller;
import org.jboss.errai.marshalling.client.marshallers.ShortMarshaller;
import org.jboss.errai.marshalling.client.marshallers.SortedMapMarshaller;
import org.jboss.errai.marshalling.client.marshallers.SortedSetMarshaller;
import org.jboss.errai.marshalling.client.marshallers.StringBufferMarshaller;
import org.jboss.errai.marshalling.client.marshallers.StringBuilderMarshaller;
import org.jboss.errai.marshalling.client.marshallers.StringMarshaller;
import org.jboss.errai.marshalling.client.marshallers.TimeMarshaller;
import org.jboss.errai.marshalling.client.marshallers.TimestampMarshaller;
import org.jboss.errai.marshalling.rebind.mappings.builtin.StackTraceElementDefinition;
import org.jboss.errai.marshalling.rebind.mappings.builtin.ThrowableDefinition;
import org.jboss.errai.marshalling.server.marshallers.ServerClassMarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
class MarshallingOsgiEnvironmentHelper {

  private static final Logger log = LoggerFactory.getLogger(MarshallingOsgiEnvironmentHelper.class);

  static Set<MetaClass> getOsgiEnvironmentCustomMappings() {
    // This should only happen in OSGI environments where we can't get classpath URLs
    log.warn("Unable to scan classpath for CustomMappings. Falling back to default.");

    final Set<Class<?>> scannedMappings = new HashSet<>();
    scannedMappings.add(ThrowableDefinition.class);
    scannedMappings.add(StackTraceElementDefinition.class);

    return scannedMappings.stream().map(MetaClassFactory::getUncached).collect(toSet());
  }

  static Set<MetaClass> getOsgiEnvironmentServerMarshallers() {
    // This should only happen in OSGI environments where we can't get classpath URLs
    log.warn("Unable to scan classpath for ServerMarshallers. Falling back to default.");

    Set<Class<?>> serverMarshallers = new HashSet<>();
    serverMarshallers.add(BigDecimalMarshaller.class);
    serverMarshallers.add(BigIntegerMarshaller.class);
    serverMarshallers.add(BooleanMarshaller.class);
    serverMarshallers.add(ByteMarshaller.class);
    serverMarshallers.add(CharacterMarshaller.class);
    serverMarshallers.add(DateMarshaller.class);
    serverMarshallers.add(DoubleMarshaller.class);
    serverMarshallers.add(FloatMarshaller.class);
    serverMarshallers.add(IntegerMarshaller.class);
    serverMarshallers.add(LinkedHashSetMarshaller.class);
    serverMarshallers.add(LinkedListMarshaller.class);
    serverMarshallers.add(LinkedMapMarshaller.class);
    serverMarshallers.add(ListMarshaller.class);
    serverMarshallers.add(LongMarshaller.class);
    serverMarshallers.add(MapMarshaller.class);
    serverMarshallers.add(ObjectMarshaller.class);
    serverMarshallers.add(PriorityQueueMarshaller.class);
    serverMarshallers.add(QueueMarshaller.class);
    serverMarshallers.add(SetMarshaller.class);
    serverMarshallers.add(ShortMarshaller.class);
    serverMarshallers.add(SortedMapMarshaller.class);
    serverMarshallers.add(SortedSetMarshaller.class);
    serverMarshallers.add(SQLDateMarshaller.class);
    serverMarshallers.add(StringBufferMarshaller.class);
    serverMarshallers.add(StringBuilderMarshaller.class);
    serverMarshallers.add(StringMarshaller.class);
    serverMarshallers.add(TimeMarshaller.class);
    serverMarshallers.add(TimestampMarshaller.class);
    serverMarshallers.add(ServerClassMarshaller.class);
    serverMarshallers.add(OptionalMarshaller.class);

    return serverMarshallers.stream().map(MetaClassFactory::getUncached).collect(toSet());
  }
}
