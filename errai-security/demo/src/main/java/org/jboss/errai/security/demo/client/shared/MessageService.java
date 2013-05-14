package org.jboss.errai.security.demo.client.shared;

import org.jboss.errai.bus.server.annotations.Remote;
import org.jboss.errai.security.shared.RequireAuthentication;

/**
 * @author edewit@redhat.com
 */
@Remote
public interface MessageService {
  @RequireAuthentication
  String hello();
  String ping();
}
