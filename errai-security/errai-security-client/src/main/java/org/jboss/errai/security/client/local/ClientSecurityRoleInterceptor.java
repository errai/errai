package org.jboss.errai.security.client.local;


import java.util.List;

import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.api.interceptor.RemoteCallContext;
import org.jboss.errai.common.client.api.interceptor.RemoteCallInterceptor;
import org.jboss.errai.security.shared.AuthenticationService;
import org.jboss.errai.security.shared.RequireRoles;
import org.jboss.errai.security.shared.Role;
import org.jboss.errai.ui.nav.client.local.api.SecurityError;

/**
 * Will 'redirect' users that try to make use of services annotated with {@link RequireRoles} that are not logged in
 * to the {@link org.jboss.errai.ui.nav.client.local.api.LoginPage} or do not have specified role
 * go to the {@link org.jboss.errai.ui.nav.client.local.api.SecurityError}. In other cases the service call will proceed
 *
 * @see org.jboss.errai.ui.nav.client.local.api.LoginPage
 * @author edewit@redhat.com
 */
public class ClientSecurityRoleInterceptor extends ClientSecurityInterceptor implements RemoteCallInterceptor<RemoteCallContext> {
  @Override
  public void aroundInvoke(final RemoteCallContext context) {
    securityCheck(getRequiredRoleAnnotation(context.getAnnotations()).value(), new Command() {
      @Override
      public void action() {
        proceed(context);
      }
    });
  }

  public void securityCheck(final String[] values, final Command command) {
    MessageBuilder.createCall(new RemoteCallback<Boolean>() {
      @Override
      public void callback(final Boolean loggedIn) {
        if (loggedIn) {
          MessageBuilder.createCall(new RemoteCallback<List<Role>>() {
            @Override
            public void callback(final List<Role> roles) {
              if (hasAllRoles(roles, values)) {
                if (command != null) command.action();
              } else {
                navigateToPage(SecurityError.class);
              }
            }
          }, AuthenticationService.class).getRoles();
        } else {
          navigateToLoginPage();
        }
      }
    }, AuthenticationService.class).isLoggedIn();
  }

}
