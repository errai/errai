package org.jboss.errai.security.client.local.handler;

import org.jboss.errai.enterprise.client.jaxrs.api.RestErrorCallback;

/**
 * Security handler for handling a {@link org.jboss.errai.security.shared.exception.SecurityException}.
 */
public interface SecurityExceptionHandler {

  /**
   * Handling of a {@link SecurityException}.
   *
   * @param caught the exception to handle.
   * @return true to continue with default error handling (currently only applicable to {@link RestErrorCallback}.
   */
  boolean handleException(Throwable caught);

}
