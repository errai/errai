package org.jboss.errai.bus.client.framework;

import java.util.Set;

/**
 * Allows tests in the proper package to access private bus features.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class Wormhole {

  public static String changeBusEndpointUrl(ClientMessageBus bus, String newUrl) {
    String oldUrl = ((ClientMessageBusImpl) bus).IN_SERVICE_ENTRY_POINT;
    ((ClientMessageBusImpl) bus).IN_SERVICE_ENTRY_POINT = newUrl;
    return oldUrl;
  }

  public static Set<String> getRemoteSubscriptions(ClientMessageBus bus) {
    return ((ClientMessageBusImpl) bus).getRemoteSubscriptions();
  }

}
