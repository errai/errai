package org.jboss.errai.security.client.local.res;

import org.jboss.errai.bus.client.api.BusErrorCallback;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.security.shared.exception.SecurityException;

public class ErrorCountingCallback extends BusErrorCallback {
  private final Counter counter;
  private final Class<? extends SecurityException> throwType;

  public ErrorCountingCallback(final Counter counter, final Class<? extends SecurityException> throwType) {
    this.counter = counter;
    this.throwType = throwType;
  }

  @Override
  public boolean error(Message message, Throwable throwable) {
    if (throwable.getClass().equals(throwType)) {
      counter.increment();
      return false;
    }
    else {
      return true;
    }
  }
}