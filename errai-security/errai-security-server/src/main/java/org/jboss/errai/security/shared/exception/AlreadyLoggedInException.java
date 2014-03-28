package org.jboss.errai.security.shared.exception;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Thrown when authentication is attempted while a user is currently logged in.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Portable
public class AlreadyLoggedInException extends AuthenticationException {

  private static final long serialVersionUID = 1L;

  public AlreadyLoggedInException() {
    super();
  }

  public AlreadyLoggedInException(String message) {
    super(message);
  }

  public AlreadyLoggedInException(String message, Throwable throwable) {
    super(message, throwable);
  }

}
