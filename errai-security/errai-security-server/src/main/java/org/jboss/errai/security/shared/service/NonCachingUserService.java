package org.jboss.errai.security.shared.service;

import org.jboss.errai.bus.server.annotations.Remote;
import org.jboss.errai.security.shared.api.identity.User;

@Remote
public interface NonCachingUserService {
  
  public User getUser();

}
