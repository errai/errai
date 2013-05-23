package org.jboss.errai.security.server;

import org.jboss.errai.security.shared.*;
import org.jboss.errai.security.shared.AuthenticationService;

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

  private final AuthenticationService authenticationService;

  @Inject
  public SecurityUserInterceptor(AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

  @AroundInvoke
  public Object aroundInvoke(InvocationContext context) throws Exception {
    if (authenticationService.isLoggedIn()) {
      return context.proceed();
    } else {
      throw new SecurityException("unauthorised access");
    }
  }
}
