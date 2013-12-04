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

import java.util.HashMap;
import java.util.Map;

import org.jboss.errai.bus.client.api.base.CommandMessage;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.otec.client.operation.OTOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mike Brock
 */
public class ClientOTPeerImpl implements OTPeer {
  private final String id = "<ServerEngine>";
  private final MessageBus bus;
  private final OTEngine engine;
  private final Map<Integer, Integer> lastSentSequences = new HashMap<Integer, Integer>();
  private boolean synced = false;
  
  private static final Logger logger = LoggerFactory.getLogger(ClientOTPeerImpl.class);

  public ClientOTPeerImpl(final MessageBus bus, final OTEngine engine) {
    this.bus = bus;
    this.engine = engine;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void sendPurgeHint(final Integer entityId, final int revision) {
    CommandMessage.create()
        .toSubject("ServerOTEngine")
        .set("PurgeHint", revision)
        .set("EntityId", entityId)
        .set(MessageParts.PriorityProcessing, "1")
        .sendNowWith(bus);

    logger.debug("SEND PURGE HINT: " + revision) ;
  }

  @Override
  public void send(final OTOperation operation) {
    CommandMessage.create()
        .toSubject("ServerOTEngine")
        .set(MessageParts.Value, OpDto.fromOperation(operation, getLastTransmittedSequence(operation.getEntityId())))
        .set(MessageParts.PriorityProcessing, "1")
        .set("lTX", getLastKnownRemoteSequence(operation.getEntityId()))
        .sendNowWith(bus);

    logger.debug("TRANSMIT:" + operation);

    lastSentSequences.put(operation.getEntityId(), operation.getRevision());
  }

  @Override
  public void beginSyncRemoteEntity(final String peerId,
                                    final int entityId,
                                    final EntitySyncCompletionCallback<State> callback) {
    synced = false;

    final EntitySyncCompletionCallback<State> completionCallback
        = new StateEntitySyncCompletionCallback(engine, entityId, callback);

    MessageBuilder.createMessage()
        .toSubject("ServerOTEngineSyncService")
        .withValue(entityId)
        .noErrorHandling().repliesTo(new EntitySyncCallback(engine, completionCallback)).sendNowWith(bus);
  }

  public void setSynced(final boolean synced) {
    this.synced = synced;
  }

  @Override
  public void forceResync(final Integer entityId, final int revision, final String state) {
  }

  @Override
  public void setLastKnownRemoteSequence(final Integer entity, final int sequence) {
  }

  @Override
  public int getLastKnownRemoteSequence(final Integer entity) {
    return 0;
  }

  @Override
  public boolean isSynced() {
    return synced;
  }

  @Override
  public int getLastTransmittedSequence(final Integer entity) {
    final Integer seq = lastSentSequences.get(entity);
    return seq == null ? 0 : seq;
  }
}
