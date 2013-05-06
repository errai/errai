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

import org.jboss.errai.otec.client.OTClientEngine;
import org.jboss.errai.otec.client.OTEntity;
import org.jboss.errai.otec.client.State;
import org.jboss.errai.otec.client.StringState;
import org.jboss.errai.otec.client.TransactionLog;
import org.jboss.errai.otec.client.util.OTLogUtil;
import org.jboss.errai.otec.harness.OTTestingLogger;
import org.jboss.errai.otec.server.OTServerEngine;
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

    peer(clientEngineA, serverEngine);
    peer(clientEngineB, serverEngine);

    final StringState state = StringState.of(initialState);
    serverEntity = serverEngine.getEntityStateSpace().addEntity(state);

    clientEngineA.syncRemoteEntity(serverEngine.getId(), serverEntity.getId(), new MockEntitySyncCompletionCallback());
    clientEngineB.syncRemoteEntity(serverEngine.getId(), serverEntity.getId(), new MockEntitySyncCompletionCallback());
  }

  protected void assertAllLogsConsistent(final String expectedResult, final String initialState) {
    System.out.println();
    System.out.println("===================================================");
    System.out.println("\nCLIENT LOG REPLAYS:\n");

    final State clientAState = StringState.of(initialState);
    final State clientBState = StringState.of(initialState);
    final State serverState = StringState.of(initialState);

    final TransactionLog transactionLogA =
        clientEngineA.getEntityStateSpace().getEntity(serverEntity.getId()).getTransactionLog();
    final TransactionLog transactionLogB =
        clientEngineB.getEntityStateSpace().getEntity(serverEntity.getId()).getTransactionLog();
    final TransactionLog serverLog = serverEntity.getTransactionLog();

    transactionLogA.cleanLog();
    transactionLogB.cleanLog();
    serverLog.cleanLog();

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
    assertSystemAssertionsEnabled();
    OTLogUtil.setLogAdapter(new OTTestingLogger());

    System.out.println("\n" + OTTestingLogger.repeat('*', 30) + " Starting: " + name.getMethodName() + " "
        + OTTestingLogger.repeat('*', 30));
    OTLogUtil.printLogTitle();
  }

  @After
  public void tearDown() throws Exception {
  }
}
