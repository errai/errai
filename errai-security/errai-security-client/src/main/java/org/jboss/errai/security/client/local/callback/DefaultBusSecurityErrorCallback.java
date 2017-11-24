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

import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ioc.client.api.UncaughtExceptionHandler;
import org.jboss.errai.security.client.local.handler.SecurityExceptionHandler;
import org.jboss.errai.security.shared.exception.SecurityException;
import org.jboss.errai.security.shared.exception.UnauthenticatedException;
import org.jboss.errai.security.shared.exception.UnauthorizedException;
import org.jboss.errai.ui.nav.client.local.api.LoginPage;
import org.jboss.errai.ui.nav.client.local.api.SecurityError;

import javax.inject.Inject;

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

  private SecurityExceptionHandler handler;

  // For proxying
  public DefaultBusSecurityErrorCallback() {
  }

  @Inject
  public DefaultBusSecurityErrorCallback(final SecurityExceptionHandler handler) {
    this.handler = handler;
  }

  @UncaughtExceptionHandler
  public void handleError(final Throwable caught) {
    handler.handleException(caught);
  }
}
