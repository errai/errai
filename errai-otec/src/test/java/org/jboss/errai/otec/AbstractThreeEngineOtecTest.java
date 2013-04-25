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

import org.jboss.errai.otec.util.OTLogFormat;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

/**
 * @author Mike Brock
 */
public abstract class AbstractThreeEngineOtecTest extends AbstractOtecTest {
  OTClientEngine clientEngineA;
  OTClientEngine clientEngineB;
  OTServerEngine serverEngine;
  OTEntity serverEntity;

  @Rule
  public TestName name = new TestName();

  protected void suspendEngines() {
    clientEngineA.stop(false);
    clientEngineB.stop(false);
    serverEngine.stop(false);
  }

  protected void resumeEnginesAB() {
    serverEngine.start();
    clientEngineA.start();
    clientEngineB.start();
  }

  protected void resumeEnginesBA() {
    serverEngine.start();
    clientEngineB.start();
    clientEngineA.start();
  }

  protected void setupEngines(final String initialState) {
    clientEngineA = (OTClientEngine) OTClientEngine.createEngineWithSinglePeer("ClientA");
    clientEngineB = (OTClientEngine) OTClientEngine.createEngineWithSinglePeer("ClientB");
    serverEngine = (OTServerEngine) OTServerEngine.createEngineWithMultiplePeers("Server");

    clientEngineA.registerPeer(createPeerFor(clientEngineA, serverEngine));
    clientEngineB.registerPeer(createPeerFor(clientEngineB, serverEngine));
    serverEngine.registerPeer(createPeerFor(serverEngine, clientEngineA));
    serverEngine.registerPeer(createPeerFor(serverEngine, clientEngineB));

    final StringState state = new StringState(initialState);
    serverEntity = serverEngine.getEntityStateSpace().addEntity(state);

    clientEngineA.syncRemoteEntity(serverEngine.getId(), serverEntity.getId(), new MockEntitySyncCompletionCallback());
    clientEngineB.syncRemoteEntity(serverEngine.getId(), serverEntity.getId(), new MockEntitySyncCompletionCallback());
  }

  protected void assertAllLogsConsistent(final String expectedResult, final String initialState) {
    System.out.println();
    System.out.println("===================================================");
    System.out.println("\nCLIENT LOG REPLAYS:\n");

    final State clientAState = new StringState(initialState);
    final State clientBState = new StringState(initialState);
    final State serverState = new StringState(initialState);

    final TransactionLog transactionLogA =
        clientEngineA.getEntityStateSpace().getEntity(serverEntity.getId()).getTransactionLog();
    final TransactionLog transactionLogB =
        clientEngineB.getEntityStateSpace().getEntity(serverEntity.getId()).getTransactionLog();
    final TransactionLog serverLog = serverEntity.getTransactionLog();

    final int revisionA = clientEngineA.getEntityStateSpace().getEntity(serverEntity.getId()).getRevision();
    final int revisionB = clientEngineB.getEntityStateSpace().getEntity(serverEntity.getId()).getRevision();
    final int revisionServer = serverEntity.getRevision();

    final String valueA = replayLogAndReturnResult("ClientA", clientAState, revisionA, transactionLogA);
    final String valueB = replayLogAndReturnResult("ClientB", clientBState, revisionB, transactionLogB);
    final String valueServer = replayLogAndReturnResult("Server", serverState, revisionServer, serverLog);

    assertEquals(expectedResult, valueA);
    assertEquals(expectedResult, valueB);
    assertEquals(expectedResult, valueServer);

    assertEquals(revisionServer, revisionA);
    assertEquals(revisionServer, revisionB);

    System.out.println("------[end]------");
  }

  protected void stopServerEngineAndWait() {
    serverEngine.stop(true);
  }

  @Before
  public void setUp() throws Exception {
    System.out.println("\n" + OTLogFormat.repeat('*', 30) + " Starting: " + name.getMethodName() + " "
        + OTLogFormat.repeat('*', 30));
    OTLogFormat.printLogTitle();
  }

  @After
  public void tearDown() throws Exception {}
}
