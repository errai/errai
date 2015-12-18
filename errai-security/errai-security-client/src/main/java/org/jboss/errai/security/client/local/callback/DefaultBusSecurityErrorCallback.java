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

import javax.inject.Inject;

import org.jboss.errai.bus.client.api.UncaughtException;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.security.client.local.api.SecurityContext;
import org.jboss.errai.security.shared.exception.SecurityException;
import org.jboss.errai.security.shared.exception.UnauthenticatedException;
import org.jboss.errai.security.shared.exception.UnauthorizedException;
import org.jboss.errai.ui.nav.client.local.api.LoginPage;
import org.jboss.errai.ui.nav.client.local.api.MissingPageRoleException;
import org.jboss.errai.ui.nav.client.local.api.SecurityError;

/**
 * Catches {@link SecurityException SecurityExceptions}. If an
 * {@link UnauthenticatedException} is caught, Errai Navigation is directed to
 * the {@link LoginPage}. If an {@link UnauthorizedException} is caught, Errai
 * Navigation is directed to the {@link SecurityError} page.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@EntryPoint
public class DefaultBusSecurityErrorCallback {

  private final SecurityContext context;

  // For proxying
  public DefaultBusSecurityErrorCallback() {
    context = null;
  }

  @Inject
  public DefaultBusSecurityErrorCallback(final SecurityContext context) {
    this.context = context;
  }

  @UncaughtException
  public void handleError(final Throwable throwable) {
    try {
      if (throwable instanceof UnauthenticatedException) {
        context.redirectToLoginPage();
      }
      else if (throwable instanceof UnauthorizedException) {
        context.redirectToSecurityErrorPage();
      }
    }
    catch (MissingPageRoleException ex) {
      throw new RuntimeException(
              "Could not redirect the user to the appropriate page because no page with that role was found.", ex);
    }

  }

}
