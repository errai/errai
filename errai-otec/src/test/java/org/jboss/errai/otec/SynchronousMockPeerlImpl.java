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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jboss.errai.otec.client.OTEngine;
import org.jboss.errai.otec.client.OTEntity;
import org.jboss.errai.otec.client.StringState;
import org.jboss.errai.otec.client.Transformer;
import org.jboss.errai.otec.client.operation.OTOperation;
import org.jboss.errai.otec.client.operation.OTOperationImpl;
import org.jboss.errai.otec.client.util.OTLogUtil;

/**
 * @author Mike Brock
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class SynchronousMockPeerlImpl extends AbstractMockPeer {

  public SynchronousMockPeerlImpl(final OTEngine localEngine, final OTEngine engine) {
    this.localEngine = localEngine;
    this.remoteEngine = engine;
  }

  @Override
  public String getId() {
    return remoteEngine.getId();
  }

  @Override
  public void send(final OTOperation operation) {
    OTLogUtil.log("TRANSMIT",
        operation.toString(),
        localEngine.toString(),
        remoteEngine.toString(),
        operation.getRevision(),
        "\"" + localEngine.getEntityStateSpace().getEntity(operation.getEntityId()).getState().get() + "\"");

    //note: this is simulating sending these operations over the wire.
    if (!remoteEngine.receive(localEngine.getId(), OTOperationImpl.createLocalOnlyOperation(remoteEngine, operation, getLastKnownRemoteSequence(operation.getEntityId())))) {
//      final OTEntity entity = localEngine.getEntityStateSpace().getEntity(operation.getEntityId());
//      forceResync(operation.getEntityId(), entity.getRevision(), String.valueOf(entity.getState().get()));
    }
    getPeerData(operation.getEntityId()).setLastKnownTransmittedSequence(operation.getRevision());
  }

  @Override
  public void sendPurgeHint(Integer entityId, int revision) {
  }

  @Override
  public void forceResync(Integer entityId, int revision, String state) {
    final OTEntity entity = remoteEngine.getEntityStateSpace().getEntity(entityId);
   // final String cliState = String.valueOf(entity.getState().get());
    final List<OTOperation> canonLog = entity.getTransactionLog().getCanonLog();

    final List<OTOperation> replayOver = new ArrayList<OTOperation>();
    for (OTOperation operation : canonLog) {
      if (operation.getAgentId().equals(remoteEngine.getId())) {
        replayOver.add(operation);
      }
    }

    entity.getTransactionLog().purgeTo(entity.getRevision());
    entity.getState().syncStateFrom(StringState.of(state));
    entity.setRevision(revision);
    entity.resetRevisionCounterTo(revision);

    final Collection<OTOperation> combined = Transformer.opCombinitator(remoteEngine, replayOver);

//    for (OTOperation operation : combined) {
//      remoteEngine.notifyOperation(operation);
//    }

 //   System.out.println();
  }
}
