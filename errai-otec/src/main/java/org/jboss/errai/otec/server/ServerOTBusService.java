package org.jboss.errai.otec.server;

import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.laundry.Laundry;
import org.jboss.errai.bus.client.api.laundry.LaundryListProviderFactory;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.server.util.LocalContext;
import org.jboss.errai.otec.client.OTEngine;
import org.jboss.errai.otec.client.OTEntity;
import org.jboss.errai.otec.client.OTPeer;
import org.jboss.errai.otec.client.OpDto;
import org.jboss.errai.otec.client.operation.OTOperation;
import org.jboss.errai.otec.client.util.OTLogUtil;

import java.util.Collections;

/**
 * @author Mike Brock
 */
public class ServerOTBusService {
  public static void startOTService(final MessageBus messageBus, final OTEngine engine) {
    messageBus.subscribe("ServerOTEngine", new MessageCallback() {

      @Override
      public void callback(final Message message) {
        final OpDto value = message.getValue(OpDto.class);
        final QueueSession queueSession = message.getResource(QueueSession.class, "Session");
        final String session = queueSession.getSessionId();
        final OTPeer peer = engine.getPeerState().getPeer(session);

        if (peer == null) {
          System.out.println("SessionID: " + session);
          System.out.println("No session for: " + message.getParts());
          return;
        }

        if (value == null && message.hasPart("PurgeHint")) {
          final Integer purgeHint = message.get(Integer.class, "PurgeHint");
          final Integer entityId = message.get(Integer.class, "EntityId");

          peer.setLastKnownRemoteSequence(entityId, purgeHint);
        }
        else if (value != null) {
          final OTEntity entity = engine.getEntityStateSpace().getEntity(value.getEntityId());
          if (entity == null) {
            return;
          }
          synchronized (entity) {
            ClientDemuxer demux = LocalContext.get(queueSession).getAttribute(ClientDemuxer.class);
            if (demux == null) {
              LocalContext.get(queueSession).setAttribute(ClientDemuxer.class, demux = new ClientDemuxer());
            }

         //   final Collection<OpDto> enginePlanFor = demux.getEnginePlanFor(value);
            for (final OpDto operation : Collections.singletonList(value)) {

              final OTOperation remoteOp = operation.otOperation(engine);

              OTLogUtil.log("RECV", "<<from: " + remoteOp.getAgentId() + ">>" ,
                  "REMOTE", "Server", operation.getRevision(),
                  "\"" + String.valueOf(entity.getState().get()) + "\"");

              if (!engine.receive(session, remoteOp)) {
                System.out.println("*** WARNING: CORRUPT PATHS - MUST RESYNC ALL ***");
                engine.getPeerState().forceResyncAll(entity);
              }
            }
          }
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

        System.out.println("Peer Register: " + session);

        if (message.hasPart("SyncAck")) {
          System.out.println("RECEIVED SYNC ACK");
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
