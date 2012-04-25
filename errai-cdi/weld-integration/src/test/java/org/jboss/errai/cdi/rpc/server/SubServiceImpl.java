package org.jboss.errai.cdi.rpc.server;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.cdi.client.remote.SubService;

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
