package org.jboss.errai.otec.client;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;

/**
 * @author Mike Brock
 */
public class ClientOTBusService {

  public static void startOTService(final MessageBus messageBus, final OTEngine engine) {
    messageBus.subscribe("ClientOTEngine", new MessageCallback() {
      @Override
      public void callback(Message message) {
        final OpDto opDto = message.getValue(OpDto.class);

        engine.receive("<ServerEngine>",  opDto.otOperation(engine));
      }
    });
  }
}
