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

import static junit.framework.Assert.assertEquals;

import org.jboss.errai.otec.client.AbstractOTEngine;
import org.jboss.errai.otec.client.OTClientEngine;
import org.jboss.errai.otec.client.OTEngine;
import org.jboss.errai.otec.client.OTEntity;
import org.jboss.errai.otec.client.OTPeer;
import org.jboss.errai.otec.client.State;
import org.jboss.errai.otec.client.StringState;
import org.jboss.errai.otec.client.TransactionLog;
import org.jboss.errai.otec.client.mutation.MutationType;
import org.jboss.errai.otec.client.operation.OTOperation;
import org.jboss.errai.otec.client.operation.OTOperationsFactory;
import org.jboss.errai.otec.server.OTServerEngine;
import org.junit.Test;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class FiveEngineTest extends AbstractOtecTest {
  OTClientEngine clientEngineA;
  OTClientEngine clientEngineB;
  OTClientEngine clientEngineC;
  OTClientEngine clientEngineD;
  AbstractOTEngine serverEngine;
  OTEntity serverEntity;

  @Override
  protected OTPeer createPeerFor(final OTEngine local, final OTEngine remote) {
    return new SynchronousMockPeerlImpl(local, remote);
  }

  protected void stopServerEngineAndWait() {
    serverEngine.stop(true);
  }

  protected void setupEngines(final String initialState) {
    clientEngineA = (OTClientEngine) OTClientEngine.createEngineWithSinglePeer("ClientA");
    clientEngineB = (OTClientEngine) OTClientEngine.createEngineWithSinglePeer("ClientB");
    clientEngineC = (OTClientEngine) OTClientEngine.createEngineWithSinglePeer("ClientC");
    clientEngineD = (OTClientEngine) OTClientEngine.createEngineWithSinglePeer("ClientD");
    serverEngine = (OTServerEngine) SynchronousServerEngine.createEngineWithMultiplePeers("Server");

    peer(clientEngineA, serverEngine);
    peer(clientEngineB, serverEngine);
    peer(clientEngineC, serverEngine);
    peer(clientEngineD, serverEngine);

    final StringState state = StringState.of(initialState);
    serverEntity = serverEngine.getEntityStateSpace().addEntity(state);

    clientEngineA.syncRemoteEntity(serverEngine.getId(), serverEntity.getId(), new MockEntitySyncCompletionCallback());
    clientEngineB.syncRemoteEntity(serverEngine.getId(), serverEntity.getId(), new MockEntitySyncCompletionCallback());
    clientEngineC.syncRemoteEntity(serverEngine.getId(), serverEntity.getId(), new MockEntitySyncCompletionCallback());
    clientEngineD.syncRemoteEntity(serverEngine.getId(), serverEntity.getId(), new MockEntitySyncCompletionCallback());
  }

  protected void assertAllLogsConsistent(final String expectedResult, final String initialState) {
    System.out.println();
    System.out.println("===================================================");
    System.out.println("\nCLIENT LOG REPLAYS:\n");

    final State clientAState = StringState.of(initialState);
    final State clientBState = StringState.of(initialState);
    final State clientCState = StringState.of(initialState);
    final State clientDState = StringState.of(initialState);
    final State serverState = StringState.of(initialState);

    final TransactionLog transactionLogA =
        clientEngineA.getEntityStateSpace().getEntity(serverEntity.getId()).getTransactionLog();
    final TransactionLog transactionLogB =
        clientEngineB.getEntityStateSpace().getEntity(serverEntity.getId()).getTransactionLog();
    final TransactionLog transactionLogC =
        clientEngineC.getEntityStateSpace().getEntity(serverEntity.getId()).getTransactionLog();
    final TransactionLog transactionLogD =
        clientEngineD.getEntityStateSpace().getEntity(serverEntity.getId()).getTransactionLog();

    final TransactionLog serverLog = serverEntity.getTransactionLog();

    final int revisionA = clientEngineA.getEntityStateSpace().getEntity(serverEntity.getId()).getRevision();
    final int revisionB = clientEngineB.getEntityStateSpace().getEntity(serverEntity.getId()).getRevision();
    final int revisionC = clientEngineC.getEntityStateSpace().getEntity(serverEntity.getId()).getRevision();
    final int revisionD = clientEngineD.getEntityStateSpace().getEntity(serverEntity.getId()).getRevision();

    final int revisionServer = serverEntity.getRevision();

    final String valueA = replayLogAndReturnResult("ClientA", clientAState, revisionA, transactionLogA);
    final String valueB = replayLogAndReturnResult("ClientB", clientBState, revisionB, transactionLogB);
    final String valueC = replayLogAndReturnResult("ClientC", clientCState, revisionC, transactionLogC);
    final String valueD = replayLogAndReturnResult("ClientD", clientDState, revisionD, transactionLogD);
    final String valueServer = replayLogAndReturnResult("Server", serverState, revisionServer, serverLog);

    assertEquals(expectedResult, valueA);
    assertEquals(expectedResult, valueB);
    assertEquals(expectedResult, valueC);
    assertEquals(expectedResult, valueD);
    assertEquals(expectedResult, valueServer);

    assertEquals(revisionServer, revisionA);
    assertEquals(revisionServer, revisionB);
    assertEquals(revisionServer, revisionC);
    assertEquals(revisionServer, revisionD);
    System.out.println("------[end]------");
  }

  @Test
  public void testTransmissionOfSingleClientDelayed() {
    final String initialState = "";
    setupEngines(initialState);

    final OTOperationsFactory opFactoryClientA = clientEngineA.getOperationsFactory();
    final OTEntity clientAEntity = clientEngineA.getEntityStateSpace().getEntity(serverEntity.getId());
    OTOperation insA1 = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, 0, "The brown ")
        .build();

    final OTOperationsFactory opFactoryClientB = clientEngineB.getOperationsFactory();
    final OTEntity clientBEntity = clientEngineB.getEntityStateSpace().getEntity(serverEntity.getId());
    OTOperation insB1 = opFactoryClientB.createOperation(clientBEntity)
        .add(MutationType.Insert, 4, "quick ")
        .build();

    OTOperation insB2 = opFactoryClientB.createOperation(clientBEntity)
        .add(MutationType.Insert, 26, "over the ")
        .build();

    OTOperation insB3 = opFactoryClientB.createOperation(clientBEntity)
        .add(MutationType.Insert, 40, "dog")
        .build();

    final OTOperationsFactory opFactoryClientC = clientEngineC.getOperationsFactory();
    final OTEntity clientCEntity = clientEngineC.getEntityStateSpace().getEntity(serverEntity.getId());
    OTOperation insC1 = opFactoryClientC.createOperation(clientCEntity)
        .add(MutationType.Insert, 10, "fox jumps ")
        .build();

    final OTOperationsFactory opFactoryClientD = clientEngineD.getOperationsFactory();
    final OTEntity clientDEntity = clientEngineD.getEntityStateSpace().getEntity(serverEntity.getId());
    OTOperation insD1 = opFactoryClientD.createOperation(clientDEntity)
        .add(MutationType.Insert, 0, "lazy ")
        .build();

    insD1 = clientEngineD.applyLocally(insD1);
    
    insA1 = clientEngineA.applyLocally(insA1);
    clientEngineA.notifyRemotes(insA1);

    insC1 = clientEngineC.applyLocally(insC1);
    clientEngineC.notifyRemotes(insC1);

    insB1 = clientEngineB.applyLocally(insB1);
    clientEngineB.notifyRemotes(insB1);
    
    insB2 = clientEngineB.applyLocally(insB2);
    clientEngineB.notifyRemotes(insB2);

    clientEngineD.notifyRemotes(insD1);
    
    insB3 = clientEngineB.applyLocally(insB3);
    clientEngineB.notifyRemotes(insB3);

    stopServerEngineAndWait();

    final String expectedState = "The quick brown fox jumps over the lazy dog";
    assertEquals(expectedState, serverEntity.getState().get());
    assertEquals(expectedState, clientAEntity.getState().get());
    assertEquals(expectedState, clientBEntity.getState().get());
    assertEquals(expectedState, clientCEntity.getState().get());
    assertEquals(expectedState, clientDEntity.getState().get());
    assertAllLogsConsistent(expectedState, initialState);
  }
  
  @Test
  public void testTransmissionOfTwoClientsDelayed() {
    final String initialState = "";
    setupEngines(initialState);

    final OTOperationsFactory opFactoryClientA = clientEngineA.getOperationsFactory();
    final OTEntity clientAEntity = clientEngineA.getEntityStateSpace().getEntity(serverEntity.getId());
    OTOperation insA1 = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, 0, "The brown ")
        .build();

    final OTOperationsFactory opFactoryClientB = clientEngineB.getOperationsFactory();
    final OTEntity clientBEntity = clientEngineB.getEntityStateSpace().getEntity(serverEntity.getId());
    OTOperation insB1 = opFactoryClientB.createOperation(clientBEntity)
        .add(MutationType.Insert, 4, "quick ")
        .build();

    OTOperation insB2 = opFactoryClientB.createOperation(clientBEntity)
        .add(MutationType.Insert, 26, "over the ")
        .build();

    OTOperation insB3 = opFactoryClientB.createOperation(clientBEntity)
        .add(MutationType.Insert, 35, "dog")
        .build();

    final OTOperationsFactory opFactoryClientC = clientEngineC.getOperationsFactory();
    final OTEntity clientCEntity = clientEngineC.getEntityStateSpace().getEntity(serverEntity.getId());
    OTOperation insC1 = opFactoryClientC.createOperation(clientCEntity)
        .add(MutationType.Insert, 10, "fox jumps ")
        .build();

    final OTOperationsFactory opFactoryClientD = clientEngineD.getOperationsFactory();
    final OTEntity clientDEntity = clientEngineD.getEntityStateSpace().getEntity(serverEntity.getId());
    OTOperation insD1 = opFactoryClientD.createOperation(clientDEntity)
        .add(MutationType.Insert, 0, "lazy ")
        .build();

    insD1 = clientEngineD.applyLocally(insD1);
    
    insA1 = clientEngineA.applyLocally(insA1);
    clientEngineA.notifyRemotes(insA1);

    insC1 = clientEngineC.applyLocally(insC1);
    clientEngineC.notifyRemotes(insC1);

    insB1 = clientEngineB.applyLocally(insB1);
    clientEngineB.notifyRemotes(insB1);
    
    insB2 = clientEngineB.applyLocally(insB2);
    insB3 = clientEngineB.applyLocally(insB3);

    clientEngineB.notifyRemotes(insB2);
    clientEngineD.notifyRemotes(insD1);
    clientEngineB.notifyRemotes(insB3);

    stopServerEngineAndWait();

    final String expectedState = "The quick brown fox jumps over the lazy dog";
    assertEquals(expectedState, serverEntity.getState().get());
    assertEquals(expectedState, clientAEntity.getState().get());
    assertEquals(expectedState, clientBEntity.getState().get());
    assertEquals(expectedState, clientCEntity.getState().get());
    assertEquals(expectedState, clientDEntity.getState().get());
    assertAllLogsConsistent(expectedState, initialState);
  }
}
