package org.jboss.errai.security.server;

import org.jboss.errai.security.shared.AuthenticationService;
import org.jboss.errai.security.shared.RequireRoles;
import org.jboss.errai.security.shared.Role;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.util.List;


/**
 * SecurityRoleInterceptor server side implementation of the SecurityRoleInterceptor does the same, but throws an exception
 * instead of 'redirecting' the user.
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
    final RequireRoles annotation = getRequiredRoleAnnotation(context.getMethod().getAnnotations());
    if (hasAllRoles(roles, annotation.value())) {
      return context.proceed();
    } else {
      throw new SecurityException("unauthorised access");
    }
  }
}
