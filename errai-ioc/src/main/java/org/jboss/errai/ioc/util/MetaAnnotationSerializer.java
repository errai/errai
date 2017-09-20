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

package org.jboss.errai.ioc.util;

import org.jboss.errai.codegen.meta.MetaAnnotation;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaEnum;
import org.jboss.errai.ioc.client.util.ClientAnnotationSerializer;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.StreamSupport;

import static java.util.Arrays.stream;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class MetaAnnotationSerializer {

  public static String serialize(final MetaAnnotation qualifier) {
    final StringBuilder builder = new StringBuilder(qualifier.annotationType().getFullyQualifiedName());
    final Set<String> keys = qualifier.values().keySet();

    if (!keys.isEmpty()) {
      builder.append('(');

      for (final String key : keys.stream().sorted(comparing(s -> s)).collect(toList())) {
        final String serializedValue = serializeObject(qualifier.value(key));
        builder.append(key).append('=').append(serializedValue).append(',');
      }

      builder.replace(builder.length() - 1, builder.length(), ")");
    }

    return builder.toString();
  }

  public static String[] getSerializedQualifiers(final Spliterator<MetaAnnotation> qualifiers) {
    return getSerializedQualifiers(StreamSupport.stream(qualifiers, false).collect(toList())).toArray(new String[0]);
  }

  public static Set<String> getSerializedQualifiers(final Collection<MetaAnnotation> qualifiers) {
    return qualifiers.stream().map(MetaAnnotationSerializer::serialize).collect(toSet());
  }

  private static String serializeObject(final Object value) {
    if (value.getClass().isArray()) {
      return Arrays.toString(stream((Object[]) value).map(MetaAnnotationSerializer::serializeObject).toArray());
    } else if (value instanceof MetaAnnotation) {
      return serialize((MetaAnnotation) value);
    } else if (value instanceof MetaClass) {
      return ((MetaClass) value).getFullyQualifiedName();
    } else if (value instanceof MetaEnum) {
      return ((MetaEnum) value).name();
    } else {
      return ClientAnnotationSerializer.serializeObject(value);
    }
  }
}
