package org.jboss.errai.bus.client.tests.support;

import org.jboss.errai.bus.client.api.CallableFuture;
import org.jboss.errai.bus.server.annotations.Remote;

/**
 * @author Mike Brock
 */
@Remote
public interface AsyncRPCService {
  public CallableFuture<String> doSomeTask();
}
