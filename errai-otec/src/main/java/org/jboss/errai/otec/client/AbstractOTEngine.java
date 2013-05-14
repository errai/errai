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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.jboss.errai.otec.client.mutation.CharacterMutation;
import org.jboss.errai.otec.client.mutation.Mutation;
import org.jboss.errai.otec.client.mutation.MutationType;
import org.jboss.errai.otec.client.mutation.StringMutation;
import org.jboss.errai.otec.client.operation.OTOperation;
import org.jboss.errai.otec.client.operation.OTOperationImpl;
import org.jboss.errai.otec.client.operation.OTOperationsFactory;
import org.jboss.errai.otec.client.operation.OTOperationsListBuilder;
import org.jboss.errai.otec.client.util.GUIDUtil;

/**
 * @author Mike Brock
 */
public abstract class AbstractOTEngine implements OTEngine {
  protected final String engineId;
  protected final PeerState peerState;
  protected final OTEntityState entityState = new OTEntityStateImpl();
  protected volatile OTEngineMode mode = OTEngineMode.Offline;
  protected String name;

  public AbstractOTEngine(final String name, final PeerState peerState) {
    engineId = GUIDUtil.createGUID();
    if (name == null) {
      this.name = engineId;
    }
    else {
      this.name = name;
    }
    this.peerState = peerState;
  }

  @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
  protected OTOperation applyFromRemote(final OTOperation remoteOp) {
    final OTEntity entity = getEntityStateSpace().getEntity(remoteOp.getEntityId());
    synchronized (entity) {
      try {
        getPeerState().flushEntityStreams(entity.getId());
        if (peerState.hasConflictResolutionPrecedence()) {
          return Transformer.createTransformerLocalPrecedence(this, entity, remoteOp).transform();
        }
        else {
          return Transformer.createTransformerRemotePrecedence(this, entity, remoteOp).transform();
        }
      }
      catch (OTException e) {
        return null;
      }
    }
  }

  @Override
  public String getId() {
    return engineId;
  }

