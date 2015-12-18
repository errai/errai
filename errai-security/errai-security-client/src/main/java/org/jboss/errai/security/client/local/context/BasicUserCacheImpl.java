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
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.security.client.local.spi.ActiveUserCache;
import org.jboss.errai.security.client.local.storage.UserStorageHandler;
import org.jboss.errai.security.shared.api.identity.User;
import org.slf4j.Logger;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
@ApplicationScoped
public class BasicUserCacheImpl implements ActiveUserCache {

  @Inject
  private Logger logger;

  @Inject
  private UserStorageHandler userStorageHandler;

  private boolean valid = false;

  private User activeUser = User.ANONYMOUS;

  @Override
  public User getUser() {
    return activeUser;
  }

  @Override
  public void setUser(User user) {
    Assert.notNull("User should not be null. Use User.ANONYMOUS instead.", user);
    setActiveUser(user, true);
  }

  @PostConstruct
  private void maybeLoadStoredCache() {
    logger.debug("PostConstruct invoked.");
    if (!isValid()) {
      logger.debug("Checking for user in local storage.");
      final User storedUser = userStorageHandler.getUser();

      if (storedUser != null) {
        setActiveUser(storedUser, false);
      }
    }
  }
  
  @Produces @Dependent
  private User produceActiveUser() {
    maybeLoadStoredCache();
    return activeUser;
  }

  private void setActiveUser(User user, boolean localStorage) {
    logger.debug("Setting active user: " + String.valueOf(user));
    valid = true;
    activeUser = user;
    if (localStorage) {
      final User toPersist = (!user.equals(User.ANONYMOUS)) ? user : null;
      userStorageHandler.setUser(toPersist);
    }
  }

  @Override
  public boolean isValid() {
    return valid;
  }

  @Override
  public void invalidateCache() {
    logger.debug("Invalidating cache.");
    valid = false;
    activeUser = User.ANONYMOUS;
    userStorageHandler.setUser(null);
  }

  @Override
  public boolean hasUser() {
    return !User.ANONYMOUS.equals(activeUser);
  }

}
