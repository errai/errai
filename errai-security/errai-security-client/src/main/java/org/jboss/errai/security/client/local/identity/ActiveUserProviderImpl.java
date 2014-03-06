package org.jboss.errai.security.client.local.identity;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.marshalling.client.Marshalling;
import org.jboss.errai.security.shared.AuthenticationService;
import org.jboss.errai.security.shared.User;

import com.google.gwt.storage.client.Storage;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Singleton
public class ActiveUserProviderImpl implements ActiveUserProvider {
  // TODO Figure out how to test caching behaviour.

  interface LocalStorageHandler {
    User getUser();

    void setUser(final User user);
  }

  class NoopStorageHandler implements LocalStorageHandler {
    @Override
    public User getUser() {
      return null;
    }

    @Override
    public void setUser(final User user) {
    }
  }

  class LocalStorageHandlerImpl implements LocalStorageHandler {

    private static final String storageKey = "ERRAI-ACTIVE-USER";

    @Override
    public User getUser() {
      final Storage storage = Storage.getLocalStorageIfSupported();
      try {
        final String json = storage.getItem(storageKey);
        if (json != null) {
          return Marshalling.fromJSON(json, User.class);
        }
        else {
          return null;
        }
      }
      catch (RuntimeException e) {
        storage.removeItem(storageKey);
        return null;
      }
    }

    @Override
    public void setUser(final User user) {
      final Storage storage = Storage.getLocalStorageIfSupported();

      if (user != null) {
        final String json = Marshalling.toJSON(user);
        storage.setItem(storageKey, json);
      }
      else {
        storage.removeItem(storageKey);
      }
    }

  }

  private static ActiveUserProvider instance;

  @Inject
  private Caller<AuthenticationService> authServiceCaller;

  private final LocalStorageHandler storageHandler;

  private boolean valid = false;

  public static ActiveUserProvider getInstance() {
    return instance;
  }

  public ActiveUserProviderImpl() {
    instance = this;

    if (Storage.isLocalStorageSupported())
      storageHandler = new LocalStorageHandlerImpl();
    else
      storageHandler = new NoopStorageHandler();
  }

  private User activeUser;

  @PostConstruct
  private void maybeLoadStoredCache() {
    final User storedUser = storageHandler.getUser();

    if (storedUser != null) {
      setActiveUser(storedUser, false);
    }
  }

  @AfterInitialization
  private void updateCacheFromServer() {
    if (!isCacheValid()) {
      authServiceCaller.call(new RemoteCallback<User>() {
        @Override
        public void callback(final User response) {
          if (response != null)
            setActiveUser(response);
        }
      }).getUser();
    }
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
  }

}
