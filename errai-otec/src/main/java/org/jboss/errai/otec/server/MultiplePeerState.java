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

import org.jboss.errai.otec.client.EntityStreamRegistration;
import org.jboss.errai.otec.client.OTEntity;
import org.jboss.errai.otec.client.ResyncListener;
import org.jboss.errai.otec.client.atomizer.EntityChangeStream;
import org.jboss.errai.otec.client.OTPeer;
import org.jboss.errai.otec.client.PeerState;
import org.jboss.errai.otec.client.operation.OTOperation;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Christian Sadilek
 * @author Mike Brock
 */
public class MultiplePeerState implements PeerState {
  private final Map<String, OTPeer> peers = new ConcurrentHashMap<String, OTPeer>();
  private final Map<Integer, Set<OTPeer>> associatedEntities = new ConcurrentHashMap<Integer, Set<OTPeer>>();

  @Override
  public void registerPeer(final OTPeer peer) {
    peers.put(peer.getId(), peer);
  }

  @Override
  public void deregisterPeer(OTPeer peer) {
    peers.remove(peer);
    for (Set<OTPeer> otPeers : associatedEntities.values()) {
      otPeers.remove(peer);
    }
  }

  @Override
  public OTPeer getPeer(final String peerId) {
    return peers.get(peerId);
  }

  @Override
  public Set<OTPeer> getPeersFor(final Integer entity) {
    final Set<OTPeer> otPeers = associatedEntities.get(entity);
    return otPeers == null ? Collections.<OTPeer>emptySet() : Collections.unmodifiableSet(otPeers);
  }

  @Override
  public Map<Integer, Set<OTPeer>> getEntityPeerRelationshipMap() {
    return Collections.unmodifiableMap(associatedEntities);
  }

  @Override
  public void associateEntity(final OTPeer peer, final Integer entity) {
    Set<OTPeer> peers = associatedEntities.get(entity);
    if (peers == null) {
      peers = Collections.newSetFromMap(new ConcurrentHashMap<OTPeer, Boolean>());
      associatedEntities.put(entity, peers);
    }
    peers.add(peer);
  }

  @Override
  public void disassociateEntity(final OTPeer peer, final Integer entity) {
    final Set<OTPeer> peers = associatedEntities.get(entity);
    if (peer != null) {
      peers.remove(peer);
    }
  }

  @Override
  public EntityStreamRegistration addEntityStream(EntityChangeStream stream) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void flushEntityStreams(Integer entityId) {
   // throw new UnsupportedOperationException();
  }

  @Override
  public boolean shouldForwardOperation(final OTOperation operation) {
    return true;
  }

  @Override
  public void forceResyncAll(OTEntity entity) {
    for (final OTPeer otPeer : getPeersFor(entity.getId())) {
      otPeer.forceResync(entity.getId(), entity.getRevision(), (String) entity.getState().get());
    }
  }

  @Override
  public boolean hasConflictResolutionPrecedence() {
    return true;
  }

  @Override
  public void addResyncListener(Integer entity, ResyncListener resyncListener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void notifyResync(OTEntity entity) {
    throw new UnsupportedOperationException();
  }
}
