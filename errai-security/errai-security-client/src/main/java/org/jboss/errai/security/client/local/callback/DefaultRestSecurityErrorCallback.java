/**
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
