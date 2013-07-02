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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Map.Entry;

import org.jboss.errai.otec.client.OTEngine;
import org.jboss.errai.otec.client.OTEntity;
import org.jboss.errai.otec.client.OTPeer;
import org.jboss.errai.otec.harness.NoFuzz;
import org.jboss.errai.otec.client.mutation.MutationType;
import org.jboss.errai.otec.client.operation.OTOperation;
import org.jboss.errai.otec.client.operation.OTOperationsFactory;
import org.junit.Test;

/**
 * @author Mike Brock
 * @author Christian Sadilek <csadilek@redhat.com>
 */

public abstract class ThreeEngineOtecTest extends AbstractThreeEngineOtecTest {

  protected abstract void startEnginesAndWait();

  @Override
  protected OTPeer createPeerFor(OTEngine local, OTEngine remote) {
    return new SynchronousMockPeerlImpl(local, remote);
  }

  @Test
  @NoFuzz
  public void testApplyLocalOperation() {
    setupEngines("Hello, World?");

    final OTOperationsFactory operationsFactory = serverEngine.getOperationsFactory();
    final OTOperation op = operationsFactory.createOperation(serverEntity)
        .add(MutationType.Delete, 12, "?")
        .add(MutationType.Insert, 12, '!')
        .build();

    assertTrue(serverEntity.getTransactionLog().getLog().isEmpty());

    serverEngine.notifyOperation(op);

    assertEquals(1, serverEntity.getTransactionLog().getLog().size());
    final boolean contains = serverEntity.getTransactionLog().getLog().contains(op);
    assertTrue(contains);
    assertEquals("Hello, World!", serverEntity.getState().get());
  }

  @Test
  @NoFuzz
  public void testNotifyRemoteOperation() {
    final String initialState = "Hello, World?";
    setupEngines(initialState);

    final OTOperationsFactory operationsFactory = clientEngineA.getOperationsFactory();
    final OTEntity clientAEntity = clientEngineA.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTOperation op = operationsFactory.createOperation(clientAEntity)
        .add(MutationType.Delete, 12, "?")
        .add(MutationType.Insert, 12, '!')
        .build();

    clientEngineA.notifyOperation(op);

    startEnginesAndWait();

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
  @NoFuzz
  public void testWikipediaExampleXab() {
    final String initialState = "abc";
    setupEngines(initialState);

    final OTOperationsFactory operationsFactory = clientEngineA.getOperationsFactory();
    final OTEntity clientAEntity = clientEngineA.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTOperation insX = operationsFactory.createOperation(clientAEntity)
        .add(MutationType.Insert, 0, 'x')
        .build();

    final OTOperationsFactory serverOperationsFactory = serverEngine.getOperationsFactory();
    final OTOperation delC = serverOperationsFactory.createOperation(serverEntity)
        .add(MutationType.Delete, 2, 'c')
        .build();

    suspendEngines();

    clientEngineA.notifyOperation(insX);
    serverEngine.notifyOperation(delC);

    startEnginesAndWait();

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
  @NoFuzz
  public void testWikipediaExampleBcx() {
    final String initialState = "abc";
    setupEngines(initialState);

    final OTOperationsFactory operationsFactory = clientEngineA.getOperationsFactory();
    final OTEntity clientAEntity = clientEngineA.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTOperation insX = operationsFactory.createOperation(clientAEntity)
        .add(MutationType.Insert, 3, 'x')
        .build();

    final OTOperationsFactory serverOperationsFactory = serverEngine.getOperationsFactory();
    final OTOperation delA = serverOperationsFactory.createOperation(serverEntity)
        .add(MutationType.Delete, 0, "a")
        .build();

    suspendEngines();

    clientEngineA.notifyOperation(insX);
    serverEngine.notifyOperation(delA);

    startEnginesAndWait();

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
  public void testConflictingDeleteAndInsert() {
    final String initialState = "goa";
    setupEngines(initialState);

    final OTOperationsFactory opFactoryClientA = clientEngineA.getOperationsFactory();
    final OTEntity clientAEntity = clientEngineA.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTOperation delA = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Delete, 2, "a")
        .build();

    final OTOperationsFactory opFactoryClientB = clientEngineB.getOperationsFactory();
    final OTEntity clientBEntity = clientEngineB.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTOperation insT = opFactoryClientB.createOperation(clientBEntity)
        .add(MutationType.Insert, 2, 't')
        .build();

    suspendEngines();

    clientEngineA.notifyOperation(delA);
    clientEngineB.notifyOperation(insT);

    startEnginesAndWait();

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
        .add(MutationType.Insert, 2, 't')
        .build();

    final OTOperationsFactory opFactoryClientB = clientEngineB.getOperationsFactory();
    final OTEntity clientBEntity = clientEngineB.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTOperation delA = opFactoryClientB.createOperation(clientBEntity)
        .add(MutationType.Delete, 2, "a")
        .build();

    suspendEngines();

    clientEngineA.notifyOperation(insT);
    clientEngineB.notifyOperation(delA);

    startEnginesAndWait();

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
        .add(MutationType.Delete, 2, "a")
        .build();

    final OTOperationsFactory opFactoryClientB = clientEngineB.getOperationsFactory();
    final OTEntity clientBEntity = clientEngineB.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTOperation delA2 = opFactoryClientB.createOperation(clientBEntity)
        .add(MutationType.Delete, 2, "a")
        .build();

    suspendEngines();

    clientEngineA.notifyOperation(delA1);
    clientEngineB.notifyOperation(delA2);

    startEnginesAndWait();

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
        .add(MutationType.Insert, 3, 'a')
        .add(MutationType.Insert, 4, 't')
        .build();

    final OTOperationsFactory opFactoryClientB = clientEngineB.getOperationsFactory();
    final OTEntity clientBEntity = clientEngineB.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTOperation insO = opFactoryClientB.createOperation(clientBEntity)
        .add(MutationType.Delete, 1, "o")
        .build();

    suspendEngines();

    clientEngineA.notifyOperation(insAT);
    clientEngineB.notifyOperation(insO);

    startEnginesAndWait();

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
        .add(MutationType.Insert, 1, '1')
        .build();

    final OTOperation ins2 = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, 2, '2')
        .build();

    final OTOperation ins3 = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, 3, '3')
        .build();

    final OTOperationsFactory opFactoryClientB = clientEngineB.getOperationsFactory();
    final OTEntity clientBEntity = clientEngineB.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTOperation delG = opFactoryClientB.createOperation(clientBEntity)
        .add(MutationType.Delete, 0, "g")
        .build();

    suspendEngines();

    clientEngineA.notifyOperation(ins1);
    clientEngineA.notifyOperation(ins2);
    clientEngineA.notifyOperation(ins3);
    clientEngineB.notifyOperation(delG);

    startEnginesAndWait();

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
        .add(MutationType.Insert, 1, '1')
        .build();

    final OTOperation ins2 = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, 2, '2')
        .build();

