package org.jboss.errai.security.client.local;

import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.api.interceptor.RemoteCallContext;
import org.jboss.errai.security.shared.AuthenticationService;

/**
 * SecurityUserInterceptor will intercept calls annotated with {@link org.jboss.errai.security.shared.RequireAuthentication}
 * and 'redirect' users to the '{@link org.jboss.errai.security.shared.LoginPage}' if not logged-in
 * @author edewit@redhat.com
 */
public class SecurityUserInterceptor extends SecurityInterceptor {

  @Override
  public void aroundInvoke(final RemoteCallContext context) {
    final AuthenticationService authenticationService = MessageBuilder.createCall(new RemoteCallback<Boolean>() {
      @Override
      public void callback(final Boolean loggedIn) {
        if (loggedIn) {
          proceed(context);
        } else {
          navigateToLoginPage();
        }
      }
    }, AuthenticationService.class);

    authenticationService.isLoggedIn();
  }
}
