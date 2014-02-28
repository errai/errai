package org.jboss.errai.demo.todo.shared;

/**
 * @author edewit@redhat.com
 */
public class UnknownUserException extends Exception {

  private static final long serialVersionUID = 1L;

  public UnknownUserException() {
  }

  public UnknownUserException(String message) {
    super(message);
  }
}
