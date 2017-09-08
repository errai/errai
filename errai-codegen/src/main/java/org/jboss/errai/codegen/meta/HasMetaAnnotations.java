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
import java.util.Collection;
import java.util.Optional;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public interface HasMetaAnnotations {

  Optional<MetaAnnotation> getAnnotation(final Class<? extends Annotation> annotationClass);

  boolean isAnnotationPresent(final MetaClass metaClass);

  default boolean isAnnotationPresent(final Class<? extends Annotation> annotationClass) {
    return getAnnotation(annotationClass).isPresent();
  }

  Collection<MetaAnnotation> getAnnotations();
}
