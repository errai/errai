package org.jboss.errai.security.shared;

import org.jboss.errai.bus.server.annotations.Remote;

/**
 * @author edewit@redhat.com
 */
@Remote
public interface SecurityManager {

  void login(String username, String password);

  boolean isLoggedIn();

  void logout();

  User getUser();
}
