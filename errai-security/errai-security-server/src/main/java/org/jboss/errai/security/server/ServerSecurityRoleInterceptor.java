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
import java.util.Arrays;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.jboss.errai.security.shared.api.annotation.RestrictedAccess;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.exception.UnauthenticatedException;
import org.jboss.errai.security.shared.exception.UnauthorizedException;
import org.jboss.errai.security.shared.service.AuthenticationService;

/**
 * SecurityRoleInterceptor server side implementation of the
 * SecurityRoleInterceptor does the same, but throws an exception instead of
 * 'redirecting' the user.
 * 
 * @author edewit@redhat.com
 */
@RestrictedAccess
@Interceptor
public class ServerSecurityRoleInterceptor {

  private final AuthenticationService authenticationService;

  @Inject
  public ServerSecurityRoleInterceptor(AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

  @AroundInvoke
  public Object aroundInvoke(InvocationContext context) throws Exception {
    final User user = authenticationService.getUser();
    final RestrictedAccess annotation = getRestrictedAccessAnnotation(context.getTarget().getClass(),
            context.getMethod());
    if (User.ANONYMOUS.equals(user)) {
      throw new UnauthenticatedException();
    }
    else if (!user.hasAllRoles(annotation.roles())) {
      throw new UnauthorizedException();
    }
    else {
      return context.proceed();
    }
  }

  private RestrictedAccess getRestrictedAccessAnnotation(Class<?> aClass, Method method) {
    RestrictedAccess annotation = method.getAnnotation(RestrictedAccess.class);
    if (annotation != null) {
      return annotation;
    }

    annotation = method.getDeclaringClass().getAnnotation(RestrictedAccess.class);
    if (annotation != null) {
      return annotation;
    }

    Class<?>[] interfaces = aClass.getInterfaces();
    for (int i = 0, interfacesLength = interfaces.length; i < interfacesLength && annotation == null; i++) {
      annotation = getRestrictedAccess(interfaces[i], method);
    }

    if (annotation == null) {
      throw new IllegalArgumentException(
              String.format("Could not @RestrictedAccess annotation on method (%s), class (%s), or interfaces.",
                      method.getName(), method.getDeclaringClass().getCanonicalName()));
    }

    return annotation;
  }

  private RestrictedAccess getRestrictedAccess(Class<?> aClass, Method searchMethod) {
    for (Method method : aClass.getMethods()) {
      final RestrictedAccess annotation = getRestrictAccess(searchMethod, method);
      if (annotation != null) {
        return annotation;
      }
    }

    return null;
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
