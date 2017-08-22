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
import java.util.Arrays;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class RuntimeMetaAnnotation extends MetaAnnotation {

  private final Annotation annotation;

  public RuntimeMetaAnnotation(final Annotation annotation) {
    this.annotation = annotation;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V> V valueAsArray(final String attributeName, final Class<V> arrayClass) {
    try {
      final Object value = annotation.getClass().getMethod(attributeName).invoke(annotation);
      return (V) convertValue(value);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Object convertValue(final Object value) {
    if (value instanceof Class[]) {
      return Arrays.stream((Class[]) value).map(MetaClassFactory::get).toArray(MetaClass[]::new);
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
}
