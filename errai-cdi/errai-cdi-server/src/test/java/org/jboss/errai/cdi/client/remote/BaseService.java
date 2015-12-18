package org.jboss.errai.cdi.client.remote;

/**
 * Service class that exists so that it can be a superinterface of
 * {@link SubService}. Part of the regression tests for ERRAI-282.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public interface BaseService {

  /**
   * Returns the number of times this method has been invoked, starting with a
   * return value of 1 for the first invocation.
   */
  int baseServiceMethod();
}
