package org.jboss.errai.security.client.local.identity;

import org.jboss.errai.security.shared.AuthenticationService;
import org.jboss.errai.security.shared.User;

/**
 * Provides a cached copy of the actively logged in user. This cached copy is
 * automatically updated on RPC calls to {@link AuthenticationService}.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public interface ActiveUserProvider {

  /**
   * @return True iff there is a cached {@link User} available from a recent login.
   */
  public boolean hasActiveUser();

  /**
   * @return The currently logged in {@link User}, or {@literal null}.
   */
  public User getActiveUser();

  /**
   * Manually set the currently logged in {@link User}.
   * 
   * @param user The {@link User} currently logged in.
   */
  public void setActiveUser(User user);
  
  /**
   * @return False if the cached {@link User} has been invalidated.
   */
  public boolean isCacheValid();
  
  /**
   * Invalidate the cached {@link User}.
   */
  public void invalidateCache();

}
