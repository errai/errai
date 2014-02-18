package org.jboss.errai.security.client.shared;

import org.jboss.errai.bus.server.annotations.Remote;
import org.jboss.errai.security.shared.RequireAuthentication;
import org.jboss.errai.security.shared.RequireRoles;

@Remote
public interface DiverseService {
  
  @RequireAuthentication
  public void needsAuthentication();
  
  @RequireRoles("admin")
  public void adminOnly();
  
  public void anybody();
  
}
