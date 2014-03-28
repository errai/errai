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
package org.jboss.errai.security.client.local.storage;

import javax.inject.Provider;
import javax.inject.Singleton;

import org.jboss.errai.ioc.client.api.IOCProvider;
import org.jboss.errai.security.shared.api.UserCookieEncoder;
import org.jboss.errai.security.shared.api.identity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.Cookies;

@IOCProvider
@Singleton
public class CookieStorageHandlerProvider implements Provider<UserStorageHandler> {

  private static final Logger logger = LoggerFactory.getLogger(CookieStorageHandlerProvider.class);

  private static class NoopStorageHandler implements UserStorageHandler {
    @Override
    public User getUser() {
      return null;
    }

    @Override
    public void setUser(final User user) {
    }
  }

  private static class UserCookieStorageHandlerImpl implements UserStorageHandler {

    @Override
    public User getUser() {
      try {
        final String json = Cookies.getCookie(UserCookieEncoder.USER_COOKIE_NAME);
        if (json != null) {
          User user = UserCookieEncoder.fromCookieValue(json);
          logger.debug("Found " + user + " in cookie cache!");
          return user;
        }
        else {
          return null;
        }
      }
      catch (RuntimeException e) {
        logger.warn("Failed to retrieve current user from a cookie.", e);
        Cookies.removeCookie(UserCookieEncoder.USER_COOKIE_NAME);
        return null;
      }
    }

    @Override
    public void setUser(final User user) {
      if (user != null) {
        try {
          logger.debug("Storing " + user + " in cookie cache.");
          final String json = UserCookieEncoder.toCookieValue(user);
          Cookies.setCookie(UserCookieEncoder.USER_COOKIE_NAME, json);
        }
        catch (RuntimeException ex) {
          logger.warn("Failed to store user in cookie cache. Subsequent visits to this app will redirect to login screen even if the session is still valid.", ex);
        }
      }
      else {
        Cookies.removeCookie(UserCookieEncoder.USER_COOKIE_NAME);
      }
    }

  }

  private final SecurityProperties properties = GWT.create(SecurityProperties.class);

  @Override
  public UserStorageHandler get() {
    if (Cookies.isCookieEnabled() && properties.isLocalStorageOfUserAllowed()) {
      return new UserCookieStorageHandlerImpl();
    }
    else {
      return new NoopStorageHandler();
    }
  }

}
