package org.jboss.errai.security.client.local.identity;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.security.shared.User;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
@ApplicationScoped
public class ActiveUserProviderImpl implements ActiveUserProvider {
  
  private User activeUser;

  @Override
  public User getActiveUser() {
    return activeUser;
  }

  @Override
  public void setActiveUser(User user) {
    activeUser = user;
  }

  @Override
  public boolean hasActiveUser() {
    return getActiveUser() != null;
  }

}
