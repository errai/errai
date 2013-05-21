package org.jboss.errai.security.client.local;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.jboss.errai.bus.client.framework.AbstractRpcProxy;
import org.jboss.errai.security.shared.*;

import java.util.List;

/**
* @author edewit@redhat.com
*/
class SpyAbstractRpcProxy extends AbstractRpcProxy implements org.jboss.errai.security.shared.SecurityManager {
  private Multiset<String> calls = HashMultiset.create();

  @Override
  public void login(String username, String password) {
    calls.add("login");
  }

  @Override
  public boolean isLoggedIn() {
    calls.add("isLoggedIn");
    return false;
  }

  @Override
  public void logout() {
    calls.add("logout");
  }

  @Override
  public User getUser() {
    calls.add("getUser");
    return null;
  }

  @Override
  public List<Role> getRoles() {
    calls.add("getRoles");
    return null;
  }

  public Integer getCallCount(String method) {
    return calls.count(method);
  }
}
