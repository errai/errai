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

package org.jboss.errai.otec;

import org.jboss.errai.otec.mutation.CharacterMutation;
import org.jboss.errai.otec.mutation.Mutation;
import org.jboss.errai.otec.mutation.MutationType;
import org.jboss.errai.otec.mutation.StringMutation;
import org.jboss.errai.otec.operation.OTOperation;
import org.jboss.errai.otec.operation.OTOperationImpl;
import org.jboss.errai.otec.operation.OTOperationsFactory;
import org.jboss.errai.otec.operation.OTOperationsListBuilder;
import org.jboss.errai.otec.util.GUIDUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Mike Brock
 */
public class OTClientEngine implements OTEngine {
  protected final String engineId;
  protected final PeerState peerState;
  protected final OTEntityState entityState = new OTEntityStateImpl();
  protected volatile OTEngineMode mode = OTEngineMode.Offline;
  private String name;


  protected OTClientEngine(final PeerState peerState, final String name) {
    engineId = GUIDUtil.createGUID();
    if (name == null) {
      this.name = engineId;
    }
    else {
      this.name = name;
    }

    this.peerState = peerState;
  }

  @SuppressWarnings("UnusedDeclaration")
  public static OTEngine createEngineWithSinglePeer() {
    return createEngineWithSinglePeer(null);
  }

  public static OTEngine createEngineWithSinglePeer(final String name) {
    final OTClientEngine otClientEngine = new OTClientEngine(new SinglePeerState(), name);
    otClientEngine.start();
    return otClientEngine;
  }

  public static OTEngine createEngineWithMultiplePeers(final String name) {
    final OTClientEngine otClientEngine = new OTClientEngine(new MultiplePeerState(), name);
    otClientEngine.start();
    return otClientEngine;
  }

  @Override
  public void receive(final String peerId, final int entityId, final OTOperation remoteOp) {

    final List<OTOperation> transformedOps;
    final OTPeer peer = getPeerState().getPeer(peerId);
    final OTEntity entity = getEntityStateSpace().getEntity(entityId);

    if (peerState.hasConflictResolutionPrecedence()) {
      transformedOps = Transformer.createTransformerLocalPrecedence(this, entity, remoteOp).transform();
    }
    else {
      transformedOps = Transformer.createTransformerRemotePrecedence(this, entity, remoteOp).transform();
    }

    // broadcast to all other peers subscribed to this entity
    final Set<OTPeer> peers = getPeerState().getPeersFor(entity);
    for (final OTPeer otPeer : peers) {
      for (final OTOperation op : transformedOps) {
        if (otPeer != peer && !op.isNoop()) {
          otPeer.send(op);
        }
      }
    }
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
        getPeerState().associateEntity(peer, newEntity);
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
  public void notifyOperation(final OTOperation operation) {
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

    final OTEntity entity = getEntityStateSpace().getEntity(operation.getEntityId());

    if (mode == OTEngineMode.Online) {
      for (final OTPeer peer : getPeerState().getPeersFor(entity)) {
        peer.send(operation);
      }
    }
  }

  @Override
  public OTOperationsFactory getOperationsFactory() {
    return new DefaultOTOperationsFactory(this);
  }

  private static void assertPeerNotNull(final OTPeer peer) {
    if (peer == null) {
      throw new OTException("could not find peer for id: " + peer);
    }
  }

  @Override
  public OTEntityState getEntityStateSpace() {
    return entityState;
  }

  private PeerState getPeerState() {
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

    getPeerState().associateEntity(peer, entity);
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

    getPeerState().disassociateEntity(peer, entity);
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

  @Override
  public void start() {
    setMode(OTEngineMode.Online);
  }

  @Override
  public void stop(boolean wait) {
    setMode(OTEngineMode.Offline);
  }

  private void transmitDeferredTransactions() {
    final Map<OTEntity, Set<OTPeer>> entityPeerRelationshipMap = getPeerState().getEntityPeerRelationshipMap();
    for (final Map.Entry<OTEntity, Set<OTPeer>> entry : entityPeerRelationshipMap.entrySet()) {

      for (final OTPeer peer : entry.getValue()) {
        final TransactionLog transactionLog = entry.getKey().getTransactionLog();
        synchronized (transactionLog.getLock()) {
          final Collection<OTOperation> log
              = transactionLog.getLogFromId(peer.getLastTransmittedSequence(entry.getKey()));

          for (final OTOperation op : log) {
            if (getPeerState().shouldForwardOperation(op)) {
              peer.send(op);
            }
          }

          transactionLog.cleanLog();
        }
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

  private static class DefaultOTOperationsFactory implements OTOperationsFactory {
    private final OTClientEngine otEngine;

    public DefaultOTOperationsFactory(final OTClientEngine otEngine) {
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
          return OTOperationImpl.createOperation(otEngine, mutationList, entity.getId(), -1, null, null);
        }
      };
    }
  }
}
