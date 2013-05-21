package org.jboss.errai.security.server;

import org.jboss.errai.security.shared.RequireAuthentication;
import org.picketlink.Identity;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

/**
 * @author edewit@redhat.com
 */
@RequireAuthentication
@Interceptor
public class SecurityInterceptor {

  @Inject
  private Identity identity;

  @AroundInvoke
  public Object aroundInvoke(InvocationContext context) throws Exception {
    if (identity.isLoggedIn()) {
      return context.proceed();
    } else {
      throw new SecurityException("unauthorised access");
    }
  }
}
