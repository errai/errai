package org.jboss.errai.bus.client.tests.support;

import org.jboss.errai.bus.server.annotations.Remote;

/**
 * Part of the regression test suite for ERRAI-282. The service method
 * {@link BaseService#baseServiceMethod()} (inherited here but not redeclared)
 * should be visible to and usable by clients.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@Remote
public interface SubService extends BaseService {

  /**
   * Returns the number of times this method has been invoked, starting with a
   * return value of 1 for the first invocation.
   */
  int subServiceMethod();

}
