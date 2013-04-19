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
import static junit.framework.Assert.assertTrue;

import org.jboss.errai.otec.mutation.CharacterData;
import org.jboss.errai.otec.mutation.IndexPosition;
import org.jboss.errai.otec.mutation.Mutation;
import org.jboss.errai.otec.mutation.MutationType;
import org.jboss.errai.otec.operation.OTOperation;
import org.jboss.errai.otec.operation.OTOperationsFactory;
import org.jboss.errai.otec.util.OTLogFormat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Mike Brock
 * @author Christian Sadilek <csadilek@redhat.com>
 */

public class OtecPrototypingTest {
  OTEngine clientEngineA;
  OTEngine clientEngineB;
  OTEngine serverEngine;

  OTEntity serverEntity;

  @Test
  public void testApplyLocalOperation() {

    setupEngines("Hello, World?");

    final OTOperationsFactory operationsFactory = serverEngine.getOperationsFactory();
    final OTOperation op = operationsFactory.createOperation(serverEntity)
        .add(MutationType.Delete, IndexPosition.of(12))
        .add(MutationType.Insert, IndexPosition.of(12), CharacterData.of('!'))
        .build();

    assertTrue(serverEntity.getTransactionLog().getLog().isEmpty());

    serverEngine.notifyOperation(op);

    assertEquals(1, serverEntity.getTransactionLog().getLog().size());
    final boolean contains = serverEntity.getTransactionLog().getLog().contains(op);
    assertTrue(contains);
    assertEquals("Hello, World!", serverEntity.getState().get());
  }

  private void suspendEngines() {
    clientEngineA.setEngineMode(OTEngineMode.Offline);
    clientEngineB.setEngineMode(OTEngineMode.Offline);
    serverEngine.setEngineMode(OTEngineMode.Offline);
  }

  private void resumeEnginesAB() {
    serverEngine.setEngineMode(OTEngineMode.Online);
    clientEngineA.setEngineMode(OTEngineMode.Online);
    clientEngineB.setEngineMode(OTEngineMode.Online);
  }

  private void resumeEnginesBA() {
    serverEngine.setEngineMode(OTEngineMode.Online);
    clientEngineB.setEngineMode(OTEngineMode.Online);
    clientEngineA.setEngineMode(OTEngineMode.Online);
  }

  @Test
  public void testNotifyRemoteOperation() {
    final String initialState = "Hello, World?";
    setupEngines(initialState);

    final OTOperationsFactory operationsFactory = clientEngineA.getOperationsFactory();
    final OTEntity clientAEntity = clientEngineA.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTOperation op = operationsFactory.createOperation(clientAEntity)
        .add(MutationType.Delete, IndexPosition.of(12))
        .add(MutationType.Insert, IndexPosition.of(12), CharacterData.of('!'))
        .build();

    clientEngineA.notifyOperation(op);

    assertEquals(1, serverEntity.getTransactionLog().getLog().size());
    assertTrue(serverEntity.getTransactionLog().getLog().contains(op));
    final String expected = "Hello, World!";
    assertEquals(expected, serverEntity.getState().get());

    final OTEntity clientBEntity = clientEngineB.getEntityStateSpace().getEntity(serverEntity.getId());
    assertEquals(1, clientBEntity.getTransactionLog().getLog().size());
    final boolean contains = clientBEntity.getTransactionLog().getLog().contains(op);
    assertTrue(contains);
    assertEquals(expected, clientBEntity.getState().get());

    assertAllLogsConsistent(expected, initialState);
  }

