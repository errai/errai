package org.jboss.errai.databinding.client;

/**
 * @author Divya Dadlani <ddadlani@redhat.com>
 */
public class InvalidBindEventException extends RuntimeException {

  public InvalidBindEventException() {
  }

  public InvalidBindEventException(String message) {
    super(message);
  }
}
