package org.jboss.errai.security.server;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.jboss.errai.security.shared.api.annotation.RequireAuthentication;
import org.jboss.errai.security.shared.exception.UnauthenticatedException;
import org.jboss.errai.security.shared.service.AuthenticationService;

/**
 * SecurityUserInterceptor server side implementation of the
 * {@link org.jboss.errai.security.client.local.SecurityUserInterceptor} does the same but throws an exception
 * instead of 'redirecting' the user.
 *
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
      throw new UnauthenticatedException();
    }
  }
}
