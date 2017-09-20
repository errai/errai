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

import javax.enterprise.util.Nonbinding;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class RuntimeAnnotation extends MetaAnnotation {

  private final Annotation annotation;

  public RuntimeAnnotation(final Annotation annotation) {
    this.annotation = annotation;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V> V valueAsArray(final String attributeName, final Class<V> arrayClass) {
    try {
      final Method method = annotation.getClass().getMethod(attributeName);
      method.setAccessible(true);
      final Object value = method.invoke(annotation);
      return (V) convertValue(value);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Object convertValue(final Object value) {
    if (value instanceof Class[]) {
      return Arrays.stream((Class[]) value).map(MetaClassFactory::get).toArray(MetaClass[]::new);
    } else if (value instanceof Annotation[]) {
      return Arrays.stream((Annotation[]) value).map(RuntimeAnnotation::new).toArray(MetaAnnotation[]::new);
    } else if (value instanceof MetaEnum[]) {
      return Arrays.stream((Enum[]) value).map(RuntimeEnum::new).toArray(MetaEnum[]::new);
    } else if (value instanceof Class) {
      return MetaClassFactory.get((Class) value);
    } else if (value instanceof Annotation) {
      return new RuntimeAnnotation((Annotation) value);
    } else if (value instanceof Enum) {
      return new RuntimeEnum((Enum) value);
    } else {
      return value;
    }
  }

  @Override
  public MetaClass annotationType() {
    return MetaClassFactory.get(annotation.annotationType());
  }

  public Annotation getAnnotation() {
    return annotation;
  }

  @Override
  public Map<String, Object> values() {
    return Arrays.stream(annotation.annotationType().getDeclaredMethods())
            .filter(m -> !m.isAnnotationPresent(Nonbinding.class))
            .collect(toMap(Method::getName, this::methodValue));
  }

  private Object methodValue(final Method m) {
    try {
      return m.invoke(annotation);
    } catch (final Exception e) {
      throw new RuntimeException(
              "Error trying to access property [" + m.getName() + "] from annotation " + annotation.annotationType()
                      .getName(), e);
    }
  }
}
