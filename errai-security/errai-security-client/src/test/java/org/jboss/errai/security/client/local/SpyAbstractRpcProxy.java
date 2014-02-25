package org.jboss.errai.security.client.local;

import org.jboss.errai.bus.client.framework.AbstractRpcProxy;
import org.jboss.errai.security.shared.AuthenticationService;
import org.jboss.errai.security.shared.User;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

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

  public Integer getCallCount(String method) {
    return calls.count(method);
  }
}
