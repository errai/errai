/*
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.cdi.server;

import org.jboss.errai.codegen.meta.impl.java.JavaReflectionMethod;
import org.jboss.errai.codegen.util.CDIAnnotationUtils;
import org.jboss.errai.enterprise.client.cdi.EventQualifierSerializer;
import org.jboss.errai.ioc.client.util.AnnotationPropertyAccessor;
import org.jboss.errai.ioc.client.util.AnnotationPropertyAccessorBuilder;
import org.jboss.errai.ioc.client.util.ClientAnnotationSerializer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

/**
 * A specialization of {@link EventQualifierSerializer} that uses the Java Reflection API to serialize qualifiers.
 *
 * This implementation is used in scenarios where a statically generated {@link EventQualifierSerializer} has not been
 * packaged in a deployment and cannot be generated when the application bootstraps.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class DynamicEventQualifierSerializer extends EventQualifierSerializer {

  @Override
  public String serialize(final Annotation qualifier) {

    if (!serializers.containsKey(qualifier.annotationType().getName())) {
      serializers.put(qualifier.annotationType().getName(), createPropertyAccessor(qualifier.annotationType()));
    }

    return super.serialize(qualifier);
  }

  private AnnotationPropertyAccessor createPropertyAccessor(final Class<? extends Annotation> annotationType) {
    final AnnotationPropertyAccessorBuilder builder = AnnotationPropertyAccessorBuilder.create();

    for (final Method method : getSerializableMethods(annotationType)) {
      builder.with(method.getName(), anno -> serializeValue(method, anno));
    }

    return builder.build();
  }

  private String serializeValue(final Method method, final Annotation annotation) {
    try {
      return ClientAnnotationSerializer.serializeObject(method.invoke(annotation));
    } catch (final Exception e) {
      throw new RuntimeException(format("Could not access '%s' property while serializing %s.", method.getName(),
              annotation.annotationType()), e);
    }
  }

  private Collection<Method> getSerializableMethods(final Class<? extends Annotation> annotationClass) {
    return Arrays.stream(annotationClass.getDeclaredMethods())
            .filter(m -> CDIAnnotationUtils.relevantForSerialization(new JavaReflectionMethod(m)))
            .collect(toList());
  }
}
