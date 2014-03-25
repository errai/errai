package org.jboss.errai.security.client.local.res;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;

public class CountingMessageCallback implements MessageCallback {
  
  private final Counter counter;
  
  public CountingMessageCallback(final Counter counter) {
    this.counter = counter;
  }

  @Override
  public void callback(final Message message) {
    counter.increment();
  }

}
