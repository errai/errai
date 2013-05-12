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

import org.jboss.errai.otec.client.atomizer.EntityChangeStream;
import org.jboss.errai.otec.client.operation.OTOperation;

import java.util.Map;
import java.util.Set;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Mike Brock
 */
public interface PeerState {
  public void registerPeer(OTPeer peer);
  public void deregisterPeer(OTPeer peer);

  public OTPeer getPeer(String peerId);
  public Set<OTPeer> getPeersFor(Integer entity);
  public Map<Integer, Set<OTPeer>> getEntityPeerRelationshipMap();

  public void associateEntity(OTPeer peer, Integer entity);
  public void disassociateEntity(OTPeer peer, Integer entity);

  public boolean shouldForwardOperation(OTOperation operation);

  public boolean hasConflictResolutionPrecedence();

  public void addResyncListener(Integer entity, ResyncListener resyncListener);
  public void forceResyncAll(OTEntity entity);
  public void notifyResync(OTEntity entity);

  public EntityStreamRegistration addEntityStream(EntityChangeStream stream);
  public void flushEntityStreams(Integer entityId);
}
