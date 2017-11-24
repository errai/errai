package org.jboss.errai.security.client.local.handler;

import org.jboss.errai.security.client.local.api.SecurityContext;
import org.jboss.errai.security.shared.exception.UnauthenticatedException;
import org.jboss.errai.security.shared.exception.UnauthorizedException;
import org.jboss.errai.ui.nav.client.local.api.MissingPageRoleException;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Default {@link SecurityExceptionHandler}.
 */
@Singleton
public class DefaultSecurityExceptionHandler implements SecurityExceptionHandler {

  protected SecurityContext context;

  // For proxying
  public DefaultSecurityExceptionHandler() {
  }

  @Inject
  public DefaultSecurityExceptionHandler(SecurityContext context) {
    this.context = context;
  }

  @Override
  public boolean handleException(Throwable throwable) {
    try {
      if (throwable instanceof UnauthenticatedException) {
        context.redirectToLoginPage();
      } else if (throwable instanceof UnauthorizedException) {
        context.redirectToSecurityErrorPage();
      }
    }
    catch (MissingPageRoleException ex) {
      throw new RuntimeException(
          "Could not redirect the user to the appropriate page because no page with that role was found.", ex);
    }
    return true;
  }
}
