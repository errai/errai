package org.jboss.errai.security.client.local.context;

import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.service.AuthenticationService;

/**
 * Provides a cached copy of the actively logged in user. This cached copy is
 * automatically updated on RPC calls to {@link AuthenticationService}.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public interface ActiveUserCache {

  /**
   * @return True iff there is a cached {@link User} available from a recent login.
   */
  public boolean hasUser();

  /**
   * @return The currently logged in {@link User}, or {@literal null}.
   */
  public User getUser();

  /**
   * Manually set the currently logged in {@link User}.
   * 
   * @param user The {@link User} currently logged in.
   */
  public void setUser(User user);
  
  /**
   * @return False if the cached {@link User} has been invalidated.
   */
  public boolean isValid();
  
  /**
   * Invalidate the cached {@link User}.
   */
  public void invalidateCache();

}
