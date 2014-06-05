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
package org.jboss.errai.security.server;

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

    RestrictedAccess annotation = method.getAnnotation(RestrictedAccess.class);
    if (annotation != null) {
      annotations.add(annotation);
    }

    annotation = method.getDeclaringClass().getAnnotation(RestrictedAccess.class);
    if (annotation != null) {
      annotations.add(annotation);
    }

    Class<?>[] interfaces = aClass.getInterfaces();
    for (int i = 0, interfacesLength = interfaces.length; i < interfacesLength; i++) {
      annotations.addAll(getRestrictedAccess(interfaces[i], method));
      if (annotation != null) {
        annotations.add(annotation);
      }
    }

    if (annotations.isEmpty()) {
      throw new IllegalArgumentException(
              String.format("Could not find any @RestrictedAccess annotations on method (%s), class (%s), or interfaces.",
                      method.getName(), method.getDeclaringClass().getCanonicalName()));
    }

    return annotations;
  }

  private Collection<RestrictedAccess> getRestrictedAccess(Class<?> aClass, Method searchMethod) {
    final Collection<RestrictedAccess> annotations = new ArrayList<RestrictedAccess>();

    for (Method method : aClass.getMethods()) {
      final RestrictedAccess annotation = getRestrictAccess(searchMethod, method);
      if (annotation != null) {
        annotations.add(annotation);
      }
    }

    if (aClass.isAnnotationPresent(RestrictedAccess.class)) {
      annotations.add(aClass.getAnnotation(RestrictedAccess.class));
    }

    return annotations;
  }

  private RestrictedAccess getRestrictAccess(Method searchMethod, Method method) {
    RestrictedAccess requiredRoles = null;

    if (searchMethod.getName().equals(method.getName())
            && Arrays.equals(searchMethod.getParameterTypes(), method.getParameterTypes())) {
      requiredRoles = method.getAnnotation(RestrictedAccess.class);
    }

    return requiredRoles;
  }
}