  /**
   * http://en.wikipedia.org/wiki/File:Basicot.png
   */
  @Test
  public void testWikipediaExampleXab() {
    final String initialState = "abc";
    setupEngines(initialState);

    final OTOperationsFactory operationsFactory = clientEngineA.getOperationsFactory();
    final OTEntity clientAEntity = clientEngineA.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTOperation insX = operationsFactory.createOperation(clientAEntity)
        .add(MutationType.Insert, IndexPosition.of(0), CharacterData.of('x'))
        .build();

    final OTOperationsFactory serverOperationsFactory = serverEngine.getOperationsFactory();
    final OTOperation delC = serverOperationsFactory.createOperation(serverEntity)
        .add(MutationType.Delete, IndexPosition.of(2), CharacterData.of('c'))
        .build();

    suspendEngines();

    clientEngineA.notifyOperation(insX);
    serverEngine.notifyOperation(delC);

    resumeEnginesAB();

    assertEquals(2, serverEntity.getTransactionLog().getLog().size());
    final String expectedState = "xab";
    assertEquals(expectedState, serverEntity.getState().get());

    assertEquals(2, clientAEntity.getTransactionLog().getLog().size());
    assertEquals(expectedState, clientAEntity.getState().get());

    final OTEntity clientBEntity = clientEngineB.getEntityStateSpace().getEntity(serverEntity.getId());
    assertEquals(2, clientBEntity.getTransactionLog().getLog().size());
    assertEquals(expectedState, clientBEntity.getState().get());

    assertAllLogsConsistent(expectedState, initialState);
  }

  @Test
  public void testWikipediaExampleBcx() {
    final String initialState = "abc";
    setupEngines(initialState);

    final OTOperationsFactory operationsFactory = clientEngineA.getOperationsFactory();
    final OTEntity clientAEntity = clientEngineA.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTOperation insX = operationsFactory.createOperation(clientAEntity)
        .add(MutationType.Insert, IndexPosition.of(3), CharacterData.of('x'))
        .build();

    final OTOperationsFactory serverOperationsFactory = serverEngine.getOperationsFactory();
    final OTOperation delA = serverOperationsFactory.createOperation(serverEntity)
        .add(MutationType.Delete, IndexPosition.of(0))
        .build();

    suspendEngines();

    clientEngineA.notifyOperation(insX);
    serverEngine.notifyOperation(delA);

    resumeEnginesAB();

    assertEquals(2, serverEntity.getTransactionLog().getLog().size());
    final String expectedState = "bcx";
    assertEquals(expectedState, serverEntity.getState().get());

    assertEquals(2, clientAEntity.getTransactionLog().getLog().size());
    assertEquals(expectedState, clientAEntity.getState().get());

    final OTEntity clientBEntity = clientEngineB.getEntityStateSpace().getEntity(serverEntity.getId());
    assertEquals(2, clientBEntity.getTransactionLog().getLog().size());
    assertEquals(expectedState, clientBEntity.getState().get());

    assertAllLogsConsistent(expectedState, initialState);
  }

  @Test
  public void testConflictingInserts() {
    final String initialState = "go";
    setupEngines(initialState);

    final OTOperationsFactory opFactoryClientA = clientEngineA.getOperationsFactory();
    final OTEntity clientAEntity = clientEngineA.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTOperation insA = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, IndexPosition.of(2), CharacterData.of('a'))
        .build();

    final OTOperationsFactory opFactoryClientB = clientEngineB.getOperationsFactory();
    final OTEntity clientBEntity = clientEngineB.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTOperation insT = opFactoryClientB.createOperation(clientBEntity)
        .add(MutationType.Insert, IndexPosition.of(2), CharacterData.of('t'))
        .build();

    suspendEngines();

    clientEngineA.notifyOperation(insA);
    clientEngineB.notifyOperation(insT);

    resumeEnginesAB();

    assertEquals(2, serverEntity.getTransactionLog().getLog().size());
    final String expectedState = "goat";
    assertEquals(expectedState, serverEntity.getState().get());

    assertEquals(2, clientAEntity.getTransactionLog().getLog().size());
    assertEquals(expectedState, clientAEntity.getState().get());

    assertEquals(2, clientBEntity.getTransactionLog().getLog().size());
    assertEquals(expectedState, clientBEntity.getState().get());

