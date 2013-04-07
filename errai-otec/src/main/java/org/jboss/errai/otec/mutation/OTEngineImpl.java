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

/**
 * @author Mike Brock
 */
public class OTEngineImpl implements OTEngine {
  private final String engineId;

  private final PeerState peerState = new SinglePeerState();
  private final OTEntityState entityState = new OTEntityStateImpl();

  public OTEngineImpl() {
    engineId = GUIDUtil.createGUID();
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
      public void receive(final List<Operation> operations) {
        new Transformer(entity, peer, operations).transform();
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
      public void receive(State obj) {
        final OTEntity newEntity = new OTEntityImpl(entityId, obj);
        entityState.addEntity(newEntity);
        getPeerState().addPeerMonitor(peer, newEntity);
      }
    };
  }

  @SuppressWarnings("unchecked")
  @Override
  public void syncRemoteEntity(String peerId, Integer entityId, EntitySyncCompletionCallback callback) {
    final OTPeer peer = getPeerState().getPeer(peerId);
    assertPeerNotNull(peer);

    peer.beginSyncRemoteEntity(peerId, entityId, callback);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void applyOperationsLocally(OTOperationList operationList) {
    final OTEntity entity = operationList.getEntity();
    final State state = entity.getState();
    for (Operation operation : operationList.getOperations()) {
      operation.apply(state);
      entity.setRevision(operation.getRevision());
    }
  }

  @Override
  public void notifyOperations(OTOperationList operationList) {
    final OTEntity entity = operationList.getEntity();
    final Collection<OTPeer> peersFor = getPeerState().getPeersFor(entity);

    for (final OTPeer peer : peersFor) {
      peer.send(entity.getId(), operationList.getOperations());
    }
  }

  @Override
  public OTOperationsFactory getOperationsFactory() {
    return new OTOperationsFactory() {
      @Override
      public OTOperationsListBuilder createOperationsList(final OTEntity entity) {
        return new OTOperationsListBuilder() {
          List<Operation> operationList = new ArrayList<Operation>();

          @Override
          public OTOperationsListBuilder add(OperationType type, Position position, Data data) {
            operationList.add(new StringOperation(entity.getNewRevisionNumber(), type, (IndexPosition) position, (CharacterData) data));
            return this;
          }

          @Override
          public OTOperationList build() {
            return new OTOperationList(operationList, entity);
          }

          @Override
          public OTOperationsListBuilder add(OperationType type, Position position) {
            operationList.add(new StringOperation(entity.getNewRevisionNumber(), type, (IndexPosition) position, null));
            return this;
          }
        };
      }
    };
  }

  private static void assertPeerNotNull(OTPeer peer) {
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
  public void registerPeer(final OTPeer peer) {
    getPeerState().registerPeer(peer);
  }

  @Override
  public String getId() {
    return engineId;
  }
}
