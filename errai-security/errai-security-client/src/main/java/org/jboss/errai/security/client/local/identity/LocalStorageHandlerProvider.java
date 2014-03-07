package org.jboss.errai.security.client.local.identity;

import javax.inject.Provider;

import org.jboss.errai.ioc.client.api.IOCProvider;
import org.jboss.errai.marshalling.client.Marshalling;
import org.jboss.errai.security.shared.User;

import com.google.gwt.storage.client.Storage;

@IOCProvider
public class LocalStorageHandlerProvider implements Provider<LocalStorageHandler> {

  private class NoopStorageHandler implements LocalStorageHandler {
    @Override
    public User getUser() {
      return null;
    }

    @Override
    public void setUser(final User user) {
    }
  }

  private class LocalStorageHandlerImpl implements LocalStorageHandler {

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

  @Override
  public LocalStorageHandler get() {
    if (Storage.isLocalStorageSupported()) {
      return new LocalStorageHandlerImpl();
    }
    else {
      return new NoopStorageHandler();
    }
  }

}
