/*
 * Copyright (C) 2014 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.security.server;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.apache.deltaspike.core.util.metadata.builder.AnnotatedTypeBuilder;
import org.jboss.errai.security.shared.api.annotation.RestrictedAccess;

/**
 * This {@link Extension} allows {@link RestrictedAccess} annotations on a type, an implemented interface, and
 * implemented interface methods to trigger server-side interceptors.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 * @author edewit@redhat.com
 */
public class SecurityAnnotationExtension implements Extension {

  public void addParameterLogger(@Observes final ProcessAnnotatedType<?> processAnnotatedType) {
    final Class<?>[] interfaces = processAnnotatedType.getAnnotatedType().getJavaClass().getInterfaces();

    for (final Class<?> anInterface : interfaces) {
      for (final Method method : anInterface.getMethods()) {
        copyAnnotation(processAnnotatedType, anInterface, method, RestrictedAccess.class);
      }
    }
  }

  private <X> void copyAnnotation(final ProcessAnnotatedType<X> annotatedType, final Class<?> anInterface, final Method method,
          final Class<? extends Annotation> annotation) {
    final Annotation methodAnnotation = method.getAnnotation(annotation);
    final Annotation typeAnnotation = anInterface.getAnnotation(annotation);

    if (methodAnnotation != null || typeAnnotation != null) {
      AnnotatedTypeBuilder<X> builder = new AnnotatedTypeBuilder<X>().readFromType(annotatedType.getAnnotatedType());

      if (typeAnnotation != null) {
        builder = builder.addToClass(typeAnnotation);
      }
      if (methodAnnotation != null) {
        builder = builder.addToMethod(getMethod(annotatedType, method.getName()), methodAnnotation);
      }

      annotatedType.setAnnotatedType(builder.create());
    }
  }

  private <X> AnnotatedMethod<? super X> getMethod(final ProcessAnnotatedType<X> annotatedType, final String name) {
    for (final AnnotatedMethod<? super X> annotatedMethod : annotatedType.getAnnotatedType().getMethods()) {
      if (name.equals(annotatedMethod.getJavaMember().getName())) {
        return annotatedMethod;
      }
    }
    throw new IllegalArgumentException("cannot find method on implementation class that is on the interface");
  }
}
