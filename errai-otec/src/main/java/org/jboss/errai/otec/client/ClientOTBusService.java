package org.jboss.errai.otec.client;

import com.google.gwt.user.client.Timer;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.common.client.util.LogUtil;
import org.jboss.errai.otec.client.operation.OTOperation;
import org.jboss.errai.otec.client.util.MeyersDiff;

import java.util.LinkedList;

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
          if (!engine.receive("<ServerEngine>", remoteOp)) {
            MessageBuilder.createMessage()
                .toSubject("ServerOTEngineSyncService")
                .withValue(remoteOp.getEntityId())
                .noErrorHandling()
                .repliesTo(new MessageCallback() {
                  @Override
                  public void callback(Message message) {
                    final String value = message.getValue(String.class);
                  //  final OTEntity entity = engine.getEntityStateSpace().addEntity(StringState.of(value));
                  //  final Integer revision = message.get(Integer.class, "revision");
                 //   entity.setRevision(revision);
                  //  entity.resetRevisionCounterTo(revision);
                    final OTEntity entity = engine.getEntityStateSpace().getEntity(remoteOp.getEntityId());
                    final LinkedList<MeyersDiff.Diff> diffs
                        = new MeyersDiff().diff_main(String.valueOf(entity.getState().get()), value);


                  }
                }).sendNowWith(messageBus);
          }
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
