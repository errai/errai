/**
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

package org.jboss.errai.enterprise.client.cdi;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.common.client.function.Function;

/**
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public abstract class EventQualifierSerializer {

  public static final String SERIALIZER_CLASS_NAME = "EventQualifierSerializerImpl";
  public static final String SERIALIZER_PACKAGE_NAME = "org.jboss.errai.cdi";

  protected static class Entry {
    private final Map<String, Function<Annotation, String>> accessorsByPropertyName;

    private Entry(final Map<String, Function<Annotation, String>> accessorsByPropertyName) {
      this.accessorsByPropertyName = accessorsByPropertyName;
    }
  }

  public static class EntryBuilder {
    private final SortedMap<String, Function<Annotation, String>> accessorsByPropertyName = new TreeMap<>();

    private EntryBuilder() {}

    public static EntryBuilder create() {
      return new EntryBuilder();
    }

    public EntryBuilder with(final String propertyName, final Function<Annotation, String> accessor) {
      accessorsByPropertyName.put(propertyName, accessor);

      return this;
    }

    public Entry build() {
      return new Entry(createOrderedPropertyMap());
    }

    private Map<String, Function<Annotation, String>> createOrderedPropertyMap() {
      return (accessorsByPropertyName.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(accessorsByPropertyName));
    }
  }

  private static EventQualifierSerializer instance;

  public static boolean isSet() {
    return instance != null;
  }

  public static void set(final EventQualifierSerializer impl) {
    if (instance == null) {
      instance = Assert.notNull(impl);
    }
    else {
      throw new RuntimeException("Cannot call set more than once.");
    }
  }

  public static EventQualifierSerializer get() {
    return Assert.notNull("Cannot call " + EventQualifierSerializer.class.getSimpleName()
            + ".get() without first setting an instance.", instance);
  }

  protected final Map<String, Entry> serializers = new HashMap<>();

  protected EventQualifierSerializer() {
  }

  public String serialize(final Annotation qualifier) {
    final Entry entry = serializers.get(qualifier.annotationType().getName());
    if (entry != null) {
      final StringBuilder builder = new StringBuilder(qualifier.annotationType().getName());
      if (!entry.accessorsByPropertyName.isEmpty()) {
        builder.append('(');
        for (final Map.Entry<String, Function<Annotation, String>> e : entry.accessorsByPropertyName.entrySet()) {
          builder.append(e.getKey())
                 .append('=')
                 .append(e.getValue().apply(qualifier))
                 .append(',');
        }
        builder.replace(builder.length()-1, builder.length(), ")");
      }

      return builder.toString();
    }
    else {
      return qualifier.annotationType().getName();
    }
  }

  public String qualifierName(final String serializedQualifier) {
    if (serializedQualifier.contains("(")) {
      return serializedQualifier.substring(0, serializedQualifier.indexOf('('));
    }
    else {
      return serializedQualifier;
    }
  }

}
