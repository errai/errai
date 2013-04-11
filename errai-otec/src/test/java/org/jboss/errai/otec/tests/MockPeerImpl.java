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

package org.jboss.errai.otec.tests;

import org.jboss.errai.otec.EntitySyncCompletionCallback;
import org.jboss.errai.otec.OTEngine;
import org.jboss.errai.otec.OTEntity;
import org.jboss.errai.otec.OTOperation;
import org.jboss.errai.otec.OTPeer;
import org.jboss.errai.otec.State;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mike Brock
 */
public class MockPeerImpl implements OTPeer {
  private OTEngine localEngine;
  private OTEngine remoteEngine;

  private final Map<Integer, Integer> lastTransmittedSequencees = new HashMap<Integer, Integer>();

  public MockPeerImpl(OTEngine localEngine, OTEngine engine) {
    this.localEngine = localEngine;
    this.remoteEngine = engine;
  }

  @Override
  public String getId() {
    return remoteEngine.getId();
  }

  @Override
  public void send(Integer entityId, OTOperation operation) {
    //note: this is simulating sending these operations over the wire.
    remoteEngine.getReceiveHandler(localEngine.getId(), entityId)
        .receive(operation);

    lastTransmittedSequencees.put(entityId, operation.getRevision());
  }

  public void beginSyncRemoteEntity(String peerId, Integer entityId, EntitySyncCompletionCallback<State> callback) {
    final OTEntity entity = remoteEngine.getEntityStateSpace().getEntity(entityId);
    localEngine.getEntityStateSpace().addEntity(new OTTestEntity(entity));

    localEngine.associateEntity(remoteEngine.getId(), entityId);
    remoteEngine.associateEntity(localEngine.getId(), entityId);

    callback.syncComplete(entity);
  }


  @Override
  public int getLastKnownRemoteSequence(OTEntity entity) {
    return 0;
  }

  @Override
  public int getLastTransmittedSequence(OTEntity entity) {
    final Integer integer = lastTransmittedSequencees.get(entity.getId());
    return integer == null ? 0 : integer;
  }

}
