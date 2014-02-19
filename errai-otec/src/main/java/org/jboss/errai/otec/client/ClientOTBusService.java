package org.jboss.errai.otec.client;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.client.Timer;

/**
 * @author Mike Brock
 */
public class ClientOTBusService {
  
  private static final Logger logger = LoggerFactory.getLogger(ClientOTBusService.class);

  public static void startOTService(final MessageBus messageBus, final OTEngine engine) {
    messageBus.subscribe("ClientOTEngineSyncService", new MessageCallback() {
      @Override
      public void callback(Message message) {
        final Integer value = message.getValue(Integer.class);
        final OTPeer peer = engine.getPeerState().getPeer("<ServerEngine>");

        peer.beginSyncRemoteEntity("<ServerEngine>", value, new StateEntitySyncCompletionCallback(engine, value,
            new EntitySyncCompletionCallback<State>() {
              @Override
              public void syncComplete(OTEntity<State> entity) {
                engine.getPeerState().notifyResync(entity);
              }
            }));
      }
    });

    messageBus.subscribe("ClientOTEngine", new MessageCallback() {
      @Override
      public void callback(Message message) {
        final OpDto opDto = message.getValue(OpDto.class);

        if (opDto == null && message.hasPart("PurgeHint")) {
          final Integer entityId = message.get(Integer.class, "EntityId");
          final Integer purgeHint = message.get(Integer.class, "PurgeHint");
          final int i = engine.getEntityStateSpace().getEntity(entityId).getTransactionLog().purgeTo(purgeHint - 100);

          logger.info("purged " + i + " old entries from log.");
        }
        else {
          final OTPeer peer = engine.getPeerState().getPeer("<ServerEngine>");

          if (!engine.receive("<ServerEngine>", opDto.otOperation(engine))) {
            peer.beginSyncRemoteEntity("<ServerEngine>", opDto.getEntityId(),
                new StateEntitySyncCompletionCallback(engine, opDto.getEntityId(),
                    new EntitySyncCompletionCallback<State>() {
                      @Override
                      public void syncComplete(OTEntity<State> entity) {
                        engine.getPeerState().notifyResync(entity);
                      }
                    }));
          }
        }
      }
    });

    new Timer() {
      @Override
      public void run() {
        logger.info("PURGE EVENT");
        for (final OTEntity otEntity : engine.getEntityStateSpace().getEntities()) {
          engine.getPeerState().getPeer("<ServerEngine>").sendPurgeHint(otEntity.getId(), otEntity.getRevision());
        }
      }
    }.scheduleRepeating(30000);
  }

}
