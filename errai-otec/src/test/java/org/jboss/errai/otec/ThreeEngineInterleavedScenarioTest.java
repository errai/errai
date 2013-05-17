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

import org.jboss.errai.otec.client.OTEngine;
import org.jboss.errai.otec.client.OTEntity;
import org.jboss.errai.otec.client.OTPeer;
import org.jboss.errai.otec.client.mutation.MutationType;
import org.jboss.errai.otec.client.operation.OTOperation;
import org.jboss.errai.otec.client.operation.OTOperationsFactory;
import org.junit.Ignore;
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
  public void testCompoundTransformWithMultipleInsert() {
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

    clientEngineB.notifyRemotes(delG);
    clientEngineA.notifyRemotes(ins1);
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

  @Test
  public void testCompoundTransformWithCompleteDeleteBeforeInsert() {
    final String initialState = "The quick brown fox jumps over the lazy dog.";
    setupEngines(initialState);

    final OTOperationsFactory opFactoryClientA = clientEngineA.getOperationsFactory();
    final OTEntity clientAEntity = clientEngineA.getEntityStateSpace().getEntity(serverEntity.getId());

    OTOperation ins1 = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Delete, 0, "The quick brown fox jumps over the lazy dog.")
        .build();

    final OTOperationsFactory opFactoryClientB = clientEngineB.getOperationsFactory();
    final OTEntity clientBEntity = clientEngineB.getEntityStateSpace().getEntity(serverEntity.getId());
    OTOperation insAT = opFactoryClientB.createOperation(clientBEntity)
        .add(MutationType.Insert, 5, "Hello")
        .build();

    ins1 = clientEngineA.applyLocally(ins1);
    insAT = clientEngineB.applyLocally(insAT);

    clientEngineB.notifyRemotes(insAT);
    clientEngineA.notifyRemotes(ins1);

    stopServerEngineAndWait();

    Assert.assertEquals(2, serverEntity.getTransactionLog().getCanonLog().size());
    final String expectedState = "Hello";

    Assert.assertEquals(expectedState, serverEntity.getState().get());

    Assert.assertEquals(2, clientAEntity.getTransactionLog().getCanonLog().size());
    Assert.assertEquals(expectedState, clientAEntity.getState().get());

    Assert.assertEquals(2, clientBEntity.getTransactionLog().getCanonLog().size());
    Assert.assertEquals(expectedState, clientBEntity.getState().get());

    assertAllLogsConsistent(expectedState, initialState);
  }

  @Test
  public void testCompoundTransformWithCompleteDeleteInsertCrossingRangeEnd() {
    final String initialState = "The quick brown fox jumps over the lazy dog.";
    setupEngines(initialState);

    final OTOperationsFactory opFactoryClientA = clientEngineA.getOperationsFactory();
    final OTEntity clientAEntity = clientEngineA.getEntityStateSpace().getEntity(serverEntity.getId());

    OTOperation ins1 = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Delete, 0, "The quick brown fox jumps over the lazy dog.")
        .build();

    final OTOperationsFactory opFactoryClientB = clientEngineB.getOperationsFactory();
    final OTEntity clientBEntity = clientEngineB.getEntityStateSpace().getEntity(serverEntity.getId());
    OTOperation insAT = opFactoryClientB.createOperation(clientBEntity)
        .add(MutationType.Insert, 44, "Hello")
        .build();

    ins1 = clientEngineA.applyLocally(ins1);
    insAT = clientEngineB.applyLocally(insAT);


    clientEngineA.notifyRemotes(ins1);
    clientEngineB.notifyRemotes(insAT);

    stopServerEngineAndWait();

    Assert.assertEquals(2, serverEntity.getTransactionLog().getCanonLog().size());
    final String expectedState = "Hello";

    Assert.assertEquals(expectedState, serverEntity.getState().get());

    Assert.assertEquals(2, clientAEntity.getTransactionLog().getCanonLog().size());
    Assert.assertEquals(expectedState, clientAEntity.getState().get());

    Assert.assertEquals(2, clientBEntity.getTransactionLog().getCanonLog().size());
    Assert.assertEquals(expectedState, clientBEntity.getState().get());

    assertAllLogsConsistent(expectedState, initialState);
  }

  @Test
  public void testCompoundTransformWithCompleteDeleteInsertCrossingRangeFront() {
    final String initialState = "The quick brown fox jumps over the lazy dog.";
    setupEngines(initialState);

    final OTOperationsFactory opFactoryClientA = clientEngineA.getOperationsFactory();
    final OTEntity clientAEntity = clientEngineA.getEntityStateSpace().getEntity(serverEntity.getId());

    OTOperation delAllFromQuick = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Delete, 4, "quick brown fox jumps over the lazy dog.")
        .build();

    final OTOperationsFactory opFactoryClientB = clientEngineB.getOperationsFactory();
    final OTEntity clientBEntity = clientEngineB.getEntityStateSpace().getEntity(serverEntity.getId());
    OTOperation delTheQuick = opFactoryClientB.createOperation(clientBEntity)
        .add(MutationType.Delete, 0, "The quick")
        .build();
    OTOperation insHello = opFactoryClientB.createOperation(clientBEntity)
        .add(MutationType.Insert, 0, "Hello")
        .build();

    delAllFromQuick = clientEngineA.applyLocally(delAllFromQuick);

    delTheQuick = clientEngineB.applyLocally(delTheQuick);
    insHello = clientEngineB.applyLocally(insHello);

    clientEngineA.notifyRemotes(delAllFromQuick);
    clientEngineB.notifyRemotes(delTheQuick);
    clientEngineB.notifyRemotes(insHello);

    stopServerEngineAndWait();

    Assert.assertEquals(3, serverEntity.getTransactionLog().getCanonLog().size());
    final String expectedState = "Hello";

    Assert.assertEquals(expectedState, serverEntity.getState().get());

    Assert.assertEquals(3, clientAEntity.getTransactionLog().getCanonLog().size());
    Assert.assertEquals(expectedState, clientAEntity.getState().get());

    Assert.assertEquals(3, clientBEntity.getTransactionLog().getCanonLog().size());
    Assert.assertEquals(expectedState, clientBEntity.getState().get());

    assertAllLogsConsistent(expectedState, initialState);
  }

  @Test
  public void testVeryLongHistoryDivergence() {
    final String initialState = "";
    setupEngines(initialState);

    final OTOperationsFactory opFactoryClientA = clientEngineA.getOperationsFactory();
    final OTEntity clientAEntity = clientEngineA.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTEntity clientBEntity = clientEngineB.getEntityStateSpace().getEntity(serverEntity.getId());

    OTOperation a = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, 0, "A")
        .build();
    OTOperation b = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, 1, "B")
        .build();
    OTOperation c = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, 2, "C")
        .build();
    OTOperation d = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, 3, "D")
        .build();
    OTOperation e = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, 4, "E")
        .build();
    OTOperation f = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, 5, "F")
        .build();
    OTOperation g = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, 6, "G")
        .build();
    OTOperation h = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, 7, "H")
        .build();

    final OTOperationsFactory opFactoryClientB = clientEngineB.getOperationsFactory();
    OTOperation x = opFactoryClientB.createOperation(clientBEntity)
        .add(MutationType.Insert, 0, "X")
        .build();
    OTOperation y = opFactoryClientB.createOperation(clientBEntity)
        .add(MutationType.Insert, 1, "Y")
        .build();
    OTOperation z = opFactoryClientB.createOperation(clientBEntity)
        .add(MutationType.Insert, 2, "Z")
        .build();

    /** ClientA apply: "abc" **/
    a = clientEngineA.applyLocally(a);
    b = clientEngineA.applyLocally(b);
    c = clientEngineA.applyLocally(c);

    /** ClientA send : "abc" **/
    clientEngineA.notifyRemotes(a);
    clientEngineA.notifyRemotes(b);
    clientEngineA.notifyRemotes(c);

    /** ClientA apply: "defgd" **/
    d = clientEngineA.applyLocally(d);
    e = clientEngineA.applyLocally(e);
    f = clientEngineA.applyLocally(f);
    g = clientEngineA.applyLocally(g);
    h = clientEngineA.applyLocally(h);

    /** ClientB apply: "XY" **/
    x = clientEngineB.applyLocally(x);
    y = clientEngineB.applyLocally(y);

    /** ClientB send: "XY" **/
    clientEngineB.notifyRemotes(x);
    clientEngineB.notifyRemotes(y);

    /** ClientA send: "DEFG" **/
    clientEngineA.notifyRemotes(d);
    clientEngineA.notifyRemotes(e);
    clientEngineA.notifyRemotes(f);
    clientEngineA.notifyRemotes(g);


    /** ClientB apply: "Z" **/
    z = clientEngineB.applyLocally(z);

    /** ClientA send: "H" **/
    clientEngineA.notifyRemotes(h);

    /** ClientB send: "Z" **/
    clientEngineB.notifyRemotes(z);

    stopServerEngineAndWait();

    final String expected = "XYZABCDEFGH";

    assertAllLogsConsistent(expected, initialState);
  }

  @Test
  public void testVeryLongHistoryDivergenceWithInitialStateABCXYDZ() {
    final String initialState = "...";
    setupEngines(initialState);

    final OTOperationsFactory opFactoryClientA = clientEngineA.getOperationsFactory();
    final OTEntity clientAEntity = clientEngineA.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTEntity clientBEntity = clientEngineB.getEntityStateSpace().getEntity(serverEntity.getId());

    OTOperation a = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, 0, "A")
        .build();
    OTOperation b = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, 1, "B")
        .build();
    OTOperation c = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, 2, "C")
        .build();
    OTOperation d = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, 3, "D")
        .build();

    final OTOperationsFactory opFactoryClientB = clientEngineB.getOperationsFactory();
    OTOperation x = opFactoryClientB.createOperation(clientBEntity)
        .add(MutationType.Insert, 3, "X")
        .build();
    OTOperation y = opFactoryClientB.createOperation(clientBEntity)
        .add(MutationType.Insert, 4, "Y")
        .build();
    OTOperation z = opFactoryClientB.createOperation(clientBEntity)
        .add(MutationType.Insert, 9, "Z")
        .build();

    /** ClientA apply: "ABCD" **/
    a = clientEngineA.applyLocally(a);
    b = clientEngineA.applyLocally(b);
    c = clientEngineA.applyLocally(c);
    d = clientEngineA.applyLocally(d);

    /** ClientB apply: "XY" **/
    x = clientEngineB.applyLocally(x);
    y = clientEngineB.applyLocally(y);

    /** ClientA send : "AB" **/
    clientEngineA.notifyRemotes(a);
    clientEngineA.notifyRemotes(b);

    /** ClientA send: "C" **/
    clientEngineA.notifyRemotes(c);

    /** ClientB send: "XY" **/
    clientEngineB.notifyRemotes(x);
    clientEngineB.notifyRemotes(y);

    /** ClientA send: "D" **/
    clientEngineA.notifyRemotes(d);

    /** ClientB apply: "Z" **/
    z = clientEngineB.applyLocally(z);

    /** ClientB send: "Z" **/
    clientEngineB.notifyRemotes(z);

    stopServerEngineAndWait();

    final String expected = "ABCD...XYZ";

    assertAllLogsConsistent(expected, initialState);
  }
  
  @Test @Ignore
  public void testVeryLongHistoryDivergenceWithInitialStateABXCYDZ() {
    final String initialState = "...";
    setupEngines(initialState);

    final OTOperationsFactory opFactoryClientA = clientEngineA.getOperationsFactory();
    final OTEntity clientAEntity = clientEngineA.getEntityStateSpace().getEntity(serverEntity.getId());
    final OTEntity clientBEntity = clientEngineB.getEntityStateSpace().getEntity(serverEntity.getId());

    OTOperation a = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, 0, "A")
        .build();
    OTOperation b = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, 1, "B")
        .build();
    OTOperation c = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, 2, "C")
        .build();
    OTOperation d = opFactoryClientA.createOperation(clientAEntity)
        .add(MutationType.Insert, 3, "D")
        .build();

    final OTOperationsFactory opFactoryClientB = clientEngineB.getOperationsFactory();
    OTOperation x = opFactoryClientB.createOperation(clientBEntity)
        .add(MutationType.Insert, 3, "X")
        .build();
    OTOperation y = opFactoryClientB.createOperation(clientBEntity)
        .add(MutationType.Insert, 4, "Y")
        .build();
    OTOperation z = opFactoryClientB.createOperation(clientBEntity)
        .add(MutationType.Insert, 9, "Z")
        .build();

    /** ClientA apply: "ABCD" **/
    a = clientEngineA.applyLocally(a);
    b = clientEngineA.applyLocally(b);
    c = clientEngineA.applyLocally(c);
    d = clientEngineA.applyLocally(d);

    /** ClientB apply: "XY" **/
    x = clientEngineB.applyLocally(x);
    y = clientEngineB.applyLocally(y);

    /** ClientA send : "AB" **/
    clientEngineA.notifyRemotes(a);
    clientEngineA.notifyRemotes(b);

    /** ClientB send: "X" **/
    clientEngineB.notifyRemotes(x);
    
    /** ClientA send: "C" **/
    clientEngineA.notifyRemotes(c);
    
    /** ClientB send: "Y" **/
    clientEngineB.notifyRemotes(y);

    /** ClientA send: "D" **/
    clientEngineA.notifyRemotes(d);

    /** ClientB apply: "Z" **/
    z = clientEngineB.applyLocally(z);

    /** ClientB send: "Z" **/
    clientEngineB.notifyRemotes(z);

    stopServerEngineAndWait();

    final String expected = "ABCD...XYZ";

    assertAllLogsConsistent(expected, initialState);
  }
}
