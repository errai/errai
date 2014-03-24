package org.jboss.errai.security.client.local.context.impl;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.security.client.local.context.ActiveUserCache;
import org.jboss.errai.security.client.local.context.Simple;
import org.jboss.errai.security.client.local.identity.LocalStorageHandler;
import org.jboss.errai.security.shared.api.identity.User;
import org.slf4j.Logger;

/**
 * A {@link Simple} implementation for {@link ActiveUserCache}, storing a
 * {@link User} in memory and in browser local storage (if configured).
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Simple
@ApplicationScoped
public class BasicUserCacheImpl implements ActiveUserCache {

  @Inject
  private Logger logger;

  @Inject
  private LocalStorageHandler storageHandler;

  private boolean valid = false;

  private User activeUser;

  @Override
  public User getUser() {
    return activeUser;
  }

  @Override
  public void setUser(User user) {
    setActiveUser(user, true);
  }

  @PostConstruct
  private void maybeLoadStoredCache() {
    logger.debug("PostConstruct invoked.");
    if (!isValid()) {
      logger.debug("Checking for user in local storage.");
      final User storedUser = storageHandler.getUser();

      if (storedUser != null) {
        setActiveUser(storedUser, false);
      }
    }
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
  public boolean hasUser() {
    return getUser() != null;
  }

  @Override
  public boolean isValid() {
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
