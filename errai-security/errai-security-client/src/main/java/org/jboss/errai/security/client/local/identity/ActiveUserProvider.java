package org.jboss.errai.security.client.local.identity;

import org.jboss.errai.security.shared.User;

public interface ActiveUserProvider {
  
  public boolean hasActiveUser();
  
  public User getActiveUser();
  
  public void setActiveUser(User user);

}
