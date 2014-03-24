package org.jboss.errai.security.client.local.context.impl;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.AfterInitialization;
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
 * This {@link SecurityContext} acts as an {@link Complex}
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
public class SecurityContextImpl implements SecurityContext {

  @Inject
  private Event<LoggedInEvent> loginEvent;

  @Inject
  private Event<LoggedOutEvent> logoutEvent;

  @Inject
  private Navigation navigation;

  @Inject
  @Simple
  // Must inject by concrete class because of IOC bug
  private BasicUserCacheImpl userCache;

  @Inject
  private Logger logger;

  @Inject
  private Caller<NonCachingUserService> userServiceCaller;

  private String lastPageCache;

  private static SecurityContextImpl instance;

  public static SecurityContextImpl getInstance() {
    return instance;
  }

  @PostConstruct
  private void setup() {
    instance = this;
    performLoginStatusChangeActions(userCache.getUser());
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

}
