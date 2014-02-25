package org.jboss.errai.security.client.local.identity;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.security.shared.AuthenticationService;
import org.jboss.errai.security.shared.User;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Singleton
public class ActiveUserProviderImpl implements ActiveUserProvider {
  
  private static ActiveUserProvider instance;

  @Inject
  private Caller<AuthenticationService> authServiceCaller;
  
  private boolean valid = false;
  
  public static ActiveUserProvider getInstance() {
    return instance;
  }
  
  public ActiveUserProviderImpl() {
    instance = this;
  }
  
  private User activeUser;
  
  @AfterInitialization
  private void init() {
    authServiceCaller.call(new RemoteCallback<User>() {
      @Override
      public void callback(final User response) {
        if (response != null)
          setActiveUser(response);
      }
    }).getUser();
  }

  @Override
  public User getActiveUser() {
    return activeUser;
  }

  @Override
  public void setActiveUser(User user) {
    valid = true;
    activeUser = user;
  }

  @Override
  public boolean hasActiveUser() {
    return getActiveUser() != null;
  }

  @Override
  public boolean isCacheValid() {
    return valid;
  }

  @Override
  public void invalidateCache() {
    valid = false;
  }

}
