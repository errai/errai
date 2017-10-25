/*
 * Copyright (C) 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.ui.rebind.ioc.element;

import org.jboss.errai.codegen.meta.HasAnnotations;
import org.jboss.errai.codegen.meta.MetaAnnotation;
import org.jboss.errai.codegen.meta.impl.java.JavaReflectionAnnotation;
import org.jboss.errai.codegen.util.CDIAnnotationUtils;

import javax.inject.Named;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Optional;

import static java.util.Collections.singleton;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
class HasNamedAnnotation implements HasAnnotations {

  private final Named named;

  HasNamedAnnotation(final String tagName) {
    this.named = new Named() {

      @Override
      public Class<? extends Annotation> annotationType() {
        return Named.class;
      }

      @Override
      public String value() {
        return tagName;
      }

      @Override
      public int hashCode() {
        return CDIAnnotationUtils.hashCode(this);
      }

      @Override
      public String toString() {
        return CDIAnnotationUtils.toString(this);
      }

      @Override
      public boolean equals(final Object obj) {
        return obj instanceof Named && CDIAnnotationUtils.equals(this, (Annotation) obj);
      }
    };
  }

  @Override
  public Collection<MetaAnnotation> getAnnotations() {
    return singleton(new JavaReflectionAnnotation(named));
  }
}
