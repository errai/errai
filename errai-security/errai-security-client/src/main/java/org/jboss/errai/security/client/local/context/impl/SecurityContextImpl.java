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
package org.jboss.errai.security.client.local.context.impl;

import java.lang.annotation.Annotation;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.security.client.local.context.ActiveUserCache;
import org.jboss.errai.security.client.local.context.Complex;
import org.jboss.errai.security.client.local.context.Simple;
import org.jboss.errai.security.client.local.context.SecurityContext;
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
 * This implements {@link SecurityContext} and implements a {@link Complex}
 * {@link ActiveUserCache} that not only caches the logged in user, but also
 * performs changes to security-related UI styles, sends events, and performs
 * any other Errai Security tasks required when the cached user information
 * changes.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Default
@Complex
@ApplicationScoped
public class SecurityContextImpl implements SecurityContext, ActiveUserCache {

  @Inject
  private Event<LoggedInEvent> loginEvent;

  @Inject
  private Event<LoggedOutEvent> logoutEvent;

  @Inject
  private Navigation navigation;

  private ActiveUserCache userCache;

  @Inject
  private Logger logger;

  @Inject
  private Caller<NonCachingUserService> userServiceCaller;

  private String lastPageCache;

  @PostConstruct
  private void setup() {
    /*
     * Manually inject ActiveUserCache to work around IOC circular dependency
     * bug.
     */
    IOC.getAsyncBeanManager().lookupBean(ActiveUserCache.class, new Annotation() {
      @Override
      public Class<? extends Annotation> annotationType() {
        return Simple.class;
      }
    }).getInstance(new CreationalCallback<ActiveUserCache>() {
      @Override
      public void callback(final ActiveUserCache userCache) {
        SecurityContextImpl.this.userCache = userCache;
        performLoginStatusChangeActions(userCache.getUser());
      }
    });
    ;
  }

  @AfterInitialization
  private void updateCacheFromServer() {
    logger.debug("AfterInitialization invoked.");
    userServiceCaller.call(new RemoteCallback<User>() {
      @Override
      public void callback(final User response) {
        logger.debug("Response received from AfterInitialization RPC: " + String.valueOf(response));
        setUser(response);
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
  public boolean hasUser() {
    return userCache.hasUser();
  }

  @Override
  public User getUser() {
    return userCache.getUser();
  }

  @Override
  public void setUser(User user) {
    // User must be updated before style bindings updated.
    userCache.setUser(user);
    performLoginStatusChangeActions(user);
  }

  @Override
  public boolean isValid() {
    return userCache.isValid();
  }

  @Override
  public ActiveUserCache getActiveUserCache() {
    return this;
  }

}
