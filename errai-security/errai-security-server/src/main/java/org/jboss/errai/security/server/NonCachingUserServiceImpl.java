package org.jboss.errai.security.server;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.jboss.errai.security.shared.service.NonCachingUserService;

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
