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

package org.jboss.errai.security.keycloak.extension;

import java.lang.annotation.Annotation;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.apache.deltaspike.core.util.metadata.builder.AnnotatedTypeBuilder;
import org.jboss.errai.security.keycloak.KeycloakAuthenticationService;
import org.jboss.errai.security.shared.service.AuthenticationService;

/**
 * This CDI Extension allows the {@link KeycloakAuthenticationService} in this project to wrap an
 * {@link AuthenticationService} already on the classpath so that the
 * {@link KeycloakAuthenticationService} can be used to extend existing security infrastructure.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class AuthenticationServiceWrapperExtension implements Extension {

  private final Wrapped wrapped = new Wrapped() {
                @Override
                public Class<? extends Annotation> annotationType() {
                  return Wrapped.class;
                }
              };

  public <T> void processAuthenticationServiceTypes(@Observes final ProcessAnnotatedType<T> processedAnnotatedType) {
    final Class<T> type = processedAnnotatedType.getAnnotatedType().getJavaClass();

    if (isNonKeycloakAuthenticationService(type)) {
      processedAnnotatedType.setAnnotatedType(new AnnotatedTypeBuilder<T>()
              .readFromType(processedAnnotatedType.getAnnotatedType()).removeFromClass(Default.class)
              .addToClass(wrapped).create());
    }
  }

  private <T> boolean isNonKeycloakAuthenticationService(final Class<T> type) {
    return AuthenticationService.class.isAssignableFrom(type)
            && !KeycloakAuthenticationService.class.isAssignableFrom(type);
  }
}
