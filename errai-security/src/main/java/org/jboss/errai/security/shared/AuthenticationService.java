package org.jboss.errai.security.shared;

import org.jboss.errai.bus.server.annotations.Remote;

import java.util.List;

/**
 * @author edewit@redhat.com
 */
@Remote
public interface AuthenticationService {

  void login(String username, String password);

  boolean isLoggedIn();

  void logout();

  User getUser();

  List<Role> getRoles();
}
