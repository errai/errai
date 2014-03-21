package org.jboss.errai.security.client.local.interceptors;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.api.interceptor.InterceptsRemoteCall;
import org.jboss.errai.common.client.api.interceptor.RemoteCallContext;
import org.jboss.errai.common.client.api.interceptor.RemoteCallInterceptor;
import org.jboss.errai.security.client.local.context.ActiveUserCache;
import org.jboss.errai.security.client.local.context.SecurityContext;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.service.AuthenticationService;

/**
 * Intercepts RPC logins through {@link AuthenticationService} for populating
 * and removing the current logged in user via {@link ActiveUserCache}.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
@InterceptsRemoteCall({ AuthenticationService.class })
@Dependent
public class AuthenticationServiceInterceptor implements RemoteCallInterceptor<RemoteCallContext> {
  
  private final SecurityContext securityContext;
  
  @Inject
  public AuthenticationServiceInterceptor(final SecurityContext securityContext) {
    this.securityContext = securityContext;
  }

  @Override
  public void aroundInvoke(final RemoteCallContext callContext) {
    if (callContext.getMethodName().equals("login")) {
      login(callContext);
    }
    else if (callContext.getMethodName().equals("logout")) {
      logout(callContext);
    }
    else if (callContext.getMethodName().equals("getUser")) {
      getUser(callContext);
    }
    else if (callContext.getMethodName().equals("isLoggedIn")) {
      isLoggedIn(callContext);
    }
    else {
      callContext.proceed();
    }
  }

  private void isLoggedIn(final RemoteCallContext callContext) {
    if (securityContext.isValid()) {
      callContext.setResult(securityContext.hasUser());
    }
    else {
      callContext.proceed();
    }
  }

  private void login(final RemoteCallContext callContext) {
    callContext.proceed(new RemoteCallback<User>() {

      @Override
      public void callback(final User response) {
        securityContext.setUser(response);
      }
    });
  }

  private void logout(final RemoteCallContext callContext) {
    securityContext.setUser(null);
    callContext.proceed();
  }

  private void getUser(final RemoteCallContext context) {
    if (securityContext.isValid()) {
      context.setResult(securityContext.getUser());
    }
    else {
      context.proceed(new RemoteCallback<User>() {

        @Override
        public void callback(final User response) {
          securityContext.setUser(response);
        }
      });
    }
  }

}
