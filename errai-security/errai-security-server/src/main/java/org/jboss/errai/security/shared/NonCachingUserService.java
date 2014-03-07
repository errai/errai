package org.jboss.errai.security.shared;

import org.jboss.errai.bus.server.annotations.Remote;

@Remote
public interface NonCachingUserService {
  
  public User getUser();

}
