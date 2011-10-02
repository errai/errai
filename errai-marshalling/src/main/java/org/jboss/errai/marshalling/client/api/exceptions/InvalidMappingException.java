package org.jboss.errai.marshalling.client.api.exceptions;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class InvalidMappingException extends RuntimeException {
  public InvalidMappingException() {
  }

  public InvalidMappingException(String message) {
    super(message);
  }

  public InvalidMappingException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidMappingException(Throwable cause) {
    super(cause);
  }
}
