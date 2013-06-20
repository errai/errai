package org.jboss.errai.security.shared;

import org.jboss.errai.bus.server.annotations.Remote;
import org.jboss.errai.ui.nav.client.local.PageRequest;

import java.util.List;

/**
 * AuthenticationService service for authenticating users and get there roles.
 *
 * @author edewit@redhat.com
 */
@Remote
public interface AuthenticationService {

  void login(String username, String password);

  boolean isLoggedIn();

  void logout();

  User getUser();

  /**
   * Get the roles of the user, null if there is no user logged in
   * @return the roles of the user and null if there is currently no user logged in.
   */
  List<Role> getRoles();

  boolean hasPermission(PageRequest pageRequest);
}
