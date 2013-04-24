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

import org.jboss.errai.otec.mutation.Mutation;
import org.jboss.errai.otec.operation.OTOperation;
import org.jboss.errai.otec.util.OTLogFormat;
import org.junit.After;
import org.junit.Before;

/**
 * @author Mike Brock
 */
public abstract class AbstractThreeEngineOtecTest {
  private static final String PLAYBACK_FORMAT = "%-30s %-40s\n";
  OTClientEngine clientEngineA;
  OTClientEngine clientEngineB;
  OTServerEngine serverEngine;
  OTEntity serverEntity;

  private static void renderPlaybackHeader(final String stateName, int currentRevision) {
    System.out.println("===================================================");
    System.out.println("NODE: " + stateName + "; CURRENT REVISION: " + currentRevision);
    System.out.println();
    System.out.printf(PLAYBACK_FORMAT, "MUTATION", "STATE");
    System.out.println("---------------------------------------------------");

  }

  private static void renderInitialStatePlayback(final State state) {
    System.out.printf(PLAYBACK_FORMAT, "SYNC", "\"" + String.valueOf(state.get()) + "\"");
  }

  private static void renderMutationPlayback(final Mutation mutation, final State state) {
    System.out.printf(PLAYBACK_FORMAT, mutation, "\"" + String.valueOf(state.get()) + "\"");
  }

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

  protected abstract OTPeer createPeerFor(OTEngine local, OTEngine remote);

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

  @SuppressWarnings("unchecked")
  private String replayLogAndReturnResult(final String name,
                                          final State state,
                                          final int revision,
                                          final TransactionLog log) {

    renderPlaybackHeader(name, revision);
    renderInitialStatePlayback(state);

    for (final OTOperation operation : log.getCanonLog()) {
      for (final Mutation mutation : operation.getMutations()) {
        mutation.apply(state);
        renderMutationPlayback(mutation, state);
      }
    }

    System.out.println("RESULTING HASH: " + state.getHash());
    System.out.println("\n");

    return (String) state.get();
  }

  protected void stopServerEngineAndWait() {
    serverEngine.stop(true);
  }

  @Before
  public void setUp() throws Exception {
    OTLogFormat.printLogTitle();
  }

  @After
  public void tearDown() throws Exception {
    System.out.println("===================================================");
  }
}
