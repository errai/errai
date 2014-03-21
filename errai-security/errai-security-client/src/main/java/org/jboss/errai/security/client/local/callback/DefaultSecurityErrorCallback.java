package org.jboss.errai.security.client.local.callback;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.bus.client.api.UncaughtException;
import org.jboss.errai.security.client.local.context.SecurityContext;
import org.jboss.errai.security.shared.exception.SecurityException;
import org.jboss.errai.security.shared.exception.UnauthenticatedException;
import org.jboss.errai.security.shared.exception.UnauthorizedException;
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
  
  private final SecurityContext context;
  
  @Inject
  public DefaultSecurityErrorCallback(final SecurityContext context) {
    this.context = context;
  }

  @UncaughtException
  private void handleError(final Throwable throwable) {
    if (throwable instanceof UnauthenticatedException) {
      context.navigateToPage(LoginPage.class);
    }
    else if (throwable instanceof UnauthorizedException) {
      context.navigateToPage(SecurityError.class);
    }

  }

}
