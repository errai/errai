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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import org.jboss.errai.otec.CharacterData;
import org.jboss.errai.otec.IndexPosition;
import org.jboss.errai.otec.MutationType;
import org.jboss.errai.otec.OTEngine;
import org.jboss.errai.otec.OTEngineImpl;
import org.jboss.errai.otec.OTEngineMode;
import org.jboss.errai.otec.OTEntity;
import org.jboss.errai.otec.OTOperationImpl;
import org.jboss.errai.otec.OTOperationsFactory;
import org.jboss.errai.otec.StringState;
import org.junit.Test;

/**
 * @author Mike Brock
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
    final OTOperationImpl op = operationsFactory.createOperation(serverEntity)
        .add(MutationType.Delete, IndexPosition.of(12))
        .add(MutationType.Insert, IndexPosition.of(12), CharacterData.of('!'))
        .build();

    assertTrue(serverEntity.getTransactionLog().getLog().isEmpty());

    serverEngine.notifyOperation(op);

    assertEquals(1, serverEntity.getTransactionLog().getLog().size());
    assertTrue(serverEntity.getTransactionLog().getLog().contains(op));
    assertEquals("Hello, World!", serverEntity.getState().get());
  }

  @Test
  public void testNotifyRemoteOperation() {
    setupEngines("Hello, World?");

    final OTOperationsFactory operationsFactory = clientEngineA.getOperationsFactory();
    final OTEntity clientAEntity = clientEngineA.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTOperationImpl op = operationsFactory.createOperation(clientAEntity)
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
   * javadoc!
   * http://en.wikipedia.org/wiki/File:Basicot.png
   */
  @Test
  public void testWikipediaExampleXab() {
    setupEngines("abc");

    final OTOperationsFactory operationsFactory = clientEngineA.getOperationsFactory();
    final OTEntity clientAEntity = clientEngineA.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTOperationImpl insX = operationsFactory.createOperation(clientAEntity)
           .add(MutationType.Insert, IndexPosition.of(0), CharacterData.of('x'))
           .build();

    final OTOperationsFactory serverOperationsFactory = serverEngine.getOperationsFactory();
    final OTOperationImpl delC = serverOperationsFactory.createOperation(serverEntity)
               .add(MutationType.Delete, IndexPosition.of(2), CharacterData.of('c'))
               .build();

    clientEngineA.setEngineMode(OTEngineMode.Offline);
    serverEngine.setEngineMode(OTEngineMode.Offline);

    clientEngineA.notifyOperation(insX);
    serverEngine.notifyOperation(delC);

    clientEngineA.setEngineMode(OTEngineMode.Online);
    serverEngine.setEngineMode(OTEngineMode.Online);

    assertEquals(2, serverEntity.getTransactionLog().getLog().size());
    assertEquals("xab", serverEntity.getState().get());

    assertEquals(2, clientAEntity.getTransactionLog().getLog().size());
    assertEquals("xab", clientAEntity.getState().get());
  }

  private void setupEngines(String initialState) {
    clientEngineA = OTEngineImpl.createEngineWithSinglePeer();
    clientEngineB = OTEngineImpl.createEngineWithSinglePeer();
    serverEngine = OTEngineImpl.createEngineWithMultiplePeers();

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
