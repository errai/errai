package org.jboss.errai.security.server;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.common.client.PageRequest;
import org.jboss.errai.security.shared.AuthenticationService;
import org.jboss.errai.security.shared.Role;
import org.jboss.errai.security.shared.User;

@Service
@ApplicationScoped
@Alternative
public class AuthenticationServiceImpl implements AuthenticationService {

  private String username;
  private String password;
  private User user;

  @Override
  public User login(String username, String password) {
    this.username = username;
    this.password = password;
    user = new User(username);

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
    password = null;
  }

  @Override
  public User getUser() {
    return user;
  }

  @Override
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

  @Override
  public boolean hasPermission(PageRequest pageRequest) {
    for (final Role role : getRoles()) {
      if (role.getName().equals(pageRequest.getPageName())) {
        return true;
      }
    }

    return false;
  }

}
