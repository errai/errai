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

package org.jboss.errai.common.apt;

import org.jboss.errai.codegen.meta.MetaAnnotation;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.common.configuration.ErraiModule;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public abstract class ErraiAptGenerator {

  private final ErraiAptExportedTypes exportedTypes;

  public ErraiAptGenerator(final ErraiAptExportedTypes exportedTypes) {
    this.exportedTypes = exportedTypes;
  }

  public abstract String generate();

  public abstract String getPackageName();

  public abstract String getClassSimpleName();

  protected final Collection<MetaClass> findAnnotatedMetaClasses(final Class<? extends Annotation> annotation) {
    return exportedTypes.findAnnotatedMetaClasses(annotation);
  }

  //FIXME: not the best places for these below methods live

  protected <V> Set<V> getErraiModuleConfiguredArrayProperty(final Function<MetaAnnotation, Stream<V>> getter) {
    return getErraiModuleMetaClassesStream().flatMap(getter).collect(toSet());
  }

  protected <V> Set<V> getErraiModuleConfiguredProperty(final Function<MetaAnnotation, V> getter) {
    return getErraiModuleMetaClassesStream().map(getter).collect(toSet());
  }

  private Stream<MetaAnnotation> getErraiModuleMetaClassesStream() {
    return findAnnotatedMetaClasses(ErraiModule.class).stream()
            .map(module -> module.getAnnotation(ErraiModule.class))
            .map(Optional::get);
  }

}
