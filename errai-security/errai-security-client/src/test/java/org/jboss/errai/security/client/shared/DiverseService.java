package org.jboss.errai.security.client.shared;

import org.jboss.errai.bus.server.annotations.Remote;
import org.jboss.errai.security.shared.api.annotation.RestrictAccess;

@Remote
public interface DiverseService {
  
  @RestrictAccess
  public void needsAuthentication();
  
  @RestrictAccess(roles = "admin")
  public void adminOnly();
  
  public void anybody();
  
}
