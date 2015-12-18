package org.jboss.errai.bus.server;

import org.jboss.errai.bus.client.tests.support.SubService;
import org.jboss.errai.bus.server.annotations.Service;

@Service
public class SubServiceImpl extends BaseServiceImpl implements SubService {

  /**
   * Count of the number of times {@link #subServiceMethod()} has been called.
   */
  private int subServiceCallCount;

  @Override
  public int subServiceMethod() {
    return ++subServiceCallCount;
  }

}
