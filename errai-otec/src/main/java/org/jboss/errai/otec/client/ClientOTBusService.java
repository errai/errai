package org.jboss.errai.otec.client;

import com.google.gwt.user.client.Timer;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.common.client.util.LogUtil;

/**
 * @author Mike Brock
 */
public class ClientOTBusService {

  public static void startOTService(final MessageBus messageBus, final OTEngine engine) {
    messageBus.subscribe("ClientOTEngineSyncService", new MessageCallback() {
      @Override
      public void callback(Message message) {
        final Integer value = message.getValue(Integer.class);
        final OTPeer peer = engine.getPeerState().getPeer("<ServerEngine>");
        peer.beginSyncRemoteEntity("<ServerEngine>", value, new EntitySyncCompletionCallback<State>() {
          @Override
          public void syncComplete(OTEntity<State> entity) {
            engine.getPeerState().flushEntityStreams(value);
          }
        });
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

          LogUtil.log("purged " + i + " old entries from log.");
        }
        else {
          engine.receive("<ServerEngine>", opDto.otOperation(engine));
        }
      }
    });

    new Timer() {
      @Override
      public void run() {
        for (OTEntity otEntity : engine.getEntityStateSpace().getEntities()) {
          engine.getPeerState().getPeer("<ServerEngine>").sendPurgeHint(otEntity.getId(), otEntity.getRevision());
        }
      }
    }.scheduleRepeating(30000);
  }
}
