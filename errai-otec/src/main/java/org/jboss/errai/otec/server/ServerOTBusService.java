package org.jboss.errai.otec.server;

import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.laundry.Laundry;
import org.jboss.errai.bus.client.api.laundry.LaundryListProviderFactory;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.otec.client.OTEngine;
import org.jboss.errai.otec.client.OTEntity;
import org.jboss.errai.otec.client.OTPeer;
import org.jboss.errai.otec.client.OpDto;

/**
 * @author Mike Brock
 */
public class ServerOTBusService {
  public static void startOTService(final MessageBus messageBus, final OTEngine engine) {
    messageBus.subscribe("ServerOTEngine", new MessageCallback() {
      @Override
      public void callback(Message message) {
        final OpDto opDto = message.getValue(OpDto.class);

        final String session = message.getResource(QueueSession.class, "Session").getSessionId();

        engine.receive(session, opDto.otOperation(engine));
      }
    });

    messageBus.subscribe("ServerOTEngineSyncService", new MessageCallback() {
      @Override
      public void callback(Message message) {
        Integer entityId = message.getValue(Integer.class);

        final QueueSession queueSession = message.getResource(QueueSession.class, "Session");
        final String session = queueSession.getSessionId();

        OTPeer peer = engine.getPeerState().getPeer(session);
        if (peer == null) {
          engine.getPeerState().registerPeer(peer = new ServerOTPeerImpl(session, messageBus));

          final OTPeer _peer = peer;

          LaundryListProviderFactory.get()
              .getLaundryList(queueSession)
              .add(new Laundry() {
                @Override
                public void clean() throws Exception {
                  engine.getPeerState().deregisterPeer(_peer);
                }
              });
        }

        engine.getPeerState().associateEntity(peer, entityId);

        final OTEntity entity = engine.getEntityStateSpace().getEntity(entityId);
        MessageBuilder.createConversation(message)
            .subjectProvided()
            .withValue(entity.getState().get())
            .with("revision", entity.getRevision())
            .noErrorHandling().reply();
      }
    });
  }
}
