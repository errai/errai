package org.jboss.errai.demo.todo.shared;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class RegistrationException extends Exception {

  // TODO include a list of BeanValidation failure items

  private static final long serialVersionUID = 1L;

  /**
   * Default constructor for the marshaller.
   */
  public RegistrationException() {
  }

  public RegistrationException(String message) {
    super(message);
  }
}
