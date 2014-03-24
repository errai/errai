package org.jboss.errai.security.client.local.context;

import org.jboss.errai.security.shared.api.identity.User;

/**
 * Stores compile-time configurations for Errai Security.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public interface SecurityProperties {

  /**
   * @return True iff {@literal ErraiApp.properties} was configured to allow
   *         {@link User Users} to be cached in browser local storage.
   */
  public Boolean isLocalStorageOfUserAllowed();

}
