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
import org.jboss.errai.otec.client.operation.OTOperation;

/**
 * @author Mike Brock
 */
public class ServerOTBusService {
  public static void startOTService(final MessageBus messageBus, final OTEngine engine) {
    messageBus.subscribe("ServerOTEngine", new MessageCallback() {
      @Override
      public void callback(final Message message) {
        final OpDto value = message.getValue(OpDto.class);

        if (value == null && message.hasPart("PurgeHint")) {
          final Integer entityId = message.get(Integer.class, "EntityId");
          final Integer purgeHint = message.get(Integer.class, "PurgeHint");
          final QueueSession queueSession = message.getResource(QueueSession.class, "Session");
          final String session = queueSession.getSessionId();
          engine.getPeerState().getPeer(session).setLastKnownRemoteSequence(entityId, purgeHint);
        }
        else {
          final OTOperation remoteOp = value.otOperation(engine);
          final QueueSession session = message.getResource(QueueSession.class, "Session");
          engine.receive(session.getSessionId(), remoteOp);
        }
      }
    });

    messageBus.subscribe("ServerOTEngineSyncService", new MessageCallback() {
      @Override
      public void callback(final Message message) {
        final Integer entityId = message.getValue(Integer.class);

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

        if (message.hasPart("SyncAck")) {
          ((ServerOTPeerImpl) peer).setSynced(true);
        }
        else {
          final OTEntity entity = engine.getEntityStateSpace().getEntity(entityId);
          MessageBuilder.createConversation(message)
              .subjectProvided()
              .withValue(entity.getState().get())
              .with("EntityID", entity.getId())
              .with("revision", entity.getRevision())
              .noErrorHandling().reply();
        }
      }
    });
  }
}
