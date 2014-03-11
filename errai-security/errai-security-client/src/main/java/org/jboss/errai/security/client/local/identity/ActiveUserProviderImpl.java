package org.jboss.errai.security.client.local.identity;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.security.client.local.util.SecurityUtil;
import org.jboss.errai.security.shared.NonCachingUserService;
import org.jboss.errai.security.shared.User;
import org.slf4j.Logger;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Singleton
public class ActiveUserProviderImpl implements ActiveUserProvider {

  private static ActiveUserProvider instance;

  @Inject
  private LocalStorageHandler storageHandler;
  
  @Inject
  private Caller<NonCachingUserService> userServiceCaller;
  
  @Inject
  private Logger logger;

  private boolean valid = false;

  public static ActiveUserProvider getInstance() {
    return instance;
  }

  public ActiveUserProviderImpl() {
    instance = this;
  }

  private User activeUser;

  @PostConstruct
  private void maybeLoadStoredCache() {
    logger.debug("PostConstruct invoked.");
    if (!isCacheValid()) {
      logger.debug("Checking for user in local storage.");
      final User storedUser = storageHandler.getUser();

      if (storedUser != null) {
        setActiveUser(storedUser, false);
      }
    }
  }

  @AfterInitialization
  private void updateCacheFromServer() {
    logger.debug("AfterInitialization invoked.");
    userServiceCaller.call(new RemoteCallback<User>() {
      @Override
      public void callback(final User response) {
        logger.debug("Response received from AfterInitialization RPC: " + String.valueOf(response));
        SecurityUtil.performLoginStatusChangeActions(response);
      }
    }).getUser();
  }

  @Override
  public User getActiveUser() {
    return activeUser;
  }

  @Override
  public void setActiveUser(User user) {
    setActiveUser(user, true);
  }

  private void setActiveUser(User user, boolean localStorage) {
    logger.debug("Setting active user: " + String.valueOf(user));
    valid = true;
    activeUser = user;
    if (localStorage) {
      storageHandler.setUser(user);
    }
  }

  @Override
  public boolean hasActiveUser() {
    return getActiveUser() != null;
  }

  @Override
  public boolean isCacheValid() {
    return valid;
  }

  @Override
  public void invalidateCache() {
    logger.debug("Invalidating cache.");
    valid = false;
    activeUser = null;
    storageHandler.setUser(null);
  }

}
