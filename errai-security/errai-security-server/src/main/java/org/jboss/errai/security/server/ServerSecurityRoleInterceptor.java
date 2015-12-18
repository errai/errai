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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.annotation.RestrictedAccess;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.exception.UnauthenticatedException;
import org.jboss.errai.security.shared.exception.UnauthorizedException;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.jboss.errai.security.shared.spi.RequiredRolesExtractor;
import org.jboss.errai.security.shared.util.AnnotationUtils;

/**
 * SecurityRoleInterceptor server side implementation of the
 * SecurityRoleInterceptor does the same, but throws an exception instead of
 * 'redirecting' the user.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 * @author edewit@redhat.com
 */
@RestrictedAccess
@Interceptor
public class ServerSecurityRoleInterceptor {

  private final AuthenticationService authenticationService;
  private final RequiredRolesExtractor roleExtractor;

  public ServerSecurityRoleInterceptor() {
    authenticationService = null;
    roleExtractor = null;
    throw new IllegalStateException(
            "This default no-arg constructor exists to ensure Java EE 6+ compliance and should never be called!");
  }

  @Inject
  public ServerSecurityRoleInterceptor(final AuthenticationService authenticationService,
          final RequiredRolesExtractor roleExtractor) {
    this.authenticationService = authenticationService;
    this.roleExtractor = roleExtractor;
  }

  @AroundInvoke
  public Object aroundInvoke(InvocationContext context) throws Exception {
    final User user = authenticationService.getUser();
    final Collection<RestrictedAccess> annotations = getRestrictedAccessAnnotations(context.getTarget().getClass(),
            context.getMethod());
    final Set<Role> roles = AnnotationUtils.mergeRoles(roleExtractor, annotations);

    if (User.ANONYMOUS.equals(user)) {
      throw new UnauthenticatedException();
    }
    else if (!user.getRoles().containsAll(roles)) {
      throw new UnauthorizedException();
    }
    else {
      return context.proceed();
    }
  }

  private Collection<RestrictedAccess> getRestrictedAccessAnnotations(Class<?> aClass, Method method) {
    final Collection<RestrictedAccess> annotations = new ArrayList<RestrictedAccess>();

    addRestrictedAccessIfPresent(method, annotations);
    addRestrictedAccessIfPresent(method.getDeclaringClass(), annotations);

    for (Class<?> iface : aClass.getInterfaces()) {
      annotations.addAll(getRestrictedAccessFromRelevantInterface(iface, method));
    }

    if (annotations.isEmpty()) {
      throw new IllegalArgumentException(String.format(
              "Could not find any @RestrictedAccess annotations on method (%s), class (%s), or interfaces.",
              method.getName(), method.getDeclaringClass().getCanonicalName()));
    }

    return annotations;
  }

  private Collection<RestrictedAccess> getRestrictedAccessFromRelevantInterface(Class<?> iface, Method searchMethod) {
    final Collection<RestrictedAccess> annotations = new ArrayList<RestrictedAccess>();

    for (Method method : iface.getMethods()) {
      if (isMatchingMethod(searchMethod, method)) {
        addRestrictedAccessIfPresent(method, annotations);
        addRestrictedAccessIfPresent(iface, annotations);
        break;
      }
    }

    return annotations;
  }

  private boolean isMatchingMethod(Method searchMethod, Method method) {
    return searchMethod.getName().equals(method.getName())
            && Arrays.equals(searchMethod.getParameterTypes(), method.getParameterTypes());
  }

  private void addRestrictedAccessIfPresent(final AnnotatedElement annotated,
          final Collection<RestrictedAccess> annotations) {
    final RestrictedAccess annotation = annotated.getAnnotation(RestrictedAccess.class);
    if (annotation != null) {
      annotations.add(annotation);
    }
  }
}
