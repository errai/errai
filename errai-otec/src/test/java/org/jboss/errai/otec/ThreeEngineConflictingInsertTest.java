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

import org.jboss.errai.otec.client.OTEngine;
import org.jboss.errai.otec.client.OTEntity;
import org.jboss.errai.otec.client.OTPeer;
import org.jboss.errai.otec.client.mutation.MutationType;
import org.jboss.errai.otec.client.operation.OTOperation;
import org.jboss.errai.otec.client.operation.OTOperationsFactory;
import org.junit.Test;

/**
 * @author Mike Brock
 */
public class ThreeEngineConflictingInsertTest extends AbstractThreeEngineOtecTest {
  @Override
  protected OTPeer createPeerFor(final OTEngine local, final OTEngine remote) {
    return new SynchronousMockPeerlImpl(local, remote);
  }

  @Test
  public void testConflictingInserts() {
    final String initialState = "go";
    setupEngines(initialState);

    final OTOperationsFactory opFactoryClientA = clientEngineA.getOperationsFactory();
    final OTEntity clientAEntity = clientEngineA.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTOperation insA = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, 2, 'a')
        .build();

    final OTOperationsFactory opFactoryClientB = clientEngineB.getOperationsFactory();
    final OTEntity clientBEntity = clientEngineB.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTOperation insT = opFactoryClientB.createOperation(clientBEntity)
        .add(MutationType.Insert, 2, 't')
        .build();

    suspendEngines();

    clientEngineA.notifyOperation(insA);
    clientEngineB.notifyOperation(insT);

    resumeEnginesAB();
    stopServerEngineAndWait();

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
  public void testConflictingInsertsInvertedOrder() {
    final String initialState = "go";
    setupEngines(initialState);

    final OTOperationsFactory opFactoryClientA = clientEngineA.getOperationsFactory();
    final OTEntity clientAEntity = clientEngineA.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTOperation insA = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, 2, 'a')
        .build();

    final OTOperationsFactory opFactoryClientB = clientEngineB.getOperationsFactory();
    final OTEntity clientBEntity = clientEngineB.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTOperation insT = opFactoryClientB.createOperation(clientBEntity)
        .add(MutationType.Insert, 2, 't')
        .build();

    suspendEngines();

    clientEngineA.notifyOperation(insA);
    clientEngineB.notifyOperation(insT);

    resumeEnginesBA();
    stopServerEngineAndWait();

    assertEquals(2, serverEntity.getTransactionLog().getLog().size());
    final String expectedState = "gota";
    assertEquals(expectedState, serverEntity.getState().get());

    assertEquals(2, clientAEntity.getTransactionLog().getLog().size());
    assertEquals(expectedState, clientAEntity.getState().get());

    assertEquals(2, clientBEntity.getTransactionLog().getLog().size());
    assertEquals(expectedState, clientBEntity.getState().get());

    assertAllLogsConsistent(expectedState, initialState);
  }

  @Test
  public void testConflictingInsertsWithStringMutations() {
    final String initialState = "go";
    setupEngines(initialState);

    final OTOperationsFactory opFactoryClientA = clientEngineA.getOperationsFactory();
    final OTEntity clientAEntity = clientEngineA.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTOperation insA = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, 2, "aa")
        .build();

    final OTOperationsFactory opFactoryClientB = clientEngineB.getOperationsFactory();
    final OTEntity clientBEntity = clientEngineB.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTOperation insT = opFactoryClientB.createOperation(clientBEntity)
        .add(MutationType.Insert, 2, "t")
        .build();

    suspendEngines();

    clientEngineA.notifyOperation(insA);
    clientEngineB.notifyOperation(insT);

    resumeEnginesAB();
    stopServerEngineAndWait();

    assertEquals(2, serverEntity.getTransactionLog().getLog().size());
    final String expectedState = "goaat";
    assertEquals(expectedState, serverEntity.getState().get());

    assertEquals(2, clientAEntity.getTransactionLog().getLog().size());
    assertEquals(expectedState, clientAEntity.getState().get());

    assertEquals(2, clientBEntity.getTransactionLog().getLog().size());
    assertEquals(expectedState, clientBEntity.getState().get());

    assertAllLogsConsistent(expectedState, initialState);
  }

  @Test
  public void testConflictingInsertsWithStringMutationsInvertedOrder() {
    final String initialState = "go";
    setupEngines(initialState);

    final OTOperationsFactory opFactoryClientA = clientEngineA.getOperationsFactory();
    final OTEntity clientAEntity = clientEngineA.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTOperation insA = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, 2, "aa")
        .build();

    final OTOperationsFactory opFactoryClientB = clientEngineB.getOperationsFactory();
    final OTEntity clientBEntity = clientEngineB.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTOperation insT = opFactoryClientB.createOperation(clientBEntity)
        .add(MutationType.Insert, 2, "t")
        .build();

    suspendEngines();

    clientEngineA.notifyOperation(insA);
    clientEngineB.notifyOperation(insT);

    resumeEnginesBA();
    stopServerEngineAndWait();

    assertEquals(2, serverEntity.getTransactionLog().getLog().size());
    final String expectedState = "gotaa";
    assertEquals(expectedState, serverEntity.getState().get());

    assertEquals(2, clientAEntity.getTransactionLog().getLog().size());
    assertEquals(expectedState, clientAEntity.getState().get());

    assertEquals(2, clientBEntity.getTransactionLog().getLog().size());
    assertEquals(expectedState, clientBEntity.getState().get());

    assertAllLogsConsistent(expectedState, initialState);
  }
}
