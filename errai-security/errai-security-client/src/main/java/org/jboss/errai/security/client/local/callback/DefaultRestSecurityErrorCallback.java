package org.jboss.errai.security.client.local.callback;

import org.jboss.errai.enterprise.client.jaxrs.api.RestErrorCallback;
import org.jboss.errai.security.client.local.util.SecurityUtil;
import org.jboss.errai.security.shared.exception.UnauthenticatedException;
import org.jboss.errai.security.shared.exception.UnauthorizedException;
import org.jboss.errai.ui.nav.client.local.api.LoginPage;
import org.jboss.errai.ui.nav.client.local.api.SecurityError;

import com.google.gwt.http.client.Request;

public class DefaultRestSecurityErrorCallback implements RestErrorCallback {

  private final RestErrorCallback wrapped;

  public DefaultRestSecurityErrorCallback(final RestErrorCallback wrapped) {
    this.wrapped = wrapped;
  }

  public DefaultRestSecurityErrorCallback() {
    this(new RestErrorCallback() {
      @Override
      public boolean error(Request message, Throwable throwable) {
        return true;
      }
    });
  }

  @Override
  public boolean error(final Request message, final Throwable throwable) {
    if (wrapped.error(message, throwable)) {
      if (throwable instanceof UnauthenticatedException) {
        SecurityUtil.navigateToPage(LoginPage.class);

        return false;
      }
      else if (throwable instanceof UnauthorizedException) {
        SecurityUtil.navigateToPage(SecurityError.class);

        return false;
      }
    }

    return false;
  }

}
