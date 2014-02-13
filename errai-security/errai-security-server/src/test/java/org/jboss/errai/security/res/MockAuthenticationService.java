package org.jboss.errai.security.res;

import java.util.List;

import org.jboss.errai.bus.client.framework.AbstractRpcProxy;
import org.jboss.errai.common.client.PageRequest;
import org.jboss.errai.security.shared.AuthenticationService;
import org.jboss.errai.security.shared.Role;
import org.jboss.errai.security.shared.User;

@SuppressWarnings("unchecked")
public class MockAuthenticationService extends AbstractRpcProxy implements AuthenticationService {
  List<Role> roleList;

  public MockAuthenticationService() {
  }

  public MockAuthenticationService(List<Role> roleList) {
    this.roleList = roleList;
  }

  @Override
  public User login(String username, String password) {
    return null;
  }

  @Override
  public boolean isLoggedIn() {
    remoteCallback.callback(Boolean.FALSE);
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
    remoteCallback.callback(roleList);
    return roleList;
  }

  @Override
  public boolean hasPermission(PageRequest pageRequest) {
    return false;
  }
}