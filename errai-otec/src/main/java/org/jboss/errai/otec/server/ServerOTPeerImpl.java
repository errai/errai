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

package org.jboss.errai.otec.server;

import org.jboss.errai.bus.client.api.base.CommandMessage;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.otec.client.EntitySyncCompletionCallback;
import org.jboss.errai.otec.client.OTPeer;
import org.jboss.errai.otec.client.OpDto;
import org.jboss.errai.otec.client.State;
import org.jboss.errai.otec.client.operation.OTOperation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Mike Brock
 */
public class ServerOTPeerImpl implements OTPeer {
  private final String queueId;
  private final MessageBus bus;
  private final Map<Integer, AtomicInteger> lastSentSequences = new ConcurrentHashMap<Integer, AtomicInteger>();

  public ServerOTPeerImpl(String remoteEngineId, MessageBus bus) {
    this.queueId = remoteEngineId;
    this.bus = bus;
  }

  @Override
  public String getId() {
    return queueId;
  }

  @Override
  public void send(OTOperation operation) {
    CommandMessage.create()
        .toSubject("ClientOTEngine")
        .set(MessageParts.Value, OpDto.fromOperation(operation))
        .set(MessageParts.SessionID, queueId)
        .set(MessageParts.PriorityProcessing, "1")
        .sendNowWith(bus);

    AtomicInteger atomicInteger = lastSentSequences.get(operation.getEntityId());
    if (atomicInteger == null) {
      lastSentSequences.put(operation.getEntityId(), atomicInteger = new AtomicInteger());
    }
    atomicInteger.set(operation.getRevision());
  }

  @Override
  public void beginSyncRemoteEntity(String peerId, int entityId, EntitySyncCompletionCallback<State> callback) {
  }

  @Override
  public int getLastKnownRemoteSequence(Integer entity) {
    return 0;
  }

  @Override
  public int getLastTransmittedSequence(Integer entity) {
    final AtomicInteger seq = lastSentSequences.get(entity);
    return seq == null ? 0 : seq.get();
  }
}
