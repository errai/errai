package org.jboss.errai.security.client.local;

import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.api.interceptor.RemoteCallContext;
import org.jboss.errai.common.client.api.interceptor.RemoteCallInterceptor;
import org.jboss.errai.security.shared.AuthenticationService;

/**
 * SecurityUserInterceptor will intercept calls annotated with {@link org.jboss.errai.security.shared.RequireAuthentication}
 * and 'redirect' users to the '{@link org.jboss.errai.ui.nav.client.local.api.LoginPage}' if not logged-in
 * @author edewit@redhat.com
 */
public class SecurityUserInterceptor extends ClientSecurityInterceptor implements RemoteCallInterceptor<RemoteCallContext> {

  @Override
  public void aroundInvoke(final RemoteCallContext context) {
    securityCheck(new Command() {

      @Override
      public void action() {
        proceed(context);
      }
    });
  }

  private void securityCheck(final Command command) {
    MessageBuilder.createCall(new RemoteCallback<Boolean>() {
      @Override
      public void callback(final Boolean loggedIn) {
        if (loggedIn) {
          if (command != null) command.action();
        } else {
          navigateToLoginPage();
        }
      }
    }, AuthenticationService.class).isLoggedIn();
  }

  public void securityCheck() {
    securityCheck(null);
  }
}
