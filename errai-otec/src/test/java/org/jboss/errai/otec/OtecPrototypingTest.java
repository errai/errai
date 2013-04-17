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
import org.jboss.errai.otec.mutation.MutationType;
import org.jboss.errai.otec.operation.OTOperation;
import org.jboss.errai.otec.operation.OTOperationsFactory;
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
    assertTrue(serverEntity.getTransactionLog().getLog().contains(op));
    assertEquals("Hello, World!", serverEntity.getState().get());
  }

  private void suspendEngines() {
    clientEngineA.setEngineMode(OTEngineMode.Offline);
    clientEngineB.setEngineMode(OTEngineMode.Offline);
    serverEngine.setEngineMode(OTEngineMode.Offline);
  }

  private void resumeEngines() {
    serverEngine.setEngineMode(OTEngineMode.Online);
    clientEngineA.setEngineMode(OTEngineMode.Online);
    clientEngineB.setEngineMode(OTEngineMode.Online);
  }

  @Test
  public void testNotifyRemoteOperation() {
    setupEngines("Hello, World?");

    final OTOperationsFactory operationsFactory = clientEngineA.getOperationsFactory();
    final OTEntity clientAEntity = clientEngineA.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTOperation op = operationsFactory.createOperation(clientAEntity)
        .add(MutationType.Delete, IndexPosition.of(12))
        .add(MutationType.Insert, IndexPosition.of(12), CharacterData.of('!'))
        .build();

    clientEngineA.notifyOperation(op);

    assertEquals(1, serverEntity.getTransactionLog().getLog().size());
    assertTrue(serverEntity.getTransactionLog().getLog().contains(op));
    assertEquals("Hello, World!", serverEntity.getState().get());

    final OTEntity clientBEntity = clientEngineB.getEntityStateSpace().getEntity(serverEntity.getId());
    assertEquals(1, clientBEntity.getTransactionLog().getLog().size());
    assertTrue(clientBEntity.getTransactionLog().getLog().contains(op));
    assertEquals("Hello, World!", clientBEntity.getState().get());
  }

  /**
   * http://en.wikipedia.org/wiki/File:Basicot.png
   */
  @Test
  public void testWikipediaExampleXab() {
    setupEngines("abc");

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

    resumeEngines();

    assertEquals(2, serverEntity.getTransactionLog().getLog().size());
    assertEquals("xab", serverEntity.getState().get());

    assertEquals(2, clientAEntity.getTransactionLog().getLog().size());
    assertEquals("xab", clientAEntity.getState().get());

    final OTEntity clientBEntity = clientEngineB.getEntityStateSpace().getEntity(serverEntity.getId());
    assertEquals(2, clientBEntity.getTransactionLog().getLog().size());
    assertEquals("xab", clientBEntity.getState().get());
  }

  @Test
  public void testConflictingInserts() {
    setupEngines("go");

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

    resumeEngines();

    assertEquals(2, serverEntity.getTransactionLog().getLog().size());
    assertEquals("goat", serverEntity.getState().get());

    assertEquals(2, clientAEntity.getTransactionLog().getLog().size());
    assertEquals("goat", clientAEntity.getState().get());

    assertEquals(2, clientBEntity.getTransactionLog().getLog().size());
    assertEquals("goat", clientBEntity.getState().get());
  }

  @Test
  public void testConflictingInsertAndDelete() {
    setupEngines("goa");

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

    resumeEngines();

    assertEquals(2, serverEntity.getTransactionLog().getLog().size());
    assertEquals("got", serverEntity.getState().get());

    assertEquals(2, clientAEntity.getTransactionLog().getLog().size());
    assertEquals("got", clientAEntity.getState().get());

    assertEquals(2, clientBEntity.getTransactionLog().getLog().size());
    assertEquals("got", clientBEntity.getState().get());
  }

  private void setupEngines(String initialState) {
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
}