    final OTOperation ins3 = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, 3, '3')
        .build();

    final OTOperationsFactory opFactoryClientB = clientEngineB.getOperationsFactory();
    final OTEntity clientBEntity = clientEngineB.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTOperation insAT = opFactoryClientB.createOperation(clientBEntity)
        .add(MutationType.Insert, 2, 'a')
        .add(MutationType.Insert, 3, 't')
        .build();

    suspendEngines();
    clientEngineA.notifyOperation(ins1);
    clientEngineA.notifyOperation(ins2);
    clientEngineA.notifyOperation(ins3);
    clientEngineB.notifyOperation(insAT);
    startEnginesAndWait();

    assertEquals(4, serverEntity.getTransactionLog().getCanonLog().size());
    final String expectedState = "g123oat";
    assertEquals(expectedState, serverEntity.getState().get());

    assertEquals(4, clientAEntity.getTransactionLog().getCanonLog().size());
    assertEquals(expectedState, clientAEntity.getState().get());

    assertEquals(4, clientBEntity.getTransactionLog().getCanonLog().size());
    assertEquals(expectedState, clientBEntity.getState().get());

    assertAllLogsConsistent(expectedState, initialState);
  }

  /**
   * http://en.wikipedia.org/wiki/File:Basicot.png
   */
  @Test
  public void testWikipediaExampleXabUsingStringMutations() {
    final String initialState = "bbbccc";
    setupEngines(initialState);

    final OTOperationsFactory operationsFactory = clientEngineA.getOperationsFactory();
    final OTEntity clientAEntity = clientEngineA.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTOperation insX = operationsFactory.createOperation(clientAEntity)
        .add(MutationType.Insert, 0, "xa")
        .build();

    final OTOperationsFactory serverOperationsFactory = serverEngine.getOperationsFactory();
    final OTOperation delC = serverOperationsFactory.createOperation(serverEntity)
        .add(MutationType.Delete, 1, "bbccc")
        .build();

    suspendEngines();

    clientEngineA.notifyOperation(insX);
    serverEngine.notifyOperation(delC);

    startEnginesAndWait();

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
  public void testCompoundTransformWithMultipleInsertsVsOneDeleteWithStringMutations() {
    final String initialState = "ggo";
    setupEngines(initialState);

    final OTOperationsFactory opFactoryClientA = clientEngineA.getOperationsFactory();
    final OTEntity clientAEntity = clientEngineA.getEntityStateSpace().getEntity(serverEntity.getId());

    final OTOperation ins1 = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, 2, "12")
        .build();

    final OTOperation ins2 = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, 4, "34")
        .build();

    final OTOperation ins3 = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, 6, "56")
        .build();

    final OTOperationsFactory opFactoryClientB = clientEngineB.getOperationsFactory();
    final OTEntity clientBEntity = clientEngineB.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTOperation delG = opFactoryClientB.createOperation(clientBEntity)
        .add(MutationType.Delete, 0, "gg")
        .build();

    suspendEngines();

    clientEngineA.notifyOperation(ins1);
    clientEngineA.notifyOperation(ins2);
    clientEngineA.notifyOperation(ins3);
    clientEngineB.notifyOperation(delG);

    startEnginesAndWait();

    assertEquals(4, serverEntity.getTransactionLog().getCanonLog().size());
    final String expectedState = "123456o";
    assertEquals(expectedState, serverEntity.getState().get());

    assertEquals(4, clientAEntity.getTransactionLog().getCanonLog().size());
    assertEquals(expectedState, clientAEntity.getState().get());

    assertEquals(4, clientBEntity.getTransactionLog().getCanonLog().size());
    assertEquals(expectedState, clientBEntity.getState().get());

    assertAllLogsConsistent(expectedState, initialState);
  }
}
