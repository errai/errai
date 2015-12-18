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
 * This {@link Extension} allows type level {@link RestrictedAccess} annotations to
 * trigger server-side interceptors on their method calls.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 * @author edewit@redhat.com
 */
public class SecurityAnnotationExtension implements Extension {

  public void addParameterLogger(@Observes ProcessAnnotatedType<?> processAnnotatedType) {
    final Class<?>[] interfaces = processAnnotatedType.getAnnotatedType().getJavaClass().getInterfaces();

    for (Class<?> anInterface : interfaces) {
      for (Method method : anInterface.getMethods()) {
        copyAnnotation(processAnnotatedType, anInterface, method, RestrictedAccess.class);
      }
    }
  }

  private <X> void copyAnnotation(ProcessAnnotatedType<X> annotatedType, Class<?> anInterface, Method method,
          Class<? extends Annotation> annotation) {
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

  private <X> AnnotatedMethod<? super X> getMethod(ProcessAnnotatedType<X> annotatedType, String name) {
    for (AnnotatedMethod<? super X> annotatedMethod : annotatedType.getAnnotatedType().getMethods()) {
      if (name.equals(annotatedMethod.getJavaMember().getName())) {
        return annotatedMethod;
      }
    }
    throw new IllegalArgumentException("cannot find method on implementation class that is on the interface");
  }
}
