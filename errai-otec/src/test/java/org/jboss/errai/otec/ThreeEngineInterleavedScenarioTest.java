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

import static org.junit.Assert.assertEquals;

import junit.framework.Assert;
import org.jboss.errai.otec.mutation.MutationType;
import org.jboss.errai.otec.operation.OTOperation;
import org.jboss.errai.otec.operation.OTOperationsFactory;
import org.junit.Test;

/**
 * @author Mike Brock
 * @author Christian Sadilek
 */
public class ThreeEngineInterleavedScenarioTest extends AbstractThreeEngineOtecTest {

  @Override
  protected OTPeer createPeerFor(OTEngine local, OTEngine remote) {
    return new SynchronousMockPeerlImpl(local, remote);
  }

  @Test
  public void testCompoundTransformWithMultipleInsertsVsOneDeleteWithStringMutations() {
    final String initialState = "ggo";
    setupEngines(initialState);

    final OTOperationsFactory opFactoryClientA = clientEngineA.getOperationsFactory();
    final OTEntity clientAEntity = clientEngineA.getEntityStateSpace().getEntity(serverEntity.getId());

    OTOperation ins1 = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, 2, "12")
        .build();

    OTOperation ins2 = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, 4, "34")
        .build();

    OTOperation ins3 = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, 6, "56")
        .build();

    final OTOperationsFactory opFactoryClientB = clientEngineB.getOperationsFactory();
    final OTEntity clientBEntity = clientEngineB.getEntityStateSpace().getEntity(serverEntity.getId());
    OTOperation delG = opFactoryClientB.createOperation(clientBEntity)
        .add(MutationType.Delete, 0, "gg")
        .build();

    ins1 = clientEngineA.applyLocally(ins1);
    ins2 = clientEngineA.applyLocally(ins2);
    ins3 = clientEngineA.applyLocally(ins3);
    delG = clientEngineB.applyLocally(delG);

    clientEngineA.notifyRemotes(ins1);
    clientEngineB.notifyRemotes(delG);
    clientEngineA.notifyRemotes(ins2);
    clientEngineA.notifyRemotes(ins3);

    stopServerEngineAndWait();

    assertEquals(4, serverEntity.getTransactionLog().getCanonLog().size());
    final String expectedState = "123456o";
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

    OTOperation ins1 = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, 1, '1')
        .build();

    OTOperation ins2 = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, 2, '2')
        .build();

    OTOperation ins3 = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, 3, '3')
        .build();

    final OTOperationsFactory opFactoryClientB = clientEngineB.getOperationsFactory();
    final OTEntity clientBEntity = clientEngineB.getEntityStateSpace().getEntity(serverEntity.getId());
    OTOperation insAT = opFactoryClientB.createOperation(clientBEntity)
        .add(MutationType.Insert, 2, 'a')
        .add(MutationType.Insert, 3, 't')
        .build();

    ins1 = clientEngineA.applyLocally(ins1);
    ins2 = clientEngineA.applyLocally(ins2);
    ins3 = clientEngineA.applyLocally(ins3);
    insAT = clientEngineB.applyLocally(insAT);

    clientEngineA.notifyRemotes(ins1);
    clientEngineA.notifyRemotes(ins2);
    clientEngineB.notifyRemotes(insAT);
    clientEngineA.notifyRemotes(ins3);

    stopServerEngineAndWait();

    Assert.assertEquals(4, serverEntity.getTransactionLog().getCanonLog().size());
    final String expectedState = "g123oat";
    Assert.assertEquals(expectedState, serverEntity.getState().get());

    Assert.assertEquals(4, clientAEntity.getTransactionLog().getCanonLog().size());
    Assert.assertEquals(expectedState, clientAEntity.getState().get());

    Assert.assertEquals(4, clientBEntity.getTransactionLog().getCanonLog().size());
    Assert.assertEquals(expectedState, clientBEntity.getState().get());

    assertAllLogsConsistent(expectedState, initialState);
  }
  
  @Test
  public void testCompoundTransformWithMultipleInsertsVsOneInsertWithConflict() {
    final String initialState = "goo";
    setupEngines(initialState);

    final OTOperationsFactory opFactoryClientA = clientEngineA.getOperationsFactory();
    final OTEntity clientAEntity = clientEngineA.getEntityStateSpace().getEntity(serverEntity.getId());

    OTOperation ins1 = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, 1, '1')
        .build();

    OTOperation ins2 = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, 2, '2')
        .build();

    OTOperation ins3 = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, 3, '3')
        .build();

    final OTOperationsFactory opFactoryClientB = clientEngineB.getOperationsFactory();
    final OTEntity clientBEntity = clientEngineB.getEntityStateSpace().getEntity(serverEntity.getId());
    OTOperation insAT = opFactoryClientB.createOperation(clientBEntity)
        .add(MutationType.Insert, 2, 'a')
        .build();

    
    ins3 = clientEngineA.applyLocally(ins3);
    ins2 = clientEngineA.applyLocally(ins2);
    ins1 = clientEngineA.applyLocally(ins1);
    insAT = clientEngineB.applyLocally(insAT);

    clientEngineA.notifyRemotes(ins3);
    clientEngineA.notifyRemotes(ins2);
    clientEngineB.notifyRemotes(insAT);
    clientEngineA.notifyRemotes(ins1);

    stopServerEngineAndWait();

    Assert.assertEquals(4, serverEntity.getTransactionLog().getCanonLog().size());
    final String expectedState = "g1o2ao3";

    Assert.assertEquals(expectedState, serverEntity.getState().get());

    Assert.assertEquals(4, clientAEntity.getTransactionLog().getCanonLog().size());
    Assert.assertEquals(expectedState, clientAEntity.getState().get());

    Assert.assertEquals(4, clientBEntity.getTransactionLog().getCanonLog().size());
    Assert.assertEquals(expectedState, clientBEntity.getState().get());

    assertAllLogsConsistent(expectedState, initialState);
  }
}
