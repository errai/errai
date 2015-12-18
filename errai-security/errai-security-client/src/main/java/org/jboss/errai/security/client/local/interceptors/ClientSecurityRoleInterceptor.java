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

package org.jboss.errai.security.client.local.interceptors;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.api.interceptor.FeatureInterceptor;
import org.jboss.errai.common.client.api.interceptor.RemoteCallContext;
import org.jboss.errai.common.client.api.interceptor.RemoteCallInterceptor;
import org.jboss.errai.security.client.local.api.SecurityContext;
import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.annotation.RestrictedAccess;
import org.jboss.errai.security.shared.exception.UnauthenticatedException;
import org.jboss.errai.security.shared.exception.UnauthorizedException;
import org.jboss.errai.security.shared.spi.RequiredRolesExtractor;
import org.jboss.errai.security.shared.util.AnnotationUtils;

/**
 * Intercepts RPC calls to resources marked with {@link RestrictedAccess}. This
 * interceptor throws an {@link UnauthenticatedException} if the user is not
 * logged in, and a {@link UnauthorizedException} if the user does not have the
 * required roles.
 *
 * @author edewit@redhat.com
 * @author Max Barkley <mbarkley@redhat.com>
 */
@FeatureInterceptor(RestrictedAccess.class)
@Dependent
public class ClientSecurityRoleInterceptor implements
RemoteCallInterceptor<RemoteCallContext> {

  private final SecurityContext securityContext;
  private final RequiredRolesExtractor roleExtractor;

  // For proxying
  public ClientSecurityRoleInterceptor() {
    securityContext = null;
    roleExtractor = null;
  }

  @Inject
  public ClientSecurityRoleInterceptor(final SecurityContext securityContext, final RequiredRolesExtractor roleExtractor) {
    this.securityContext = securityContext;
    this.roleExtractor = roleExtractor;
  }

  @Override
  public void aroundInvoke(final RemoteCallContext callContext) {
    securityCheck(
            AnnotationUtils.mergeRoles(
                    roleExtractor,
                    getRestrictedAccessAnnotation(callContext.getTypeAnnotations()),
                    getRestrictedAccessAnnotation(callContext.getAnnotations())),
                    callContext);
  }

  private void securityCheck(final Set<Role> requiredRoleNames, final RemoteCallContext callContext) {
    if (securityContext.isUserCacheValid()) {
      if (securityContext.hasCachedUser()) {
        if (securityContext.getCachedUser().getRoles().containsAll(requiredRoleNames)) {
          callContext.proceed(new RemoteCallback<Object>() {

            @Override
            public void callback(final Object response) {
              callContext.setResult(response);
            }
          }, new ErrorCallback<Object>() {

            @Override
            public boolean error(Object message, Throwable throwable) {
              if (throwable instanceof UnauthenticatedException) {
                securityContext.invalidateCache();
              }

              return true;
            }
          });
        }
        else {
          throw new UnauthorizedException();
        }
      }
      else {
        throw new UnauthenticatedException();
      }
    }
    else {
      callContext.proceed();
    }
  }

  private RestrictedAccess getRestrictedAccessAnnotation(Annotation[] annotations) {
    for (Annotation annotation : annotations) {
      if (annotation instanceof RestrictedAccess) {
        return (RestrictedAccess) annotation;
      }
    }
    return null;
  }

}
