package org.jboss.errai.security.shared.exception;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Thrown when authentication fails from invalid credentials.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Portable
public class FailedAuthenticationException extends AuthenticationException {

  private static final long serialVersionUID = 1L;
  
  public FailedAuthenticationException() {
    super();
  }
  
  public FailedAuthenticationException(String message) {
    super(message);
  }

  public FailedAuthenticationException(String message, Throwable throwable) {
    super(message, throwable);
  }

}
