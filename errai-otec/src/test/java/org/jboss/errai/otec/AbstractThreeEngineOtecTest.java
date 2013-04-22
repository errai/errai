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
public class AbstractThreeEngineOtecTest {
  private static final String PLAYBACK_FORMAT = "%-30s %-40s\n";
  OTEngine clientEngineA;
  OTEngine clientEngineB;
  OTEngine serverEngine;
  OTEntity serverEntity;

  private static void renderPlaybackHeader(final String stateName) {
    System.out.println("===================================================");
    System.out.println("NODE: " + stateName);
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
    clientEngineA.setEngineMode(OTEngineMode.Offline);
    clientEngineB.setEngineMode(OTEngineMode.Offline);
    serverEngine.setEngineMode(OTEngineMode.Offline);
  }

  protected void resumeEnginesAB() {
    serverEngine.setEngineMode(OTEngineMode.Online);
    clientEngineA.setEngineMode(OTEngineMode.Online);
    clientEngineB.setEngineMode(OTEngineMode.Online);
  }

  protected void resumeEnginesBA() {
    serverEngine.setEngineMode(OTEngineMode.Online);
    clientEngineB.setEngineMode(OTEngineMode.Online);
    clientEngineA.setEngineMode(OTEngineMode.Online);
  }

  protected void setupEngines(final String initialState) {
    clientEngineA = OTEngineImpl.createEngineWithSinglePeer("ClientA");
    clientEngineB = OTEngineImpl.createEngineWithSinglePeer("ClientB");
    serverEngine = OTEngineImpl.createEngineWithMultiplePeers("Server");

    clientEngineA.registerPeer(new MockPeerImpl(clientEngineA, serverEngine));
    clientEngineB.registerPeer(new MockPeerImpl(clientEngineB, serverEngine));
    serverEngine.registerPeer(new MockPeerImpl(serverEngine, clientEngineA));
    serverEngine.registerPeer(new MockPeerImpl(serverEngine, clientEngineB));

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

    final String valueA = replayLogAndReturnResult("ClientA", clientAState, transactionLogA);
    final String valueB = replayLogAndReturnResult("ClientB", clientBState, transactionLogB);
    final String valueServer = replayLogAndReturnResult("Server", serverState, serverLog);

    assertEquals(expectedResult, valueA);
    assertEquals(expectedResult, valueB);
    assertEquals(expectedResult, valueServer);

    System.out.println("------[end]------");
  }

  @SuppressWarnings("unchecked")
  private String replayLogAndReturnResult(final String name, final State state, final TransactionLog log) {
    renderPlaybackHeader(name);
    renderInitialStatePlayback(state);

    for (final OTOperation operation : log.getCanonLog()) {
      for (final Mutation mutation : operation.getMutations()) {
        mutation.apply(state);
        renderMutationPlayback(mutation, state);
      }
    }

    System.out.println("\n");

    return (String) state.get();
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
