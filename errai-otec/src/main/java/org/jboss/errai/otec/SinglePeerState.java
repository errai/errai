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

import org.jboss.errai.otec.operation.OTOperation;

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
  final Set<OTEntity> associatedEntities = Collections.newSetFromMap(new IdentityHashMap<OTEntity, Boolean>());

  @Override
  public OTPeer getPeer(String peerId) {
    return remotePeer;
  }

  @Override
  public void registerPeer(OTPeer peer) {
    if (remotePeer != null) {
      throw new OTException("peer already registered for SinglePeerState!");
    }

    remotePeer = peer;
  }

  @Override
  public Map<OTEntity, Set<OTPeer>> getEntityPeerRelationshipMap() {
    Map<OTEntity, Set<OTPeer>> entityPeerMap = new HashMap<OTEntity, Set<OTPeer>>();
    for (OTEntity associatedEntity : associatedEntities) {
      entityPeerMap.put(associatedEntity, Collections.singleton(remotePeer));
    }
    return Collections.unmodifiableMap(entityPeerMap);
  }

  @Override
  public Set<OTPeer> getPeersFor(OTEntity entity) {
    return Collections.singleton(remotePeer);
  }

  @Override
  public void associateEntity(OTPeer peer, OTEntity entity) {
    associatedEntities.add(entity);
  }

  @Override
  public void disassociateEntity(OTPeer peer, OTEntity entity) {
    associatedEntities.remove(entity);
  }

  @Override
  public boolean shouldForwardOperation(OTOperation operation) {
    return operation.shouldPropagate();
  }
}
