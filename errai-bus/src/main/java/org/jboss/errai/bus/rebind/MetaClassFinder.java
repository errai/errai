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

package org.jboss.errai.bus.rebind;

import com.google.gwt.core.ext.GeneratorContext;
import org.jboss.errai.codegen.meta.MetaClass;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.function.BiFunction;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public interface MetaClassFinder
        extends BiFunction<GeneratorContext, Class<? extends Annotation>, Collection<MetaClass>> {

  default Collection<MetaClass> find(final GeneratorContext context, final Class<? extends Annotation> annotationClass) {
    return this.apply(context, annotationClass);
  }
}
