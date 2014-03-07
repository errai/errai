package org.jboss.errai.security.client.local.identity;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.security.shared.AuthenticationService;
import org.jboss.errai.security.shared.User;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Singleton
public class ActiveUserProviderImpl implements ActiveUserProvider {

  private static ActiveUserProvider instance;

  @Inject
  private Caller<AuthenticationService> authServiceCaller;

  @Inject
  private LocalStorageHandler storageHandler;

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
    if (!isCacheValid()) {
      final User storedUser = storageHandler.getUser();

      if (storedUser != null) {
        setActiveUser(storedUser, false);
      }
    }
  }

  @AfterInitialization
  private void updateCacheFromServer() {
    authServiceCaller.call(new RemoteCallback<User>() {
      @Override
      public void callback(final User response) {
        setActiveUser(response);
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
    valid = false;
    activeUser = null;
    storageHandler.setUser(null);
  }

}
