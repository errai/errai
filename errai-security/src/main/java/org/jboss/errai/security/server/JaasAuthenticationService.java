package org.jboss.errai.security.server;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.security.shared.Role;
import org.jboss.errai.security.shared.AuthenticationService;
import org.jboss.errai.security.shared.User;
import org.jboss.errai.ui.nav.client.shared.PageRequest;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import java.util.List;

/**
 * @author edewit@redhat.com
 */
@Service
@Alternative
@ApplicationScoped
public class JaasAuthenticationService implements AuthenticationService {

  @Override
  public User login(String username, String password) {
    return null;
  }

  @Override
  public boolean isLoggedIn() {
    return false;
  }

  @Override
  public void logout() {
  }

  @Override
  public User getUser() {
    return null;
  }

  @Override
  public List<Role> getRoles() {
    return null;
  }

  @Override
  public boolean hasPermission(PageRequest pageRequest) {
    return false;
  }
}
