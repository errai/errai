package org.jboss.errai.security.shared.exception;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Thrown when a user attempts access to a resource which requires
 * authentication while the user is unauthenticated.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Portable
public class UnauthenticatedException extends SecurityException {

  private static final long serialVersionUID = 1L;

}
