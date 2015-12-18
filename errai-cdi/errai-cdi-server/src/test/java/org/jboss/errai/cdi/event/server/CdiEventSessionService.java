package org.jboss.errai.cdi.event.server;

import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.server.annotations.Service;


public class CdiEventSessionService {

  /**
   * Invalidates the current session for testing purposes.
   */
  @Service
  public void queueSessionInvalidationService(Message message) {
    QueueSession session = message.getResource(QueueSession.class, "Session");
    session.endSession();
  }
}
