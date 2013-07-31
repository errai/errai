package org.jboss.errai.bus.client.framework;

import org.jboss.errai.bus.client.api.ClientMessageBus;

/**
 * Allows tests in the proper package to access private bus features.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class Wormhole {

  public static Fixer changeBusEndpointUrl(ClientMessageBus bus, String newUrl) {
    String oldInEntryPoint = ((ClientMessageBusImpl) bus).IN_SERVICE_ENTRY_POINT;
    String oldOutEntryPoint = ((ClientMessageBusImpl) bus).OUT_SERVICE_ENTRY_POINT;

    ((ClientMessageBusImpl) bus).IN_SERVICE_ENTRY_POINT = newUrl;
    ((ClientMessageBusImpl) bus).OUT_SERVICE_ENTRY_POINT = newUrl;

    System.out.println("CHANGED ENDPOINT TO: " + newUrl);
    return new Fixer(bus, oldInEntryPoint, oldOutEntryPoint);
  }

  public static class Fixer {
    ClientMessageBus bus;
    String oldInEntryPoint;
    String oldOutEntryPoint;

    Fixer(ClientMessageBus bus, String oldInEntryPoint, String oldOutEntryPoint) {
      this.bus = bus;
      this.oldInEntryPoint = oldInEntryPoint;
      this.oldOutEntryPoint = oldOutEntryPoint;
    }

    public void fix() {
      ((ClientMessageBusImpl) bus).IN_SERVICE_ENTRY_POINT = oldInEntryPoint;
      ((ClientMessageBusImpl) bus).OUT_SERVICE_ENTRY_POINT = oldOutEntryPoint;
    }
  }
}
