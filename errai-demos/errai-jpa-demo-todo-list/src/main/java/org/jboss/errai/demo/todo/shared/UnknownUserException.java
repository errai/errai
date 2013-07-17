package org.jboss.errai.demo.todo.shared;

/**
 * @author edewit@redhat.com
 */
public class UnknownUserException extends Exception {
  public UnknownUserException() {
  }

  public UnknownUserException(String message) {
    super(message);
  }
}
