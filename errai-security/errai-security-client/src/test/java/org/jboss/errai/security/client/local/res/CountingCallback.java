package org.jboss.errai.security.client.local.res;

import org.jboss.errai.common.client.api.RemoteCallback;

public class CountingCallback implements RemoteCallback<Void> {

  private final Counter counter;

  public CountingCallback(final Counter counter) {
    this.counter = counter;
  }

  @Override
  public void callback(Void response) {
    counter.increment();
  }

}
