/**
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.security.client.local.interceptors;

import java.lang.annotation.Annotation;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.api.interceptor.FeatureInterceptor;
import org.jboss.errai.common.client.api.interceptor.RemoteCallContext;
import org.jboss.errai.common.client.api.interceptor.RemoteCallInterceptor;
import org.jboss.errai.security.client.local.api.SecurityContext;
import org.jboss.errai.security.shared.api.annotation.RestrictedAccess;
import org.jboss.errai.security.shared.exception.UnauthenticatedException;
import org.jboss.errai.security.shared.exception.UnauthorizedException;
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

  @Inject
  public ClientSecurityRoleInterceptor(final SecurityContext securityContext) {
    this.securityContext = securityContext;
  }

  @Override
  public void aroundInvoke(final RemoteCallContext callContext) {
    securityCheck(
            AnnotationUtils.mergeRoles(
                    getRestrictedAccessAnnotation(callContext.getTypeAnnotations()),
                    getRestrictedAccessAnnotation(callContext.getAnnotations())),
                    callContext);
  }

  private void securityCheck(final String[] requiredRoleNames, final RemoteCallContext callContext) {
    if (securityContext.isUserCacheValid()) {
      if (securityContext.hasCachedUser()) {
        if (securityContext.getCachedUser().hasAllRoles(requiredRoleNames)) {
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
