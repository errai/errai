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

package org.jboss.errai.apt.internal.generator.util;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.common.apt.MetaClassFinder;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IocRelevantClassesFinder;

import java.lang.annotation.Annotation;
import java.util.Collection;

import static java.util.stream.Collectors.toSet;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class AptIocRelevantClassesFinder implements IocRelevantClassesFinder {

  private final MetaClassFinder metaClassFinder;

  public AptIocRelevantClassesFinder(final MetaClassFinder metaClassFinder) {
    this.metaClassFinder = metaClassFinder;
  }

  @Override
  public Collection<MetaClass> find(final Collection<Class<? extends Annotation>> annotations) {
    return annotations.stream().flatMap(s -> metaClassFinder.findAnnotatedWith(s).stream()).collect(toSet());
  }
}
