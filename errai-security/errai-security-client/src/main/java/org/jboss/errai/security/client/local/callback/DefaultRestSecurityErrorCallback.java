package org.jboss.errai.security.client.local.callback;

import javax.inject.Inject;

import org.jboss.errai.enterprise.client.jaxrs.api.RestErrorCallback;
import org.jboss.errai.security.client.local.context.SecurityContext;
import org.jboss.errai.security.shared.exception.UnauthenticatedException;
import org.jboss.errai.security.shared.exception.UnauthorizedException;
import org.jboss.errai.ui.nav.client.local.api.LoginPage;
import org.jboss.errai.ui.nav.client.local.api.SecurityError;

import com.google.gwt.http.client.Request;

public class DefaultRestSecurityErrorCallback implements RestErrorCallback {

  private RestErrorCallback wrapped;

  private final SecurityContext context;

  public DefaultRestSecurityErrorCallback(final RestErrorCallback wrapped, final SecurityContext context) {
    this.context = context;
    this.wrapped = wrapped;
  }

  @Inject
  public DefaultRestSecurityErrorCallback(final SecurityContext context) {
    this(new RestErrorCallback() {
      @Override
      public boolean error(Request message, Throwable throwable) {
        return true;
      }
    }, context);
  }

  @Override
  public boolean error(final Request message, final Throwable throwable) {
    if (wrapped.error(message, throwable)) {
      if (throwable instanceof UnauthenticatedException) {
        context.navigateToPage(LoginPage.class);

      }
      else if (throwable instanceof UnauthorizedException) {
        context.navigateToPage(SecurityError.class);

      }
    }

    return false;
  }
  
  public void setWrappedErrorCallback(final RestErrorCallback errorCallback) {
    wrapped = errorCallback;
  }

}
