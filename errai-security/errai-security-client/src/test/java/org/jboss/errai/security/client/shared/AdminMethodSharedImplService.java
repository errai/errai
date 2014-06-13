package org.jboss.errai.security.client.shared;

import org.jboss.errai.bus.server.annotations.Remote;
import org.jboss.errai.security.shared.api.annotation.RestrictedAccess;

@Remote
public interface AdminMethodSharedImplService {

  @RestrictedAccess(roles = "admin")
  void someAdminService();

}
