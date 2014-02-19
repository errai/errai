package org.jboss.errai.security.client.local.callback;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.bus.client.api.UncaughtException;
import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.security.shared.exception.SecurityException;
import org.jboss.errai.security.shared.exception.UnauthenticatedException;
import org.jboss.errai.security.shared.exception.UnauthorizedException;
import org.jboss.errai.ui.nav.client.local.Navigation;
import org.jboss.errai.ui.nav.client.local.UniquePageRole;
import org.jboss.errai.ui.nav.client.local.api.LoginPage;
import org.jboss.errai.ui.nav.client.local.api.SecurityError;

/**
 * Catches {@link SecurityException SecurityExceptions}. If an
 * {@link UnauthenticatedException} is caught, Errai Navigation is directed to
 * the {@link LoginPage}. If an {@link UnauthorizedException} is caught, Errai
 * Navigation is directed to the {@link SecurityError} page.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
@ApplicationScoped
public class DefaultSecurityErrorCallback {

  @UncaughtException
  private void handleError(final Throwable throwable) {
    if (throwable instanceof SecurityException) {
      final Class<? extends UniquePageRole> pageRole;
      if (throwable instanceof UnauthenticatedException) {
        pageRole = LoginPage.class;
      }
      else {
        pageRole = SecurityError.class;
      }

      IOC.getAsyncBeanManager().lookupBean(Navigation.class).getInstance(new CreationalCallback<Navigation>() {

        @Override
        public void callback(final Navigation nav) {
          nav.goToWithRole(pageRole);
        }
      });
    }
  }

}
