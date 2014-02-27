package org.jboss.errai.security.server;

import java.lang.reflect.Method;
import java.util.Arrays;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.jboss.errai.security.shared.AuthenticationService;
import org.jboss.errai.security.shared.RequireRoles;
import org.jboss.errai.security.shared.SecurityInterceptor;
import org.jboss.errai.security.shared.User;
import org.jboss.errai.security.shared.exception.UnauthenticatedException;
import org.jboss.errai.security.shared.exception.UnauthorizedException;

/**
 * SecurityRoleInterceptor server side implementation of the SecurityRoleInterceptor does the same,
 * but throws an exception instead of 'redirecting' the user.
 *
 * @author edewit@redhat.com
 */
@RequireRoles({})
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
    final RequireRoles annotation = getRequiredRoleAnnotation(context.getTarget().getClass(), context.getMethod());
    if (user == null) {
      throw new UnauthenticatedException();
    }
    else if (!hasAllRoles(user.getRoles(), annotation.value())) {
      throw new UnauthorizedException();
    } else {
      return context.proceed();
    }
  }

  private RequireRoles getRequiredRoleAnnotation(Class<?> aClass, Method method) {
    RequireRoles requireRoles = getRequiredRoleAnnotation(method.getAnnotations());
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

  private RequireRoles getRequireRoles(Class<?> aClass, Method searchMethod) {
    for (Method method : aClass.getMethods()) {
      final RequireRoles requireRoles = getRequireRoles(searchMethod, method);
      if (requireRoles != null) {
        return requireRoles;
      }
    }

    return null;
  }

  private RequireRoles getRequireRoles(Method searchMethod, Method method) {
    RequireRoles requiredRoles = null;

    if (searchMethod.getName().equals(method.getName())
            && Arrays.equals(searchMethod.getParameterTypes(), method.getParameterTypes())) {
      requiredRoles = getRequiredRoleAnnotation(method.getAnnotations());
    }

    return requiredRoles;
  }
}
