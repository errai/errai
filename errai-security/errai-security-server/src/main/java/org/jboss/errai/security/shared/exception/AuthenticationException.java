package org.jboss.errai.security.shared.exception;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Thrown when authentication fails.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Portable
public class AuthenticationException extends SecurityException {

  private static final long serialVersionUID = 1L;

}
