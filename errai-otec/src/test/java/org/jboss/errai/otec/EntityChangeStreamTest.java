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

import org.jboss.errai.otec.atomizer.EntityChangeStream;
import org.jboss.errai.otec.atomizer.EntityChangeStreamImpl;
import org.jboss.errai.otec.mutation.MutationType;
import org.jboss.errai.otec.operation.OTOperation;
import org.junit.Test;

/**
 * Tests atomizing operations using an {@link EntityChangeStream}.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class EntityChangeStreamTest {

  @Test
  public void testInsertAndFlush() {
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
  }
  
  @Test
  public void testInsertAndFlushOnCursorMovingBack() {
    MockOTEngine engine = new MockOTEngine();
    OTEntity entity = engine.getEntityStateSpace().addEntity(StringState.of(""));

    EntityChangeStream ecs = new EntityChangeStreamImpl(engine, entity);
    ecs.notifyInsert(0, "A");
    ecs.notifyInsert(1, "B");
    ecs.notifyInsert(2, "C");
    ecs.notifyInsert(2, "D");
    
    assertEquals("Expected exactly one operations", 1, engine.getNotifiedOperations().size());
    OTOperation opInsert = engine.getNotifiedOperations().get(0);
    assertEquals("Expected exactly one mutation", 1, opInsert.getMutations().size());
    assertEquals("Expected insert mutation", MutationType.Insert, opInsert.getMutations().get(0).getType());
    assertEquals("Wrong mutation data", "ABC", opInsert.getMutations().get(0).getData());
  }
  
  @Test
  public void testInsertAndFlushOnDelete() {
    MockOTEngine engine = new MockOTEngine();
    OTEntity entity = engine.getEntityStateSpace().addEntity(StringState.of(""));

    EntityChangeStream ecs = new EntityChangeStreamImpl(engine, entity);
    ecs.notifyInsert(0, "A");
    ecs.notifyInsert(1, "B");
    ecs.notifyInsert(2, "C");
    ecs.notifyDelete(1, "B");
    
    assertEquals("Expected exactly two operations", 2, engine.getNotifiedOperations().size());
    OTOperation opInsert = engine.getNotifiedOperations().get(0);
    assertEquals("Expected exactly one mutation", 1, opInsert.getMutations().size());
    assertEquals("Expected insert mutation", MutationType.Insert, opInsert.getMutations().get(0).getType());
    assertEquals("Wrong mutation data", "ABC", opInsert.getMutations().get(0).getData());
    
    OTOperation opDelete = engine.getNotifiedOperations().get(1);
    assertEquals("Expected exactly one mutation", 1, opDelete.getMutations().size());
    assertEquals("Expected insert mutation", MutationType.Delete, opDelete.getMutations().get(0).getType());
    assertEquals("Wrong mutation data", "B", opDelete.getMutations().get(0).getData());
  }
  
  @Test
  public void testDeleteAndInsertAndFlush() {
    MockOTEngine engine = new MockOTEngine();
    OTEntity entity = engine.getEntityStateSpace().addEntity(StringState.of("ABC"));

    EntityChangeStream ecs = new EntityChangeStreamImpl(engine, entity);
    ecs.notifyDelete(2, "C");
    ecs.notifyInsert(2, "C");
    ecs.flush();

    assertEquals("Expected exactly two operations", 2, engine.getNotifiedOperations().size());
    
    OTOperation opDelete = engine.getNotifiedOperations().get(0);
    assertEquals("Expected exactly one mutation", 1, opDelete.getMutations().size());
    assertEquals("Expected insert mutation", MutationType.Delete, opDelete.getMutations().get(0).getType());
    assertEquals("Wrong mutation data", "C", opDelete.getMutations().get(0).getData());
    
    OTOperation opInsert = engine.getNotifiedOperations().get(1);
    assertEquals("Expected exactly one mutation", 1, opInsert.getMutations().size());
    assertEquals("Expected insert mutation", MutationType.Insert, opInsert.getMutations().get(0).getType());
    assertEquals("Wrong mutation data", "C", opInsert.getMutations().get(0).getData());
  }
}
