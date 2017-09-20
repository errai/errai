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

package org.jboss.errai.codegen.meta.impl.apt;

import org.apache.commons.lang3.ClassUtils;
import org.jboss.errai.codegen.meta.MetaAnnotation;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaEnum;

import javax.enterprise.util.Nonbinding;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.toMap;
import static org.jboss.errai.codegen.meta.impl.apt.APTClassUtil.elements;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class APTAnnotation extends MetaAnnotation {

  private static final Map<MetaClass, Class> ANNOTATION_VALUE_POSSIBLE_ARRAY_TYPES_BY_ITS_COMPONENTS_META_CLASS = Stream
          .of(String[].class, Enum[].class, Class[].class, Annotation[].class, byte[].class, short[].class, int[].class,
                  long[].class, float[].class, double[].class, char[].class, boolean[].class)
          .collect(Collectors.toMap(clazz -> MetaClassFactory.get(clazz.getComponentType()), c -> c));

  private final Map<String, Object> values;
  private final AnnotationMirror annotationMirror;

  public APTAnnotation(final AnnotationMirror annotationMirror) {
    this.annotationMirror = annotationMirror;
    this.values = elements.getElementValuesWithDefaults(annotationMirror)
            .entrySet()
            .stream()
            .collect(toMap(e -> e.getKey().getSimpleName().toString(), e -> e.getValue().getValue()));
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V> V valueAsArray(final String attributeName, final Class<V> arrayTypeHint) {
    return (V) convertValue(values.get(attributeName), arrayTypeHint);
  }

  @SuppressWarnings("unchecked")
  public <V> V value(final String attributeName, final MetaClass attributeMetaClass) {

    if (attributeMetaClass.isArray()) {
      return (V) valueAsArray(attributeName, arrayType(attributeMetaClass));
    }

    return (V) value(attributeName);
  }

  private Class<?> arrayType(final MetaClass attributeMetaClass) {
    return ANNOTATION_VALUE_POSSIBLE_ARRAY_TYPES_BY_ITS_COMPONENTS_META_CLASS.getOrDefault(
            attributeMetaClass.getComponentType().getErased(), Object[].class);
  }

  @Override
  public MetaClass annotationType() {
    return new APTClass(annotationMirror.getAnnotationType());
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private static Object convertValue(final Object value, final Class<?> arrayType) {
    if (value == null) {
      return null;
    } else if (value instanceof String) {
      return value;
    } else if (value instanceof TypeMirror) {
      return new APTClass((TypeMirror) value);
    } else if (value instanceof VariableElement) {
      return new APTEnum((VariableElement) value);
    } else if (value instanceof AnnotationMirror) {
      return new APTAnnotation((AnnotationMirror) value);
    } else if (value instanceof List) {
      return convertToArrayValue(value, arrayType);
    } else if (ClassUtils.isPrimitiveWrapper(value.getClass())) {
      return value;
    } else {
      throw new IllegalArgumentException(
              format("Unrecognized annotation module [%s] of type [%s].", value, value.getClass()));
    }
  }

  private static Object convertToArrayValue(final Object value, final Class<?> arrayTypeHint) {
    return ((List<?>) value).stream()
            .map(av -> ((AnnotationValue) av).getValue())
            .map(v -> convertValue(v, arrayTypeHint))
            .toArray(n -> buildArray(arrayTypeHint, n));
  }

  private static Object[] buildArray(final Class<?> arrayTypeHint, final int n) {
    if (arrayTypeHint.equals(Class[].class)) {
      return (Object[]) Array.newInstance(MetaClass.class, n);
    } else if (arrayTypeHint.equals(Annotation[].class)) {
      return (Object[]) Array.newInstance(MetaAnnotation.class, n);
    } else if (arrayTypeHint.equals(Enum[].class)) {
      return (Object[]) Array.newInstance(MetaEnum.class, n);
    } else {
      return (Object[]) Array.newInstance(arrayTypeHint.getComponentType(), n);
    }
  }

  @Override
  public Map<String, Object> values() {
    return values.entrySet()
            .stream()
            .filter(e -> !isNonbinding(e.getKey()))
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private boolean isNonbinding(final String methodName) {
    return annotationType().getDeclaredMethod(methodName, new MetaClass[0]).isAnnotationPresent(Nonbinding.class);
  }
}
