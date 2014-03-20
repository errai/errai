package org.jboss.errai.security.client.local.identity;

import javax.inject.Provider;
import javax.inject.Singleton;

import org.jboss.errai.ioc.client.api.IOCProvider;
import org.jboss.errai.marshalling.client.Marshalling;
import org.jboss.errai.security.client.local.context.SecurityProperties;
import org.jboss.errai.security.shared.api.identity.User;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.storage.client.Storage;

@IOCProvider
@Singleton
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

    private static final String storageKey = "errai-active-user";

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
  
  private final SecurityProperties properties = GWT.create(SecurityProperties.class);

  @Override
  public LocalStorageHandler get() {
    if (Storage.isLocalStorageSupported() && properties.isLocalStorageAllowed()) {
      return new LocalStorageHandlerImpl();
    }
    else {
      return new NoopStorageHandler();
    }
  }

}
