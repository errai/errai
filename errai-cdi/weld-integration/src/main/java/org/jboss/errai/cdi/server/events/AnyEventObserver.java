package org.jboss.errai.cdi.server.events;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.EventMetadata;

import org.jboss.errai.config.rebind.EnvUtil;

/**
 * Managed bean that observes all server-side events and dispatches them to the
 * connected clients using the {@link EventDispatcher}.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@ApplicationScoped
public class AnyEventObserver {

  private static EventDispatcher eventDispatcher;

  public static void init(EventDispatcher dispatcher) {
    eventDispatcher = dispatcher;
  }

  @SuppressWarnings("unused")
  private void onEvent(@Observes Object event, EventMetadata emd) {
    // Check if initialized
    if (eventDispatcher == null)
      return;

    // Check if the event is a portable Errai CDI event and should be forwarded
    // to all listening clients
    if (EnvUtil.isPortableType(event.getClass())) {
      eventDispatcher.sendEventToClients(event, emd);
    }

  }
}
