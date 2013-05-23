package org.jboss.errai.demo.todo.shared;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Thrown when the user attempts to perform an action that is not permitted.
 * <p>
 * TODO delete this and use the javax.security or org.picketlink type.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
@Portable
public class AccessDeniedException extends RuntimeException {

  public AccessDeniedException() {

  }

  public AccessDeniedException(String message) {
    super(message);
  }
}
