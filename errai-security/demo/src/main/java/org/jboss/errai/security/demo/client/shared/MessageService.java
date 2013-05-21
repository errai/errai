package org.jboss.errai.security.demo.client.shared;

import org.jboss.errai.bus.server.annotations.Remote;
import org.jboss.errai.security.shared.RequireAuthentication;
import org.jboss.errai.security.shared.RequireRoles;

/**
 * @author edewit@redhat.com
 */
@Remote
public interface MessageService {
  @RequireAuthentication
  String hello();

  @RequireRoles("admin")
  String ping();
}
