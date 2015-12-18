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

package org.jboss.errai.security.client.local.storage;

import java.util.MissingResourceException;

import javax.inject.Provider;
import javax.inject.Singleton;

import org.jboss.errai.ioc.client.api.IOCProvider;
import org.jboss.errai.marshalling.client.Marshalling;
import org.jboss.errai.marshalling.client.api.MarshallerFramework;
import org.jboss.errai.security.shared.api.SecurityConstants;
import org.jboss.errai.security.shared.api.UserCookieEncoder;
import org.jboss.errai.security.shared.api.identity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.Cookies;

@IOCProvider
@Singleton
public class CookieStorageHandlerProvider implements Provider<UserStorageHandler> {

  private static final Logger logger = LoggerFactory.getLogger(CookieStorageHandlerProvider.class);

  private static class ReadOnlyStorageHandler implements UserStorageHandler {

    ReadOnlyStorageHandler() {
      MarshallerFramework.initializeDefaultSessionProvider();
    }

    @Override
    public User getUser() {
      User user = null;
      try {
        // If the GWT app's host page is behind a login page, the server
        // can set the currently authenticated user by providing the
        // errai_security_context variable as part of the host page. This way
        // the Errai app can bootstrap and the already authenticated user
        // instance is immediately injectable (without contacting the server
        // first).
        Dictionary dictionary = Dictionary.getDictionary(SecurityConstants.ERRAI_SECURITY_CONTEXT_DICTIONARY);
        user = (User) Marshalling.fromJSON(dictionary.get(SecurityConstants.DICTIONARY_USER));
      } 
      catch (MissingResourceException mre) {
        // Writing the errai_security_context variable is optional.
      }
      return user;
    }

    @Override
    public void setUser(final User user) {
    }
  }

  private static class UserCookieStorageHandlerImpl implements UserStorageHandler {

    UserCookieStorageHandlerImpl() {
      MarshallerFramework.initializeDefaultSessionProvider();
    }
    
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
      } catch (RuntimeException e) {
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
        } catch (RuntimeException ex) {
          logger.warn(
                  "Failed to store user in cookie cache. Subsequent visits to this app will redirect to login screen even if the session is still valid.",
                  ex);
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
      return new ReadOnlyStorageHandler();
    }
  }

}
