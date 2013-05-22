package org.jboss.errai.security.server;

import org.jboss.errai.security.shared.*;
import org.jboss.errai.security.shared.SecurityManager;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

/**
 * @author edewit@redhat.com
 */
@RequireAuthentication
@Interceptor
public class SecurityUserInterceptor {

  private final SecurityManager securityManager;

  @Inject
  public SecurityUserInterceptor(SecurityManager securityManager) {
    this.securityManager = securityManager;
  }

  @AroundInvoke
  public Object aroundInvoke(InvocationContext context) throws Exception {
    if (securityManager.isLoggedIn()) {
      return context.proceed();
    } else {
      throw new SecurityException("unauthorised access");
    }
  }
}
