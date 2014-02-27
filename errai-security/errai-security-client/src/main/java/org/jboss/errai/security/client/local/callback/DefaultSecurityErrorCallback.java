package org.jboss.errai.security.client.local.callback;

import javax.inject.Singleton;

import org.jboss.errai.bus.client.api.UncaughtException;
import org.jboss.errai.security.client.local.nav.SecurityNavigationUtil;
import org.jboss.errai.security.shared.exception.SecurityException;
import org.jboss.errai.security.shared.exception.UnauthenticatedException;
import org.jboss.errai.security.shared.exception.UnauthorizedException;
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
@Singleton
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

      SecurityNavigationUtil.navigateToPage(pageRole);
    }
  }

}
