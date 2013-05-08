package org.jboss.errai.security.server;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.security.shared.SecurityManager;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

/**
 * @author edewit@redhat.com
 */
@Service
@Alternative
@ApplicationScoped
public class JaasSecurityManager implements SecurityManager {

  @Override
  public void login(String username, String password) {
  }

  @Override
  public boolean isLoggedIn() {
    return false;
  }

  @Override
  public void logout() {
  }
}
