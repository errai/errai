package org.jboss.errai.bus.client.api;

import org.jboss.errai.bus.client.framework.ClientMessageBus;
import org.jboss.errai.common.client.api.Assert;

public final class BusLifecycleEvent {

  private ClientMessageBus bus;

  public BusLifecycleEvent(ClientMessageBus bus) {
    this.bus = Assert.notNull(bus);
  }

  public ClientMessageBus getBus() {
    return bus;
  }
}
