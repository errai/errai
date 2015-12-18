/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.common.metadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Mike Brock
 */
public class GWTMetaDataScanner extends MetaDataScanner {
  public GWTMetaDataScanner(final List<URL> urls) {
    super(urls, null);
  }

  @Override
  public Set<Class<?>> getTypesAnnotatedWith(final Class<? extends Annotation> annotation) {
    return super.getTypesAnnotatedWith(annotation);
  }

  @Override
  public Set<Field> getFieldsAnnotatedWith(final Class<? extends Annotation> annotation, final Collection<String> packages) {
    return super.getFieldsAnnotatedWith(annotation, packages);
  }

  @Override
  public Set<Method> getMethodsAnnotatedWith(final Class<? extends Annotation> annotation, final Collection<String> packages) {
    return super.getMethodsAnnotatedWith(annotation, packages);
  }

  @Override
  public Set<Class<?>> getTypesAnnotatedWith(final Class<? extends Annotation> annotation, final Collection<String> packages) {
    return super.getTypesAnnotatedWith(annotation, packages);
  }

  @Override
  public Set<Class<?>> getTypesAnnotatedWithExcluding(final Class<? extends Annotation> annotation, final String excludeRegex) {
    return super.getTypesAnnotatedWithExcluding(annotation, excludeRegex);
  }
}