    assertAllLogsConsistent(expectedState, initialState);
  }

  @Test
  public void testConflictingDeleteAndInsert() {
    final String initialState = "goa";
    setupEngines(initialState);

    final OTOperationsFactory opFactoryClientA = clientEngineA.getOperationsFactory();
    final OTEntity clientAEntity = clientEngineA.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTOperation delA = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Delete, IndexPosition.of(2))
        .build();

    final OTOperationsFactory opFactoryClientB = clientEngineB.getOperationsFactory();
    final OTEntity clientBEntity = clientEngineB.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTOperation insT = opFactoryClientB.createOperation(clientBEntity)
        .add(MutationType.Insert, IndexPosition.of(2), CharacterData.of('t'))
        .build();

    suspendEngines();

    clientEngineA.notifyOperation(delA);
    clientEngineB.notifyOperation(insT);

    resumeEnginesAB();

    assertEquals(2, serverEntity.getTransactionLog().getLog().size());
    final String expectedState = "got";
    assertEquals(expectedState, serverEntity.getState().get());

    assertEquals(2, clientAEntity.getTransactionLog().getLog().size());
    assertEquals(expectedState, clientAEntity.getState().get());

    assertEquals(2, clientBEntity.getTransactionLog().getLog().size());
    assertEquals(expectedState, clientBEntity.getState().get());

    assertAllLogsConsistent(expectedState, initialState);
  }

  @Test
  public void testConflictingInsertAndDelete() {
    final String initialState = "goa";
    setupEngines(initialState);

    final OTOperationsFactory opFactoryClientA = clientEngineA.getOperationsFactory();
    final OTEntity clientAEntity = clientEngineA.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTOperation insT = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, IndexPosition.of(2), CharacterData.of('t'))
        .build();

    final OTOperationsFactory opFactoryClientB = clientEngineB.getOperationsFactory();
    final OTEntity clientBEntity = clientEngineB.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTOperation delA = opFactoryClientB.createOperation(clientBEntity)
        .add(MutationType.Delete, IndexPosition.of(2))
        .build();

    suspendEngines();

    clientEngineA.notifyOperation(insT);
    clientEngineB.notifyOperation(delA);

    resumeEnginesAB();

    assertEquals(2, serverEntity.getTransactionLog().getLog().size());
    final String expectedState = "got";
    assertEquals(expectedState, serverEntity.getState().get());

    assertEquals(2, clientAEntity.getTransactionLog().getLog().size());
    assertEquals(expectedState, clientAEntity.getState().get());

    assertEquals(2, clientBEntity.getTransactionLog().getLog().size());
    assertEquals(expectedState, clientBEntity.getState().get());

    assertAllLogsConsistent(expectedState, initialState);
  }

  @Test
  public void testConflictingDeletes() {
    final String initialState = "goat";
    setupEngines(initialState);

    final OTOperationsFactory opFactoryClientA = clientEngineA.getOperationsFactory();
    final OTEntity clientAEntity = clientEngineA.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTOperation delA1 = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Delete, IndexPosition.of(2))
        .build();

    final OTOperationsFactory opFactoryClientB = clientEngineB.getOperationsFactory();
    final OTEntity clientBEntity = clientEngineB.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTOperation delA2 = opFactoryClientB.createOperation(clientBEntity)
        .add(MutationType.Delete, IndexPosition.of(2))
        .build();

    suspendEngines();

    clientEngineA.notifyOperation(delA1);
    clientEngineB.notifyOperation(delA2);

    resumeEnginesAB();

    assertEquals(1, serverEntity.getTransactionLog().getLog().size());
    final String expectedState = "got";
    assertEquals(expectedState, serverEntity.getState().get());

    assertEquals(1, clientAEntity.getTransactionLog().getLog().size());
    assertEquals(expectedState, clientAEntity.getState().get());

    assertEquals(1, clientBEntity.getTransactionLog().getLog().size());
    assertEquals(expectedState, clientBEntity.getState().get());

    assertAllLogsConsistent(expectedState, initialState);
  }

  @Test
  public void testNonConflictingOperationsOfDifferentSize() {
    final String initialState = "goo";
    setupEngines(initialState);

    final OTOperationsFactory opFactoryClientA = clientEngineA.getOperationsFactory();
    final OTEntity clientAEntity = clientEngineA.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTOperation insAT = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, IndexPosition.of(3), CharacterData.of('a'))
        .add(MutationType.Insert, IndexPosition.of(4), CharacterData.of('t'))
        .build();

    final OTOperationsFactory opFactoryClientB = clientEngineB.getOperationsFactory();
    final OTEntity clientBEntity = clientEngineB.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTOperation insO = opFactoryClientB.createOperation(clientBEntity)
        .add(MutationType.Delete, IndexPosition.of(1))
        .build();

    suspendEngines();

    clientEngineA.notifyOperation(insAT);
    clientEngineB.notifyOperation(insO);

    resumeEnginesAB();

    assertEquals(2, serverEntity.getTransactionLog().getLog().size());
    final String expectedState = "goat";
    assertEquals(expectedState, serverEntity.getState().get());

    assertEquals(2, clientAEntity.getTransactionLog().getLog().size());
    assertEquals(expectedState, clientAEntity.getState().get());

    assertEquals(2, clientBEntity.getTransactionLog().getLog().size());
    assertEquals(expectedState, clientBEntity.getState().get());

    assertAllLogsConsistent(expectedState, initialState);
  }

  @Test
  public void testNonConflictingOperationsOfDifferentSizeInvertedOrder() {
    final String initialState = "goo";
    setupEngines(initialState);

    final OTOperationsFactory opFactoryClientA = clientEngineA.getOperationsFactory();
    final OTEntity clientAEntity = clientEngineA.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTOperation insAT = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, IndexPosition.of(3), CharacterData.of('a'))
        .add(MutationType.Insert, IndexPosition.of(4), CharacterData.of('t'))
        .build();

    final OTOperationsFactory opFactoryClientB = clientEngineB.getOperationsFactory();
    final OTEntity clientBEntity = clientEngineB.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTOperation insO = opFactoryClientB.createOperation(clientBEntity)
        .add(MutationType.Delete, IndexPosition.of(1))
        .build();

    suspendEngines();

    clientEngineA.notifyOperation(insAT);
    clientEngineB.notifyOperation(insO);

    resumeEnginesBA();

    assertEquals(2, serverEntity.getTransactionLog().getLog().size());
    final String expectedState = "goat";
    assertEquals(expectedState, serverEntity.getState().get());

    assertEquals(2, clientAEntity.getTransactionLog().getLog().size());
    assertEquals(expectedState, clientAEntity.getState().get());

    assertEquals(2, clientBEntity.getTransactionLog().getLog().size());
    assertEquals(expectedState, clientBEntity.getState().get());

    assertAllLogsConsistent(expectedState, initialState);
  }

  @Test
  public void testCompoundTransformWithMultipleInsertsVsOneDelete() {
    final String initialState = "go";
    setupEngines(initialState);

    final OTOperationsFactory opFactoryClientA = clientEngineA.getOperationsFactory();
    final OTEntity clientAEntity = clientEngineA.getEntityStateSpace().getEntity(serverEntity.getId());

    final OTOperation ins1 = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, IndexPosition.of(1), CharacterData.of('1'))
        .build();

    final OTOperation ins2 = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, IndexPosition.of(2), CharacterData.of('2'))
        .build();

    final OTOperation ins3 = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, IndexPosition.of(3), CharacterData.of('3'))
        .build();

    final OTOperationsFactory opFactoryClientB = clientEngineB.getOperationsFactory();
    final OTEntity clientBEntity = clientEngineB.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTOperation delG = opFactoryClientB.createOperation(clientBEntity)
        .add(MutationType.Delete, IndexPosition.of(0))
        .build();

    suspendEngines();

    clientEngineA.notifyOperation(ins1);
    clientEngineA.notifyOperation(ins2);
    clientEngineA.notifyOperation(ins3);
    clientEngineB.notifyOperation(delG);

    resumeEnginesAB();

    assertEquals(4, serverEntity.getTransactionLog().getCanonLog().size());
    final String expectedState = "123o";
    assertEquals(expectedState, serverEntity.getState().get());

    assertEquals(4, clientAEntity.getTransactionLog().getCanonLog().size());
    assertEquals(expectedState, clientAEntity.getState().get());

    assertEquals(4, clientBEntity.getTransactionLog().getCanonLog().size());
    assertEquals(expectedState, clientBEntity.getState().get());

    assertAllLogsConsistent(expectedState, initialState);
  }

  @Test
  public void testCompoundTransformWithMultipleInsertsVsOneDeleteInvertedOrder() {
    final String initialState = "go";
    setupEngines(initialState);

    final OTOperationsFactory opFactoryClientA = clientEngineA.getOperationsFactory();
    final OTEntity clientAEntity = clientEngineA.getEntityStateSpace().getEntity(serverEntity.getId());

    final OTOperation ins1 = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, IndexPosition.of(1), CharacterData.of('1'))
        .build();

    final OTOperation ins2 = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, IndexPosition.of(2), CharacterData.of('2'))
        .build();

    final OTOperation ins3 = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, IndexPosition.of(3), CharacterData.of('3'))
        .build();

    final OTOperationsFactory opFactoryClientB = clientEngineB.getOperationsFactory();
    final OTEntity clientBEntity = clientEngineB.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTOperation delG = opFactoryClientB.createOperation(clientBEntity)
        .add(MutationType.Delete, IndexPosition.of(0))
        .build();

    suspendEngines();

    clientEngineA.notifyOperation(ins1);
    clientEngineA.notifyOperation(ins2);
    clientEngineA.notifyOperation(ins3);
    clientEngineB.notifyOperation(delG);

    resumeEnginesBA();

    assertEquals(4, serverEntity.getTransactionLog().getCanonLog().size());
    final String expectedState = "123o";
    assertEquals(expectedState, serverEntity.getState().get());

    assertEquals(4, clientAEntity.getTransactionLog().getCanonLog().size());
    assertEquals(expectedState, clientAEntity.getState().get());

    assertEquals(4, clientBEntity.getTransactionLog().getCanonLog().size());
    assertEquals(expectedState, clientBEntity.getState().get());

    assertAllLogsConsistent(expectedState, initialState);
  }

  @Test
  public void testCompoundTransformWithMultipleInsertsVsOneInsert() {
    final String initialState = "go";
    setupEngines(initialState);

    final OTOperationsFactory opFactoryClientA = clientEngineA.getOperationsFactory();
    final OTEntity clientAEntity = clientEngineA.getEntityStateSpace().getEntity(serverEntity.getId());

    final OTOperation ins1 = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, IndexPosition.of(1), CharacterData.of('1'))
        .build();

    final OTOperation ins2 = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, IndexPosition.of(2), CharacterData.of('2'))
        .build();

    final OTOperation ins3 = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, IndexPosition.of(3), CharacterData.of('3'))
        .build();

    final OTOperationsFactory opFactoryClientB = clientEngineB.getOperationsFactory();
    final OTEntity clientBEntity = clientEngineB.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTOperation insAT = opFactoryClientB.createOperation(clientBEntity)
        .add(MutationType.Insert, IndexPosition.of(2), CharacterData.of('a'))
        .add(MutationType.Insert, IndexPosition.of(3), CharacterData.of('t'))
        .build();

    suspendEngines();
    clientEngineA.notifyOperation(ins1);
    clientEngineA.notifyOperation(ins2);
    clientEngineA.notifyOperation(ins3);
    clientEngineB.notifyOperation(insAT);
    resumeEnginesAB();

    assertEquals(4, serverEntity.getTransactionLog().getCanonLog().size());
    final String expectedState = "g123oat";
    assertEquals(expectedState, serverEntity.getState().get());

    assertEquals(4, clientAEntity.getTransactionLog().getCanonLog().size());
    assertEquals(expectedState, clientAEntity.getState().get());

    assertEquals(4, clientBEntity.getTransactionLog().getCanonLog().size());
    assertEquals(expectedState, clientBEntity.getState().get());

    assertAllLogsConsistent(expectedState, initialState);
  }

  @Test
  public void testCompoundTransformWithMultipleInsertsVsOneInsertInvertedOrder() {
    final String initialState = "go";
    setupEngines(initialState);

    final OTOperationsFactory opFactoryClientA = clientEngineA.getOperationsFactory();
    final OTEntity clientAEntity = clientEngineA.getEntityStateSpace().getEntity(serverEntity.getId());

    final OTOperation ins1 = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, IndexPosition.of(1), CharacterData.of('1'))
        .build();

    final OTOperation ins2 = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, IndexPosition.of(2), CharacterData.of('2'))
        .build();

    final OTOperation ins3 = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, IndexPosition.of(3), CharacterData.of('3'))
        .build();

    final OTOperationsFactory opFactoryClientB = clientEngineB.getOperationsFactory();
    final OTEntity clientBEntity = clientEngineB.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTOperation insAT = opFactoryClientB.createOperation(clientBEntity)
        .add(MutationType.Insert, IndexPosition.of(2), CharacterData.of('a'))
        .add(MutationType.Insert, IndexPosition.of(3), CharacterData.of('t'))
        .build();

    suspendEngines();
    clientEngineA.notifyOperation(ins1);
    clientEngineA.notifyOperation(ins2);
    clientEngineA.notifyOperation(ins3);
    clientEngineB.notifyOperation(insAT);
    resumeEnginesBA();

    assertEquals(4, serverEntity.getTransactionLog().getCanonLog().size());
    final String expectedState = "g123oat";
    assertEquals(expectedState, serverEntity.getState().get());

    assertEquals(4, clientAEntity.getTransactionLog().getCanonLog().size());
    assertEquals(expectedState, clientAEntity.getState().get());

    assertEquals(4, clientBEntity.getTransactionLog().getCanonLog().size());
    assertEquals(expectedState, clientBEntity.getState().get());

    assertAllLogsConsistent(expectedState, initialState);
  }

  private void setupEngines(final String initialState) {
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

  private void assertAllLogsConsistent(final String expectedResult, final String initialState) {
    System.out.println("\n----[log replay]-----");

    final State clientAState = new StringState(initialState);
    final State clientBState = new StringState(initialState);
    final State serverState = new StringState(initialState);

    final TransactionLog transactionLogA =
        clientEngineA.getEntityStateSpace().getEntity(serverEntity.getId()).getTransactionLog();
    final TransactionLog transactionLogB =
        clientEngineB.getEntityStateSpace().getEntity(serverEntity.getId()).getTransactionLog();
    final TransactionLog serverLog = serverEntity.getTransactionLog();

    final String valueA = replayLogAndReturnResult(clientAState, transactionLogA);
    final String valueB = replayLogAndReturnResult(clientBState, transactionLogB);
    final String valueServer = replayLogAndReturnResult(serverState, serverLog);

    System.out.println("Client A: " + transactionLogA);
    System.out.println("Client B: " + transactionLogB);
    System.out.println("Server  : " + serverLog);

    assertEquals(expectedResult, valueA);
    assertEquals(expectedResult, valueB);
    assertEquals(expectedResult, valueServer);

    System.out.println("----[end]------");
  }

  @SuppressWarnings("unchecked")
  private String replayLogAndReturnResult(final State state, final TransactionLog log) {
    for (final OTOperation operation : log.getCanonLog()) {
      for (final Mutation mutation : operation.getMutations()) {
        mutation.apply(state);
      }
    }

    return (String) state.get();
  }

  @Before
  public void setUp() throws Exception {
    OTLogFormat.printLogTitle();
  }

  @After
  public void tearDown() throws Exception {
    System.out.println("\n==========================\n");
  }
}
