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

package org.jboss.errai.codegen.meta;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public abstract class MetaAnnotation {

  @SuppressWarnings("unchecked")
  public <V> V value() {
    return (V) valueAsArray(Object[].class);
  }

  @SuppressWarnings("unchecked")
  public <V> V value(final String attributeName) {
    return (V) valueAsArray(attributeName, Object[].class);
  }

  public <V> V valueAsArray(final Class<V> arrayClass) {
    return valueAsArray("value", arrayClass);
  }

  public abstract <V> V valueAsArray(final String attributeName, final Class<V> arrayClass);

  public abstract MetaClass annotationType();

  public boolean instanceOf(final Class<? extends Annotation> clazz) {
    return clazz.getCanonicalName().equals(annotationType().getCanonicalName());
  }

  @Override
  public boolean equals(final Object o) {
    return o instanceof MetaAnnotation
            && annotationType().equals(((MetaAnnotation) o).annotationType())
            && values().equals(((MetaAnnotation) o).values());
  }

  @Override
  public String toString() {
    return "@" + annotationType().getName();
  }

  public abstract Map<String, Object> values();
}
