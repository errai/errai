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

import org.jboss.errai.otec.client.OTEntity;
import org.jboss.errai.otec.client.StringState;
import org.jboss.errai.otec.client.atomizer.EntityChangeStream;
import org.jboss.errai.otec.client.atomizer.EntityChangeStreamImpl;
import org.jboss.errai.otec.client.mutation.MutationType;
import org.jboss.errai.otec.client.operation.OTOperation;
import org.junit.Test;

/**
 * Tests atomizing operations using an {@link EntityChangeStream}.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class EntityChangeStreamTest {

  @Test
  public void testInsert() {
    MockOTEngine engine = new MockOTEngine();
    OTEntity entity = engine.getEntityStateSpace().addEntity(StringState.of(""));

    EntityChangeStream ecs = new EntityChangeStreamImpl(engine, entity);
    ecs.notifyInsert(0, "A");
    ecs.notifyInsert(1, "B");
    ecs.notifyInsert(2, "C");
    ecs.flush();

    assertEquals("Expected exactly one operation", 1, engine.getNotifiedOperations().size());
    OTOperation op = engine.getNotifiedOperations().get(0);
    assertEquals("Expected exactly one mutation", 1, op.getMutations().size());
    assertEquals("Expected insert mutation", MutationType.Insert, op.getMutations().get(0).getType());
    assertEquals("Wrong mutation data", "ABC", op.getMutations().get(0).getData());
    assertEquals("Wrong mutation position", 0, op.getMutations().get(0).getPosition());
  }

  @Test
  public void testDelete() {
    MockOTEngine engine = new MockOTEngine();
    OTEntity entity = engine.getEntityStateSpace().addEntity(StringState.of("ABC"));

    EntityChangeStream ecs = new EntityChangeStreamImpl(engine, entity);
    ecs.notifyDelete(0, "A");
    ecs.notifyDelete(0, "B");
    ecs.flush();

    assertEquals("Expected exactly one operation", 1, engine.getNotifiedOperations().size());
    OTOperation op = engine.getNotifiedOperations().get(0);
    assertEquals("Expected exactly one mutation", 1, op.getMutations().size());
    assertEquals("Expected delete mutation", MutationType.Delete, op.getMutations().get(0).getType());
    assertEquals("Wrong mutation data", "AB", op.getMutations().get(0).getData());
    assertEquals("Wrong mutation position", 0, op.getMutations().get(0).getPosition());
  }


  @Test
  public void testInsertWithCursorMovingBack() {
    MockOTEngine engine = new MockOTEngine();
    OTEntity entity = engine.getEntityStateSpace().addEntity(StringState.of(""));

    EntityChangeStream ecs = new EntityChangeStreamImpl(engine, entity);
    ecs.notifyInsert(0, "A");
    ecs.notifyInsert(1, "B");
    ecs.notifyInsert(2, "D");
    ecs.notifyInsert(2, "C");
    ecs.flush();

    assertEquals("Expected exactly one operations", 1, engine.getNotifiedOperations().size());
    OTOperation opInsert = engine.getNotifiedOperations().get(0);
    assertEquals("Expected exactly one mutation", 1, opInsert.getMutations().size());
    assertEquals("Expected insert mutation", MutationType.Insert, opInsert.getMutations().get(0).getType());
    assertEquals("Wrong mutation data", "ABCD", opInsert.getMutations().get(0).getData());
    assertEquals("Wrong mutation position", 0, opInsert.getMutations().get(0).getPosition());
  }

  @Test
  public void testInsertAndDeleteWithCursorMovingBack() {
    MockOTEngine engine = new MockOTEngine();
    OTEntity entity = engine.getEntityStateSpace().addEntity(StringState.of(""));

    EntityChangeStream ecs = new EntityChangeStreamImpl(engine, entity);
    ecs.notifyInsert(0, "A");
    ecs.notifyInsert(1, "B");
    ecs.notifyInsert(2, "C");
    ecs.notifyDelete(1, "B");
    ecs.flush();

    assertEquals("Expected exactly one operations", 1, engine.getNotifiedOperations().size());
    OTOperation opInsert = engine.getNotifiedOperations().get(0);
    assertEquals("Expected exactly one mutation", 1, opInsert.getMutations().size());
    assertEquals("Expected insert mutation", MutationType.Insert, opInsert.getMutations().get(0).getType());
    assertEquals("Wrong mutation data", "AC", opInsert.getMutations().get(0).getData());
    assertEquals("Wrong mutation position", 0, opInsert.getMutations().get(0).getPosition());
  }

  @Test
  public void testDeleteAndInsert() {
    MockOTEngine engine = new MockOTEngine();
    OTEntity entity = engine.getEntityStateSpace().addEntity(StringState.of(""));

    EntityChangeStream ecs = new EntityChangeStreamImpl(engine, entity);
    ecs.notifyInsert(0, "A");
    ecs.notifyDelete(0, "A");
    ecs.notifyInsert(0, "B");
    ecs.flush();

    assertEquals("Expected exactly one operations", 1, engine.getNotifiedOperations().size());

    OTOperation opInsert = engine.getNotifiedOperations().get(0);
    assertEquals("Expected exactly one mutation", 1, opInsert.getMutations().size());
    assertEquals("Expected insert mutation", MutationType.Insert, opInsert.getMutations().get(0).getType());
    assertEquals("Wrong mutation data", "B", opInsert.getMutations().get(0).getData());
    assertEquals("Wrong mutation position", 0, opInsert.getMutations().get(0).getPosition());
  }

  @Test
  public void testNonContiguousDelete() {
    MockOTEngine engine = new MockOTEngine();
    OTEntity entity = engine.getEntityStateSpace().addEntity(StringState.of(""));

    EntityChangeStream ecs = new EntityChangeStreamImpl(engine, entity);
    ecs.notifyInsert(0, "A");
    ecs.notifyInsert(1, "B");
    ecs.notifyInsert(2, "C");
    ecs.notifyInsert(3, "D");
    ecs.notifyDelete(3, "D");
    ecs.notifyDelete(0, "A");
    ecs.flush();

    assertEquals("Expected exactly one operation", 1, engine.getNotifiedOperations().size());
    OTOperation op = engine.getNotifiedOperations().get(0);
    assertEquals("Expected exactly one mutation", 1, op.getMutations().size());
    assertEquals("Expected delete mutation", MutationType.Insert, op.getMutations().get(0).getType());
    assertEquals("Wrong mutation data", "BC", op.getMutations().get(0).getData());
    assertEquals("Wrong mutation position", 0, op.getMutations().get(0).getPosition());
  }

  @Test
  public void testAutomaticFlushByInsertOutOfRangeRight() {
    MockOTEngine engine = new MockOTEngine();
    OTEntity entity = engine.getEntityStateSpace().addEntity(StringState.of(""));

    EntityChangeStream ecs = new EntityChangeStreamImpl(engine, entity);
    ecs.notifyInsert(0, "A");
    ecs.notifyInsert(1, "B");
    ecs.notifyInsert(2, "C");
    ecs.notifyInsert(5, "C");

    assertEquals("Expected exactly one operations", 1, engine.getNotifiedOperations().size());
    OTOperation opInsert = engine.getNotifiedOperations().get(0);
    assertEquals("Expected exactly one mutation", 1, opInsert.getMutations().size());
    assertEquals("Expected insert mutation", MutationType.Insert, opInsert.getMutations().get(0).getType());
    assertEquals("Wrong mutation data", "ABC", opInsert.getMutations().get(0).getData());
    assertEquals("Wrong mutation position", 0, opInsert.getMutations().get(0).getPosition());
  }

  @Test
  public void testAutomaticFlushByInsertOutOfRangeLeft() {
    MockOTEngine engine = new MockOTEngine();
    OTEntity entity = engine.getEntityStateSpace().addEntity(StringState.of("ABCD"));

    EntityChangeStream ecs = new EntityChangeStreamImpl(engine, entity);
    ecs.notifyInsert(4, "E");
    ecs.notifyInsert(5, "F");
    ecs.notifyInsert(2, "Z");

    assertEquals("Expected exactly one operations", 1, engine.getNotifiedOperations().size());
    OTOperation opInsert = engine.getNotifiedOperations().get(0);
    assertEquals("Expected exactly one mutation", 1, opInsert.getMutations().size());
    assertEquals("Expected insert mutation", MutationType.Insert, opInsert.getMutations().get(0).getType());
    assertEquals("Wrong mutation data", "EF", opInsert.getMutations().get(0).getData());
    assertEquals("Wrong mutation position", 4, opInsert.getMutations().get(0).getPosition());
  }

  @Test
  public void testAutomaticFlushByDeleteOutOfRangeRight() {
    MockOTEngine engine = new MockOTEngine();
    OTEntity entity = engine.getEntityStateSpace().addEntity(StringState.of("ABC"));

    EntityChangeStream ecs = new EntityChangeStreamImpl(engine, entity);
    ecs.notifyDelete(1, "B");
    ecs.notifyDelete(5, "Z");

    assertEquals("Expected exactly one operations", 1, engine.getNotifiedOperations().size());
    OTOperation opDelete = engine.getNotifiedOperations().get(0);
    assertEquals("Expected exactly one mutation", 1, opDelete.getMutations().size());
    assertEquals("Expected delete mutation", MutationType.Delete, opDelete.getMutations().get(0).getType());
    assertEquals("Wrong mutation data", "B", opDelete.getMutations().get(0).getData());
    assertEquals("Wrong mutation position", 1, opDelete.getMutations().get(0).getPosition());
  }

  @Test
  public void testAutomaticFlushByDeleteOutOfRangeLeft() {
    MockOTEngine engine = new MockOTEngine();
    OTEntity entity = engine.getEntityStateSpace().addEntity(StringState.of("ABCD"));

    EntityChangeStream ecs = new EntityChangeStreamImpl(engine, entity);
    ecs.notifyDelete(2, "C");
    ecs.notifyDelete(0, "A");

    assertEquals("Expected exactly one operations", 1, engine.getNotifiedOperations().size());
    OTOperation opInsert = engine.getNotifiedOperations().get(0);
    assertEquals("Expected exactly one mutation", 1, opInsert.getMutations().size());
    assertEquals("Expected delete mutation", MutationType.Delete, opInsert.getMutations().get(0).getType());
    assertEquals("Wrong mutation data", "C", opInsert.getMutations().get(0).getData());
    assertEquals("Wrong mutation position", 2, opInsert.getMutations().get(0).getPosition());
  }


  @Test
  public void testInsertAndDeleteToStart() {
    MockOTEngine engine = new MockOTEngine();
    OTEntity entity = engine.getEntityStateSpace().addEntity(StringState.of(""));

    EntityChangeStream ecs = new EntityChangeStreamImpl(engine, entity);
    ecs.notifyInsert(0, "A");
    ecs.notifyInsert(1, "B");
    ecs.notifyInsert(2, "C");
    ecs.notifyDelete(2, "C");
    ecs.notifyDelete(1, "B");
    ecs.notifyDelete(0, "A");
    ecs.notifyInsert(0, "I");
    ecs.flush();

    assertEquals("Expected exactly one operations", 1, engine.getNotifiedOperations().size());
    OTOperation opInsert = engine.getNotifiedOperations().get(0);
    assertEquals("Expected exactly one mutation", 1, opInsert.getMutations().size());
    assertEquals("Expected delete mutation", MutationType.Insert, opInsert.getMutations().get(0).getType());
    assertEquals("Wrong mutation data", "I", opInsert.getMutations().get(0).getData());
    assertEquals("Wrong mutation position", 0, opInsert.getMutations().get(0).getPosition());
  }
}

