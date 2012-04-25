package org.jboss.errai.bus.server;

import org.jboss.errai.bus.client.tests.support.BaseService;

/**
 * Implementation of BaseService for the ERRAI-282 regression test.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class BaseServiceImpl implements BaseService {

  /**
   * Count of the number of times {@link #baseServiceMethod()} has been called.
   */
  private int baseServiceCallCount;

  @Override
  public int baseServiceMethod() {
    return ++baseServiceCallCount;
  }

}
