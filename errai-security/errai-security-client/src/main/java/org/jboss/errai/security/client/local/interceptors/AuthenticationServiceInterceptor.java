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

package org.jboss.errai.security.client.local.interceptors;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.api.interceptor.InterceptsRemoteCall;
import org.jboss.errai.common.client.api.interceptor.RemoteCallContext;
import org.jboss.errai.common.client.api.interceptor.RemoteCallInterceptor;
import org.jboss.errai.security.client.local.api.SecurityContext;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.service.AuthenticationService;

/**
 * Intercepts RPC logins through {@link AuthenticationService} for populating
 * and removing the current logged in user via {@link SecurityContext}.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
@InterceptsRemoteCall({ AuthenticationService.class })
@Dependent
public class AuthenticationServiceInterceptor implements RemoteCallInterceptor<RemoteCallContext> {
  
  private final SecurityContext securityContext;
  
  @Inject
  public AuthenticationServiceInterceptor(final SecurityContext securityContext) {
    this.securityContext = securityContext;
  }

  @Override
  public void aroundInvoke(final RemoteCallContext callContext) {
    if (callContext.getMethodName().equals("login")) {
      login(callContext);
    }
    else if (callContext.getMethodName().equals("logout")) {
      logout(callContext);
    }
    else if (callContext.getMethodName().equals("getUser")) {
      getUser(callContext);
    }
    else if (callContext.getMethodName().equals("isLoggedIn")) {
      isLoggedIn(callContext);
    }
    else {
      callContext.proceed();
    }
  }

  private void isLoggedIn(final RemoteCallContext callContext) {
    if (securityContext.isUserCacheValid()) {
      callContext.setResult(securityContext.hasCachedUser());
    }
    else {
      callContext.proceed();
    }
  }

  private void login(final RemoteCallContext callContext) {
    callContext.proceed(new RemoteCallback<User>() {

      @Override
      public void callback(final User response) {
        securityContext.setCachedUser(response);
      }
    });
  }

  private void logout(final RemoteCallContext callContext) {
    securityContext.setCachedUser(User.ANONYMOUS);
    callContext.proceed();
  }

  private void getUser(final RemoteCallContext context) {
    if (securityContext.isUserCacheValid()) {
      context.setResult(securityContext.getCachedUser());
    }
    else {
      context.proceed(new RemoteCallback<User>() {

        @Override
        public void callback(final User response) {
          securityContext.setCachedUser(response);
        }
      });
    }
  }

}
