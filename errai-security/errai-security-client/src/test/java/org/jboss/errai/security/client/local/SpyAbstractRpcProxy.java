package org.jboss.errai.security.client.local;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.jboss.errai.bus.client.framework.AbstractRpcProxy;
import org.jboss.errai.common.client.PageRequest;
import org.jboss.errai.security.shared.*;

import java.util.ArrayList;
import java.util.List;

/**
* @author edewit@redhat.com
*/
@SuppressWarnings("unchecked")
public class SpyAbstractRpcProxy extends AbstractRpcProxy implements AuthenticationService {
  private Multiset<String> calls = HashMultiset.create();

  @Override
  public User login(String username, String password) {
    this.remoteCallback.callback(null);
    calls.add("login");
    return null;
  }

  @Override
  public boolean isLoggedIn() {
    calls.add("isLoggedIn");
    return false;
  }

  @Override
  public void logout() {
    this.remoteCallback.callback(null);
    calls.add("logout");
  }

  @Override
  public User getUser() {
    this.remoteCallback.callback(null);
    calls.add("getUser");
    return null;
  }

  @Override
  public List<Role> getRoles() {
    calls.add("getRoles");
    return null;
  }

  @Override
  public boolean hasPermission(PageRequest pageRequest) {
    this.remoteCallback.callback(null);
    calls.add("hasPermission");
    return false;
  }

  public Integer getCallCount(String method) {
    return calls.count(method);
  }
}
