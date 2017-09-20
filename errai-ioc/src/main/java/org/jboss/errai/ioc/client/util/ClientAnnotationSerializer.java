/*
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.ioc.client.util;

import java.lang.annotation.Annotation;
import java.util.Arrays;

import static java.util.Arrays.stream;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class ClientAnnotationSerializer {

  public static String serialize(final Annotation annotation,
          final AnnotationPropertyAccessor annotationPropertyAccessor) {

    if (annotationPropertyAccessor != null) {
      final StringBuilder builder = new StringBuilder(annotation.annotationType().getName());
      if (!annotationPropertyAccessor.accessorsByPropertyName.isEmpty()) {
        builder.append('(');

        annotationPropertyAccessor.accessorsByPropertyName.forEach((attrName, attrValueAccessor) -> {
          final String serializedValue = attrValueAccessor.apply(annotation);
          builder.append(attrName).append('=').append(serializedValue).append(',');
        });

        builder.replace(builder.length() - 1, builder.length(), ")");
      }

      return builder.toString();
    } else {
      return annotation.annotationType().getName();
    }
  }

  public static String serializeObject(final Object value) {
    if (value.getClass().isArray()) {
      return Arrays.toString(stream((Object[]) value).map(ClientAnnotationSerializer::serializeObject).toArray());
    } else if (value instanceof Class) {
      return ((Class) value).getName();
    } else {
      return String.valueOf(value);
    }
  }

}
