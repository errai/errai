package org.jboss.errai.security.client.local.identity;

import org.jboss.errai.security.shared.api.identity.User;

public interface LocalStorageHandler {
  User getUser();

  void setUser(final User user);
}