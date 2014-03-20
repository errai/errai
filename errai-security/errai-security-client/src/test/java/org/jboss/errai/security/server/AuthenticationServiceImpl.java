package org.jboss.errai.security.server;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.security.shared.api.identity.Role;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.service.AuthenticationService;

@Service
@ApplicationScoped
@Alternative
public class AuthenticationServiceImpl implements AuthenticationService {

  private String username;
  private User user;

  @Override
  public User login(String username, String password) {
    this.username = username;
    user = new User(username);
    user.setRoles(getRoles());

    return user;
  }

  @Override
  public boolean isLoggedIn() {
    return user != null;
  }

  @Override
  public void logout() {
    user = null;
    username = null;
  }

  @Override
  public User getUser() {
    return user;
  }

  public Set<Role> getRoles() {
    final Set<Role> roles = new HashSet<Role>();
    if (isLoggedIn()) {
      if (username.equals("admin")) {
        roles.add(new Role("admin"));
      }
      roles.add(new Role("user"));
    }

    return roles;
  }

}
