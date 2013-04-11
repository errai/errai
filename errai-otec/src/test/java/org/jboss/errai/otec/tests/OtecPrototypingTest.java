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

import org.jboss.errai.otec.mutation.*;
import org.jboss.errai.otec.mutation.IndexPosition;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Mike Brock
 */

public class OtecPrototypingTest {
  OTEngine clientEngineA;
  OTEngine clientEngineB;
  OTEngine serverEngine;

  OTEntity serverEntity;

  @Before
  public void setUp() throws Exception {
    clientEngineA = OTEngineImpl.createEngineWithSinglePeer();
    clientEngineB = OTEngineImpl.createEngineWithSinglePeer();
    serverEngine = OTEngineImpl.createEngineWithMultiplePeers();

    clientEngineA.registerPeer(new MockPeerImpl(clientEngineA, serverEngine));
    clientEngineB.registerPeer(new MockPeerImpl(clientEngineB, serverEngine));
    serverEngine.registerPeer(new MockPeerImpl(serverEngine, clientEngineA));
    serverEngine.registerPeer(new MockPeerImpl(serverEngine, clientEngineB));

    final String myEntityOfStringFun = "Hello, World?";
    final StringState state = new StringState(myEntityOfStringFun);
    serverEntity = serverEngine.getEntityStateSpace().addEntity(state);

    clientEngineA.syncRemoteEntity(serverEngine.getId(), serverEntity.getId(), new MockEntitySyncCompletionCallback());
    clientEngineB.syncRemoteEntity(serverEngine.getId(), serverEntity.getId(), new MockEntitySyncCompletionCallback());
  }

  @Test
  public void testApplyOperationLocally() {
    final OTOperationsFactory operationsFactory = serverEngine.getOperationsFactory();
    final OTOperationImpl op = operationsFactory.createOperation(serverEntity)
        .add(MutationType.Delete, IndexPosition.of(12))
        .add(MutationType.Insert, IndexPosition.of(12), CharacterData.of('!'))
        .build();

    assertTrue(serverEntity.getTransactionLog().getLog().isEmpty());

    serverEngine.applyOperationLocally(op);

    assertEquals(1, serverEntity.getTransactionLog().getLog().size());
    assertTrue(serverEntity.getTransactionLog().getLog().contains(op));
    assertEquals("Hello, World!", serverEntity.getState().get());
  }

  @Test
  public void testNotifyOperation() {
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
}
