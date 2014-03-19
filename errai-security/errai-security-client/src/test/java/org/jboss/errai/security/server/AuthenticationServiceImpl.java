package org.jboss.errai.security.server;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.security.shared.AuthenticationService;
import org.jboss.errai.security.shared.Role;
import org.jboss.errai.security.shared.User;

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

  public List<Role> getRoles() {
    final List<Role> roles = new ArrayList<Role>(2);
    if (isLoggedIn()) {
      if (username.equals("admin")) {
        roles.add(new Role("admin"));
      }
      roles.add(new Role("user"));
    }

    return roles;
  }

}
