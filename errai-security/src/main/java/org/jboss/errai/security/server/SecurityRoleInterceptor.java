package org.jboss.errai.security.server;

import org.jboss.errai.security.shared.RequireRoles;
import org.jboss.errai.security.shared.Role;
import org.jboss.errai.security.shared.SecurityManager;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.util.List;


/**
 * @author edewit@redhat.com
 */
@RequireRoles("")
@Interceptor
public class SecurityRoleInterceptor extends org.jboss.errai.security.client.local.SecurityRoleInterceptor {

  private final SecurityManager securityManager;

  @Inject
  public SecurityRoleInterceptor(SecurityManager securityManager) {
    this.securityManager = securityManager;
  }

  @AroundInvoke
  public Object aroundInvoke(InvocationContext context) throws Exception {
    final List<Role> roles = securityManager.getRoles();
    final RequireRoles annotation = getRequiredRoleAnnotation(context.getMethod().getAnnotations());
    if (hasAllRoles(roles, annotation.value())) {
      return context.proceed();
    } else {
      throw new SecurityException("unauthorised access");
    }
  }
}
