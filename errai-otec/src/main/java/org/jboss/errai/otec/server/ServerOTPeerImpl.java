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
  protected final Map<Integer, PeerData> peerDataMap = new ConcurrentHashMap<Integer, PeerData>();
  protected volatile boolean synced;

  public ServerOTPeerImpl(final String remoteEngineId, final MessageBus bus) {
    this.queueId = remoteEngineId;
    this.bus = bus;
  }

  @Override
  public String getId() {
    return queueId;
  }

  @Override
  public void sendPurgeHint(final Integer entityId, final int revision) {
    CommandMessage.create()
        .toSubject("ClientOTEngine")
        .set("PurgeHint", revision)
        .set("EntityId", entityId)
        .set(MessageParts.SessionID, queueId)
        .set(MessageParts.PriorityProcessing, "1")
        .sendNowWith(bus);
  }


  @Override
  public void send(final OTOperation operation) {
    CommandMessage.create()
        .toSubject("ClientOTEngine")
        .set(MessageParts.Value, OpDto.fromOperation(operation, getLastKnownRemoteSequence(operation.getEntityId())))
        .set(MessageParts.SessionID, queueId)
        .set(MessageParts.PriorityProcessing, "1")
        .sendNowWith(bus);

    getPeerData(operation.getEntityId()).setLastKnownTransmittedSequence(operation.getRevision());
  }

  @Override
  public void forceResync(final Integer entityId, final int revision, final String state) {
    synced = false;
    CommandMessage.create()
        .toSubject("ClientOTEngineSyncService")
        .set(MessageParts.Value, entityId)
        .set(MessageParts.SessionID, queueId)
        .set(MessageParts.PriorityProcessing, "1")
        .sendNowWith(bus);
  }

  @Override
  public boolean isSynced() {
    return synced;
  }

  public void setSynced(boolean synced) {
    this.synced = synced;
  }

  @Override
  public void beginSyncRemoteEntity(final String peerId, final int entityId, final EntitySyncCompletionCallback<State> callback) {
  }

  @Override
  public void setLastKnownRemoteSequence(final Integer entity, final int sequence) {
    getPeerData(entity).setLastKnownRemoteSequence(sequence);
  }

  @Override
  public int getLastKnownRemoteSequence(final Integer entity) {
    return getPeerData(entity).getLastKnownRemoteSequence();
  }

  @Override
  public int getLastTransmittedSequence(final Integer entity) {
    final AtomicInteger seq = lastSentSequences.get(entity);
    return seq == null ? 0 : seq.get();
  }


  protected PeerData getPeerData(final Integer entityId) {
     PeerData peerData = peerDataMap.get(entityId);
     if (peerData == null) {
       peerDataMap.put(entityId, peerData = new PeerData());
     }
     return peerData;
   }

  static class PeerData {
    volatile AtomicInteger lastKnownRemoteSequence = new AtomicInteger();
    volatile AtomicInteger lastKnownTransmittedSequence = new AtomicInteger();

    public int getLastKnownRemoteSequence() {
      return lastKnownRemoteSequence.get();
    }

    public void setLastKnownRemoteSequence(final int lastKnownRemoteSequence) {
      this.lastKnownRemoteSequence.set(lastKnownRemoteSequence);
    }

    public int getLastKnownTransmittedSequence() {
      return lastKnownTransmittedSequence.get();
    }

    public void setLastKnownTransmittedSequence(final int lastKnownTransmittedSequence) {
      this.lastKnownTransmittedSequence.set(lastKnownTransmittedSequence);
    }
  }
}
