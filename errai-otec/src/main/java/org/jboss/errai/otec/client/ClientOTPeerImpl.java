/*
 * Copyright 2013 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.otec.client;

import org.jboss.errai.bus.client.api.base.CommandMessage;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.common.client.util.LogUtil;
import org.jboss.errai.otec.client.operation.OTOperation;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mike Brock
 */
public class ClientOTPeerImpl implements OTPeer {
  private final String id = "<ServerEngine>";
  private final MessageBus bus;
  private final OTEngine engine;
  private final Map<Integer, Integer> lastSentSequences = new HashMap<Integer, Integer>();


  public ClientOTPeerImpl(MessageBus bus, OTEngine engine) {
    this.bus = bus;
    this.engine = engine;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void sendPurgeHint(Integer entityId, int revision) {
    CommandMessage.create()
        .toSubject("ServerOTEngine")
        .set("PurgeHint", revision)
        .set("EntityId", entityId)
        .set(MessageParts.PriorityProcessing, "1")
        .sendNowWith(bus);
  }

  @Override
  public void send(OTOperation operation) {
    CommandMessage.create()
        .toSubject("ServerOTEngine")
        .set(MessageParts.Value, OpDto.fromOperation(operation))
        .set(MessageParts.PriorityProcessing, "1")
        .sendNowWith(bus);

    LogUtil.log("TRANSMIT:" + operation);

    lastSentSequences.put(operation.getEntityId(), operation.getRevision());
  }

  @Override
  public void beginSyncRemoteEntity(String peerId, int entityId, final EntitySyncCompletionCallback<State> callback) {
    MessageBuilder.createMessage()
        .toSubject("ServerOTEngineSyncService")
        .withValue(entityId)
        .noErrorHandling().repliesTo(new MessageCallback() {
      @Override
      public void callback(Message message) {
        final OTEntity entity = engine.getEntityStateSpace().addEntity(StringState.of(message.getValue(String.class)));
        final Integer revision = message.get(Integer.class, "revision");
        entity.setRevision(revision);
        entity.resetRevisionCounterTo(revision);
        callback.syncComplete(entity);
      }
    }).sendNowWith(bus);
  }

  @Override
  public void forceResync(Integer entityId, int revision, String state) {
  }

  @Override
  public void setLastKnownRemoteSequence(Integer entity, int sequence) {
  }

  @Override
  public int getLastKnownRemoteSequence(Integer entity) {
    return 0;
  }

  @Override
  public int getLastTransmittedSequence(Integer entity) {
    final Integer seq = lastSentSequences.get(entity);
    return seq == null ? 0 : seq;
  }
}
