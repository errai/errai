package org.jboss.errai.security.server;

import java.lang.reflect.Method;
import java.util.Arrays;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.jboss.errai.security.shared.api.annotation.RestrictAccess;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.exception.UnauthenticatedException;
import org.jboss.errai.security.shared.exception.UnauthorizedException;
import org.jboss.errai.security.shared.interceptor.SecurityInterceptor;
import org.jboss.errai.security.shared.service.AuthenticationService;

/**
 * SecurityRoleInterceptor server side implementation of the SecurityRoleInterceptor does the same,
 * but throws an exception instead of 'redirecting' the user.
 *
 * @author edewit@redhat.com
 */
@RestrictAccess
@Interceptor
public class ServerSecurityRoleInterceptor extends SecurityInterceptor {

  private final AuthenticationService authenticationService;

  @Inject
  public ServerSecurityRoleInterceptor(AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

  @AroundInvoke
  public Object aroundInvoke(InvocationContext context) throws Exception {
    final User user = authenticationService.getUser();
    final RestrictAccess annotation = getRequiredRoleAnnotation(context.getTarget().getClass(), context.getMethod());
    if (user == null) {
      throw new UnauthenticatedException();
    }
    else if (!hasAllRoles(user.getRoles(), annotation.roles())) {
      throw new UnauthorizedException();
    } else {
      return context.proceed();
    }
  }

  private RestrictAccess getRequiredRoleAnnotation(Class<?> aClass, Method method) {
    RestrictAccess requireRoles = getRequiredRoleAnnotation(method.getAnnotations());
    if (requireRoles != null) {
      return requireRoles;
    }

    Class<?>[] interfaces = aClass.getInterfaces();
    for (int i = 0, interfacesLength = interfaces.length; i < interfacesLength && requireRoles == null; i++) {
      requireRoles = getRequireRoles(interfaces[i], method);
    }

    if (requireRoles == null) {
      throw new IllegalArgumentException("could not find method that was intercepted!");
    }

    return requireRoles;
  }

  private RestrictAccess getRequireRoles(Class<?> aClass, Method searchMethod) {
    for (Method method : aClass.getMethods()) {
      final RestrictAccess requireRoles = getRequireRoles(searchMethod, method);
      if (requireRoles != null) {
        return requireRoles;
      }
    }

    return null;
  }

  private RestrictAccess getRequireRoles(Method searchMethod, Method method) {
    RestrictAccess requiredRoles = null;

    if (searchMethod.getName().equals(method.getName())
            && Arrays.equals(searchMethod.getParameterTypes(), method.getParameterTypes())) {
      requiredRoles = getRequiredRoleAnnotation(method.getAnnotations());
    }

    return requiredRoles;
  }
}
