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
package org.jboss.errai.security.client.local.context;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.security.client.local.api.SecurityContext;
import org.jboss.errai.security.client.local.spi.ActiveUserCache;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.event.LoggedInEvent;
import org.jboss.errai.security.shared.event.LoggedOutEvent;
import org.jboss.errai.security.shared.service.NonCachingUserService;
import org.jboss.errai.ui.nav.client.local.DefaultPage;
import org.jboss.errai.ui.nav.client.local.Navigation;
import org.jboss.errai.ui.nav.client.local.UniquePageRole;
import org.jboss.errai.ui.shared.api.style.StyleBindingsRegistry;
import org.slf4j.Logger;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
@ApplicationScoped
public class SecurityContextImpl implements SecurityContext {

  @Inject
  private Event<LoggedInEvent> loginEvent;

  @Inject
  private Event<LoggedOutEvent> logoutEvent;

  @Inject
  private Navigation navigation;

  @Inject
  private ActiveUserCache userCache;

  @Inject
  private Logger logger;

  @Inject
  private Caller<NonCachingUserService> userServiceCaller;

  private String lastPageCache;

  @PostConstruct
  private void setup() {
    performLoginStatusChangeActions(userCache.getUser());
  }

  @AfterInitialization
  private void updateCacheFromServer() {
    logger.debug("AfterInitialization invoked.");
    userServiceCaller.call(new RemoteCallback<User>() {
      @Override
      public void callback(final User response) {
        logger.debug("Response received from AfterInitialization RPC: " + String.valueOf(response));
        setCachedUser(response);
      }
    }).getUser();
  }

  @Override
  public void navigateToPage(final Class<? extends UniquePageRole> roleClass, final String lastPage) {
    final String pageName;
    if (lastPage != null) {
      pageName = lastPage;
    }
    // Edge case: first page load of app.
    else if (navigation.getCurrentPage() != null) {
      pageName = navigation.getCurrentPage().name();
    }
    else {
      pageName = null;
    }

    if (pageName != null) {
      lastPageCache = pageName;
    }
    navigation.goToWithRole(roleClass);
  }

  @Override
  public void navigateToPage(final Class<? extends UniquePageRole> roleClass) {
    navigateToPage(roleClass, null);
  }

  @Override
  public String getLastCachedPageName() {
    return lastPageCache;
  }

  @Override
  public void invalidateCache() {
    if (userCache.isValid()) {
      // User must be updated before style bindings updated.
      userCache.invalidateCache();
      performLoginStatusChangeActions(userCache.getUser());
    }
  }

  private void performLoginStatusChangeActions(final User user) {
    StyleBindingsRegistry.get().updateStyles();
    if (user == null) {
      logoutEvent.fire(new LoggedOutEvent());
    }
    else {
      loginEvent.fire(new LoggedInEvent(user));
    }
  }

  @Override
  public void navigateBackOrHome() {
    if (lastPageCache != null) {
      navigation.goTo(lastPageCache);
    }
    else {
      navigation.goToWithRole(DefaultPage.class);
    }
  }

  @Override
  public boolean hasCachedUser() {
    return userCache.hasUser();
  }

  @Override
  public User getCachedUser() {
    return userCache.getUser();
  }

  @Override
  public void setCachedUser(final User user) {
    // User must be updated before style bindings updated.
    userCache.setUser(user);
    performLoginStatusChangeActions(user);
  }

  @Override
  public boolean isUserCacheValid() {
    return userCache.isValid();
  }

}
