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

package org.jboss.errai.otec.mutation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Mike Brock
 */
public class OTEngineImpl implements OTEngine {
  private final String engineId;

  private final PeerState peerState;
  private final OTEntityState entityState = new OTEntityStateImpl();

  private OTEngineImpl(PeerState peerState) {
    engineId = GUIDUtil.createGUID();
    this.peerState = peerState;
  }

  public static OTEngine createEngineWithSinglePeer() {
    return new OTEngineImpl(new SinglePeerState());
  }

  public static OTEngine createEngineWithMultiplePeers() {
    return new OTEngineImpl(new MultiplePeerState());
  }

  @Override
  public ReceiveHandler getReceiveHandler(final String peerId, final Integer entityId) {
    final OTEntity entity = entityState.getEntity(entityId);

    if (entity == null) {
      throw new OTException("could not find entity for reference: " + entityId);
    }

    final OTPeer peer = peerState.getPeer(peerId);

    return new ReceiveHandler() {
      @Override
      public void receive(final OTOperation operation) {
        Transformer.createTransformer(entity, peer, operation).transform();

        // broadcast to all other peers subscribed to this entity
        final Set<OTPeer> peers = getPeerState().getPeersFor(entity);
        for (OTPeer otPeer : peers) {
          if (otPeer != peer) {
            otPeer.send(entityId, operation);
          }
        }
      }
    };
  }

  @Override
  public InitialStateReceiveHandler getInitialStateReceiveHandler(final String peerId, final Integer entityId) {
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
  public void syncRemoteEntity(final String peerId, final Integer entityId, final EntitySyncCompletionCallback callback) {
    final OTPeer peer = getPeerState().getPeer(peerId);
    assertPeerNotNull(peer);

    peer.beginSyncRemoteEntity(peerId, entityId, callback);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void applyOperationLocally(final OTOperationImpl otOperation) {
    final OTEntity entity = otOperation.getEntity();
    final State state = entity.getState();
    for (final Mutation mutation : otOperation.getMutations()) {
      mutation.apply(state);
    }
    entity.getTransactionLog().appendLog(otOperation);
  }

  @Override
  public void notifyOperation(final OTOperationImpl operation) {
    final OTEntity entity = operation.getEntity();
    final Collection<OTPeer> peersFor = getPeerState().getPeersFor(entity);

    for (final OTPeer peer : peersFor) {
      peer.send(entity.getId(), operation);
    }
  }

  @Override
  public OTOperationsFactory getOperationsFactory() {
    return new OTOperationsFactory() {
      @Override
      public OTOperationsListBuilder createOperation(final OTEntity entity) {
        return new OTOperationsListBuilder() {
          List<Mutation> operationList = new ArrayList<Mutation>();

          @Override
          public OTOperationsListBuilder add(final MutationType type, final Position position, final Data data) {
            operationList.add(
                new StringMutation(entity.getNewRevisionNumber(), type, (IndexPosition) position, (CharacterData) data)
            );
            return this;
          }

          @Override
          public OTOperationImpl build() {
            return new OTOperationImpl(operationList, entity);
          }

          @Override
          public OTOperationsListBuilder add(final MutationType type, final Position position) {
            operationList.add(new StringMutation(entity.getNewRevisionNumber(), type, (IndexPosition) position, null));
            return this;
          }
        };
      }
    };
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
  public void associateEntity(String peerId, Integer entityId) {
    final OTPeer peer = getPeerState().getPeer(peerId);
    if (peer == null) {
      throw new OTException("no peer for id: " + peerId);
    }

    final OTEntity entity = getEntityStateSpace().getEntity(entityId);
    if (entity == null) {
      throw new OTException("not entity for id: " + entityId);
    }


    getPeerState().associateEntity(peer, entity);
  }

  @Override
  public void disassociateEntity(String peerId, Integer entityId) {
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

  @Override
  public String getId() {
    return engineId;
  }
}
