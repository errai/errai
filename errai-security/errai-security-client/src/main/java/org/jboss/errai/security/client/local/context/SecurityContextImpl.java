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

package org.jboss.errai.security.client.local.context;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.ClientMessageBus;
import org.jboss.errai.bus.client.framework.BusState;
import org.jboss.errai.bus.client.framework.ClientMessageBusImpl;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.api.extension.InitVotes;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.security.client.local.api.SecurityContext;
import org.jboss.errai.security.client.local.spi.ActiveUserCache;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.event.LoggedInEvent;
import org.jboss.errai.security.shared.event.LoggedOutEvent;
import org.jboss.errai.security.shared.service.NonCachingUserService;
import org.jboss.errai.ui.nav.client.local.DefaultPage;
import org.jboss.errai.ui.nav.client.local.Navigation;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.TransitionToRole;
import org.jboss.errai.ui.nav.client.local.api.LoginPage;
import org.jboss.errai.ui.nav.client.local.api.SecurityError;
import org.jboss.errai.ui.nav.client.local.spi.PageNode;
import org.jboss.errai.ui.nav.rebind.NavigationGraphGenerator;
import org.jboss.errai.ui.shared.api.style.StyleBindingsRegistry;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
@EntryPoint
public class SecurityContextImpl implements SecurityContext {

  /**
   * This page exists so that the existence of pages with {@link LoginPage} and
   * {@link SecurityError} roles can be enforced at compile-time. Currently the
   * {@link NavigationGraphGenerator} only scans {@link Page} annotated classes for transitions. For
   * performance reasons, this is preferable to scanning the whole classpath.
   */
  @Page
  public static class SecurityRolesConstraintPage extends SimplePanel {
    @SuppressWarnings("unused")
    @Inject private TransitionToRole<LoginPage> loginTransition;
    @SuppressWarnings("unused")
    @Inject private TransitionToRole<SecurityError> securityErrorTransition;
  }

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

  private Class<?> lastPageCache;

  @PostConstruct
  private void setup() {
    performLoginStatusChangeActions(userCache.getUser());
    InitVotes.waitFor(SecurityContext.class);
    InitVotes.registerOneTimeDependencyCallback(ClientMessageBus.class, new Runnable() {

      @Override
      public void run() {
        if (((ClientMessageBusImpl) ErraiBus.get()).getState() == BusState.CONNECTED) {
          initializeCacheFromServer();
        }
        else {
          // Don't cause initialization to fail if remote communication is disabled
          InitVotes.voteFor(SecurityContext.class);
        }
      }
    });
  }

  private void initializeCacheFromServer() {
    logger.debug("Attempting to initialize User cache from server.");
    userServiceCaller.call(new RemoteCallback<User>() {
      @Override
      public void callback(final User user) {
        logger.debug("Response received. Initializing user to " + String.valueOf(user));
        setCachedUser(user);
        InitVotes.voteFor(SecurityContext.class);
      }
    }, new ErrorCallback<Object>() {

      @Override
      public boolean error(final Object message, final Throwable throwable) {
        logger.warn("Error received while attempting to populate cache: " + throwable.getMessage());
        InitVotes.voteFor(SecurityContext.class);
        return false;
      }
    }).getUser();
  }

  @Override
  public void redirectToLoginPage() {
    redirectToLoginPage(getCurrentPage());
  }

  private Class<?> getCurrentPage() {
    if (navigation.getCurrentPage() != null) {
      return navigation.getCurrentPage().contentType();
    }
    else {
      // Guaranteed to exist at compile-time.
      return navigation.getPagesByRole(DefaultPage.class).iterator().next().contentType();
    }
  }

  @Override
  public void redirectToLoginPage(final Class<?> fromPage) {
    lastPageCache = fromPage;
    navigation.goToWithRole(LoginPage.class);
  }

  @Override
  public void redirectToSecurityErrorPage() {
    redirectToSecurityErrorPage(getCurrentPage());
  }

  @Override
  public void redirectToSecurityErrorPage(final Class<?> fromPage) {
    lastPageCache = fromPage;
    navigation.goToWithRole(SecurityError.class);
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
      throw new RuntimeException("The current user should never be null.");
    } else if (User.ANONYMOUS.equals(user)) {
      logoutEvent.fire(new LoggedOutEvent());
    }
    else {
      loginEvent.fire(new LoggedInEvent(user));
    }
  }

  @Override
  public void navigateBackOrToPage(final Class<?> pageType) {
    if (lastPageCache != null) {
      navigation.goTo(lastPageCache, ImmutableMultimap.<String, String>of());
      lastPageCache = null;
    }
    else {
      navigation.goTo(pageType, ImmutableListMultimap.<String, String>of());
    }
  }

  @Override
  public void navigateBackOrHome() {
    // Guaranteed to exist at compile-time.
    final PageNode<?> defaultPageNode = navigation.getPagesByRole(DefaultPage.class).iterator().next();
    navigateBackOrToPage(defaultPageNode.contentType());
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
