package org.jboss.errai.security.server;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.security.shared.AuthenticationService;
import org.jboss.errai.security.shared.NonCachingUserService;
import org.jboss.errai.security.shared.User;

@Singleton
@Service
public class NonCachingUserServiceImpl implements NonCachingUserService {
  
  @Inject
  private AuthenticationService authService;
  
  @Override
  public User getUser() {
    return authService.getUser();
  }
}
