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

import org.jboss.errai.otec.client.EntitySyncCompletionCallback;
import org.jboss.errai.otec.client.InitialStateReceiveHandler;
import org.jboss.errai.otec.client.OTEngine;
import org.jboss.errai.otec.client.OTEntityState;
import org.jboss.errai.otec.client.OTEntityStateImpl;
import org.jboss.errai.otec.client.OTPeer;
import org.jboss.errai.otec.client.PeerState;
import org.jboss.errai.otec.client.operation.OTOperation;
import org.jboss.errai.otec.client.operation.OTOperationsFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class MockOTEngine implements OTEngine {
  protected final OTEntityState entityState = new OTEntityStateImpl();
  private final List<OTOperation> notifiedOps = new ArrayList<OTOperation>();

  @Override
  public String getId() {
    return "Mock!";
  }

  @Override
  public InitialStateReceiveHandler getInitialStateReceiveHandler(String peerId, int entityId) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public void syncRemoteEntity(String peerId, int entityId, EntitySyncCompletionCallback callback) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public void notifyOperation(OTOperation operation) {
    notifiedOps.add(operation);
  }

  @Override
  public OTEntityState getEntityStateSpace() {
    return entityState;
  }

  @Override
  public OTOperationsFactory getOperationsFactory() {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public void associateEntity(String peerId, int entityId) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public void disassociateEntity(String peerId, int entityId) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public void registerPeer(OTPeer peer) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public String getName() {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public void start() {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public void stop(boolean wait) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public boolean receive(String peerId, OTOperation remoteOp) {
    notifiedOps.add(remoteOp);
    return true;
  }

  @Override
  public PeerState getPeerState() {
    throw new UnsupportedOperationException();
  }

  public List<OTOperation> getNotifiedOperations() {
    return notifiedOps;
  }

}
