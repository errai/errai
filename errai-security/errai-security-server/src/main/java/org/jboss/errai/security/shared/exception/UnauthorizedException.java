package org.jboss.errai.security.shared.exception;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Thrown when the user attempts to access a resource which requires greater
 * priveleges than the user possesses.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Portable
public class UnauthorizedException extends SecurityException {

  private static final long serialVersionUID = 1L;

}
