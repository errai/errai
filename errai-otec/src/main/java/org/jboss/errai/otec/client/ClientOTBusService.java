package org.jboss.errai.otec.client;

import com.google.gwt.user.client.Timer;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.common.client.util.LogUtil;
import org.jboss.errai.otec.client.operation.OTOperation;

/**
 * @author Mike Brock
 */
public class ClientOTBusService {

  public static void startOTService(final MessageBus messageBus, final OTEngine engine) {
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
          final OTOperation remoteOp = opDto.otOperation(engine);
          LogUtil.log("RECV:" + remoteOp);
          engine.receive("<ServerEngine>", remoteOp);
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
