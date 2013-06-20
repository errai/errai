package org.jboss.errai.security.server;

import org.jboss.errai.security.shared.AuthenticationService;
import org.jboss.errai.security.shared.RequireRoles;
import org.jboss.errai.security.shared.Role;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * SecurityRoleInterceptor server side implementation of the SecurityRoleInterceptor does the same,
 * but throws an exception instead of 'redirecting' the user.
 *
 * @author edewit@redhat.com
 */
@RequireRoles("")
@Interceptor
public class SecurityRoleInterceptor extends org.jboss.errai.security.client.local.SecurityRoleInterceptor {

  private final AuthenticationService authenticationService;

  @Inject
  public SecurityRoleInterceptor(AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

  @AroundInvoke
  public Object aroundInvoke(InvocationContext context) throws Exception {
    final List<Role> roles = authenticationService.getRoles();
    final RequireRoles annotation = getRequiredRoleAnnotation(context.getTarget().getClass(), context.getMethod());
    if (hasAllRoles(roles, annotation.value())) {
      return context.proceed();
    } else {
      throw new SecurityException("unauthorised access");
    }
  }

  private RequireRoles getRequiredRoleAnnotation(Class<?> service, Method method) {
    final Class<?>[] interfaces = service.getInterfaces();

    RequireRoles requiredRoles = null;
    if (interfaces.length > 0) {
      for (Class<?> anInterface : interfaces) {
        requiredRoles = getRequireRoles(method, anInterface);
      }
    } else {
      requiredRoles = getRequireRoles(method, service);
    }

    if (requiredRoles == null) {
      throw new IllegalArgumentException("could not find method that was intercepted!");
    }

    return requiredRoles;
  }

  private RequireRoles getRequireRoles(Method method, Class<?> aClass) {
    RequireRoles requiredRoles = getRequiredRoleAnnotation(method.getAnnotations());
    if (requiredRoles == null) {
      for (Method m : aClass.getMethods()) {
        if (m.getName().equals(method.getName())
                && Arrays.equals(m.getParameterTypes(), method.getParameterTypes())) {
          requiredRoles = getRequiredRoleAnnotation(m.getAnnotations());
          if (requiredRoles != null)
            break;
        }
      }
    }
    return requiredRoles;
  }
}
