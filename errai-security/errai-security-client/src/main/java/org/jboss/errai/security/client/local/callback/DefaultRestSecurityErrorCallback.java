/*
 * Copyright (C) 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.security.client.local.callback;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.enterprise.client.jaxrs.api.RestErrorCallback;
import org.jboss.errai.security.client.local.api.SecurityContext;
import org.jboss.errai.security.shared.exception.UnauthenticatedException;
import org.jboss.errai.security.shared.exception.UnauthorizedException;
import org.jboss.errai.ui.nav.client.local.api.LoginPage;
import org.jboss.errai.ui.nav.client.local.api.MissingPageRoleException;
import org.jboss.errai.ui.nav.client.local.api.SecurityError;

import com.google.gwt.http.client.Request;

/**
 * A {@link RestErrorCallback} that catches {@link UnauthenticatedException
 * UnauthenticatedExceptions} and {@link UnauthorizedException
 * UnauthorizedExceptions} and navigates to the page with the {@link LoginPage}
 * or {@link SecurityError} role, respectively.
 * 
 * Optionally, this class can wrap a given {@link RestErrorCallback} that it
 * will call first, in which case this class will only perform the actions
 * described above if the wrapped callback returns true.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Dependent
public class DefaultRestSecurityErrorCallback implements RestErrorCallback {

  private static RestErrorCallback defaultWrapped = new RestErrorCallback() {
    @Override
    public boolean error(Request message, Throwable throwable) {
      return true;
    }
  };

  private RestErrorCallback wrapped;

  private final SecurityContext context;

  /**
   * Create a {@link DefaultRestSecurityErrorCallback} wrapping a given
   * {@link RestErrorCallback}.
   * 
   * @param wrapped
   *          The wrapped callback (should never be {@code null}, that will be
   *          invoked first when
   *          {@link RestErrorCallback#error(Request, Throwable)} is called. If
   *          the error method on the {@code wrapped} returns {@code false}, the
   *          whole callback returns {@code false} immediately.
   * @param context
   *          The {@link SecurityContext}.
   */
  public DefaultRestSecurityErrorCallback(final RestErrorCallback wrapped, final SecurityContext context) {
    this.context = context;
    this.wrapped = wrapped;
  }

  /**
   * Create a {@link DefaultRestSecurityErrorCallback}.
   * 
   * @param context
   *          The {@link SecurityContext}.
   */
  @Inject
  public DefaultRestSecurityErrorCallback(final SecurityContext context) {
    this(defaultWrapped, context);
  }

  @Override
  public boolean error(final Request message, final Throwable throwable) throws MissingPageRoleException {
    if (wrapped.error(message, throwable)) {
      try {
        if (throwable instanceof UnauthenticatedException) {
          context.redirectToLoginPage();
        }
        else if (throwable instanceof UnauthorizedException) {
          context.redirectToSecurityErrorPage();
        }
        else {
          return true;
        }
      }
      catch (MissingPageRoleException ex) {
        throw new RuntimeException(
                "Could not redirect the user to the appropriate page because no page with that role was found.", ex);
      }
    }

    return false;
  }

  /**
   * Set the wrapped callback that will be invoked first when
   * {@link RestErrorCallback#error(Request, Throwable)} is called. If the error
   * method on the wrapped callback returns {@code false}, the whole callback
   * returns {@code false} immediately.
   * 
   * @param wrapped
   *          The wrapped callback. Passing in {@code null} clears any previous
   *          wrapped callback.
   * @return A reference to this callback for chaining.
   */
  public RestErrorCallback setWrappedErrorCallback(final RestErrorCallback wrapped) {
    this.wrapped = (wrapped != null) ? wrapped : defaultWrapped;
    return this;
  }

}
