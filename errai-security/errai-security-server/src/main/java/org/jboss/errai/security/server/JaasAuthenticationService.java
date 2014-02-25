package org.jboss.errai.security.server;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.security.shared.AuthenticationService;
import org.jboss.errai.security.shared.User;

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
}