  @Override
  public String toString() {
    return getName();
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public InitialStateReceiveHandler getInitialStateReceiveHandler(final String peerId, final int entityId) {
    final OTPeer peer = getPeerState().getPeer(peerId);
    assertPeerNotNull(peer);

    return new InitialStateReceiveHandler() {
      @SuppressWarnings("unchecked")
      @Override
      public void receive(final State obj) {
        final OTEntity newEntity = new OTEntityImpl(entityId, obj);
        entityState.addEntity(newEntity);
        getPeerState().associateEntity(peer, entityId);
      }
    };
  }

  @SuppressWarnings("unchecked")
  @Override
  public void syncRemoteEntity(final String peerId, final int entityId, final EntitySyncCompletionCallback callback) {
    final OTPeer peer = getPeerState().getPeer(peerId);
    assertPeerNotNull(peer);
    peer.beginSyncRemoteEntity(peerId, entityId, callback);
  }

  @Override
  public void notifyOperation(OTOperation operation) {
    notifyRemotes(applyLocally(operation));
  }

  public OTOperation applyLocally(OTOperation operation) {
    final OTEntity entity = getEntityStateSpace().getEntity(operation.getEntityId());

    if (operation.getRevision() == -1) {
      operation = operation.getBasedOn(entity.getRevision());
    }

    operation.apply(entity);

    return operation;
  }

  public void notifyRemotes(final OTOperation operation) {
    if (!operation.shouldPropagate()) {
      return;
    }

    if (mode == OTEngineMode.Online) {
      for (final OTPeer peer : getPeerState().getPeersFor(operation.getEntityId())) {
        peer.send(operation);
      }
    }
  }

  @Override
  public OTOperationsFactory getOperationsFactory() {
    return new DefaultOTOperationsFactory(this);
  }

  @Override
  public OTEntityState getEntityStateSpace() {
    return entityState;
  }

  @Override
  public PeerState getPeerState() {
    return peerState;
  }

  @Override
  public void associateEntity(final String peerId, final int entityId) {
    final OTPeer peer = getPeerState().getPeer(peerId);
    if (peer == null) {
      throw new OTException("no peer for id: " + peerId);
    }

    final OTEntity entity = getEntityStateSpace().getEntity(entityId);
    if (entity == null) {
      throw new OTException("no entity for id: " + entityId);
    }

    getPeerState().associateEntity(peer, entityId);
  }

  @Override
  public void disassociateEntity(final String peerId, final int entityId) {
    final OTPeer peer = getPeerState().getPeer(peerId);
    if (peer == null) {
      throw new OTException("no peer for id: " + peerId);
    }

    final OTEntity entity = getEntityStateSpace().getEntity(entityId);
    if (entity == null) {
      throw new OTException("not entity for id: " + entityId);
    }

    getPeerState().disassociateEntity(peer, entityId);
  }

  @Override
  public void registerPeer(final OTPeer peer) {
    getPeerState().registerPeer(peer);
  }

  protected void setMode(final OTEngineMode mode) {
    if (this.mode == OTEngineMode.Offline && mode == OTEngineMode.Online) {
      transmitDeferredTransactions();
    }
    this.mode = mode;
  }

  private static void assertPeerNotNull(final OTPeer peer) {
    if (peer == null) {
      throw new OTException("could not find peer for id: " + peer);
    }
  }

  private void transmitDeferredTransactions() {
    final Map<Integer, Set<OTPeer>> entityPeerRelationshipMap = getPeerState().getEntityPeerRelationshipMap();
    for (final Map.Entry<Integer, Set<OTPeer>> entry : entityPeerRelationshipMap.entrySet()) {

      for (final OTPeer peer : entry.getValue()) {
        final Integer key = entry.getKey();

        final OTEntity entity = getEntityStateSpace().getEntity(key);

        final TransactionLog transactionLog = entity.getTransactionLog();
        synchronized (transactionLog.getLock()) {
          final List<OTOperation> log = transactionLog.getLog();
          final int lastTransmittedSequence = peer.getLastTransmittedSequence(entry.getKey());
          final List<OTOperation> toSend = new ArrayList<OTOperation>();

          final ListIterator<OTOperation> iter = log.listIterator(log.size());
          while (iter.hasPrevious()) {
            final OTOperation previous = iter.previous();

            toSend.add(previous);

            if (previous.getRevision() == lastTransmittedSequence) {
              Collections.reverse(toSend);
              break;
            }
          }

          for (final OTOperation op : toSend) {
            if (getPeerState().shouldForwardOperation(op)) {
              peer.send(op);
            }
          }

          transactionLog.cleanLog();
        }
      }
    }
  }

  protected static class DefaultOTOperationsFactory implements OTOperationsFactory {
    private final AbstractOTEngine otEngine;

    public DefaultOTOperationsFactory(final AbstractOTEngine otEngine) {
      this.otEngine = otEngine;
    }

    @Override
    public OTOperationsListBuilder createOperation(final OTEntity entity) {
      return new OTOperationsListBuilder() {
        List<Mutation> mutationList = new ArrayList<Mutation>();

        @Override
        public OTOperationsListBuilder add(final MutationType type, final int position, final char data) {
          mutationList.add(
              CharacterMutation.of(type, position, data)
          );
          return this;
        }

        @Override
        public OTOperationsListBuilder add(final MutationType type, final int position, final String data) {
          mutationList.add(
              StringMutation.of(type, position, data)
          );
          return this;
        }

        @Override
        public OTOperation build() {
          return OTOperationImpl.createOperation(otEngine, otEngine.getId(), mutationList, entity.getId(), -1, null, null, -1);
        }

        @Override
        public void submit() {
          otEngine.notifyOperation(build());
        }
      };
    }
  }
}
