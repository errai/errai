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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.jboss.errai.otec.client.atomizer.EntityChangeStream;
import org.jboss.errai.otec.client.operation.OTOperation;

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Mike Brock
 */
public class SinglePeerState implements PeerState {
  OTPeer remotePeer;
  final Map<Integer, Boolean> associatedEntities = new IdentityHashMap<Integer, Boolean>();
  private final Multimap<Integer, EntityChangeStream> entityChangeStreamList
      = HashMultimap.create();
  private final Multimap<Integer, ResyncListener> resyncListeners
      = HashMultimap.create();

  @Override
  public OTPeer getPeer(final String peerId) {
    return remotePeer;
  }

  @Override
  public void registerPeer(final OTPeer peer) {
    if (remotePeer != null) {
      throw new OTException("peer already registered for SinglePeerState!");
    }

    remotePeer = peer;
  }

  @Override
  public void deregisterPeer(final OTPeer peer) {
  }

  @Override
  public Map<Integer, Set<OTPeer>> getEntityPeerRelationshipMap() {
    final Map<Integer, Set<OTPeer>> entityPeerMap = new HashMap<Integer, Set<OTPeer>>(associatedEntities.size() * 2);
    for (final Integer associatedEntity : associatedEntities.keySet()) {
      entityPeerMap.put(associatedEntity, Collections.singleton(remotePeer));
    }
    return Collections.unmodifiableMap(entityPeerMap);
  }

  @Override
  public Set<OTPeer> getPeersFor(final Integer entity) {
    if (remotePeer == null) {
      return Collections.emptySet();
    }
    else {
      return Collections.singleton(remotePeer);
    }
  }

  @Override
  public void forceResyncAll(OTEntity entity) {
  }

  @Override
  public void addResyncListener(Integer entity, ResyncListener resyncListener) {
    resyncListeners.put(entity, resyncListener);
  }

  @Override
  public void notifyResync(OTEntity entity) {
    for (ResyncListener resyncListener : resyncListeners.get(entity.getId())) {
      resyncListener.onResync(entity);
    }
  }

  @Override
  public void associateEntity(final OTPeer peer, final Integer entity) {
    associatedEntities.put(entity, Boolean.TRUE);
  }

  @Override
  public void disassociateEntity(final OTPeer peer, final Integer entity) {
    associatedEntities.remove(entity);
  }

  @Override
  public EntityStreamRegistration addEntityStream(final EntityChangeStream stream) {
    entityChangeStreamList.put(stream.getEntityId(), stream);
    return new EntityStreamRegistration() {
      @Override
      public void remove() {
         entityChangeStreamList.remove(stream.getEntityId(), stream);
      }
    };
  }

  @Override
  public void flushEntityStreams(final Integer entityId) {
    for (final EntityChangeStream entityChangeStream : entityChangeStreamList.get(entityId)) {
      entityChangeStream.flush();
    }
  }

  @Override
  public boolean shouldForwardOperation(final OTOperation operation) {
    return operation.shouldPropagate();
  }

  @Override
  public boolean hasConflictResolutionPrecedence() {
    return false;
  }
}
