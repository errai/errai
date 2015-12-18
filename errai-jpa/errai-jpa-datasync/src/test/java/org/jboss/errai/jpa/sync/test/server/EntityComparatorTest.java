/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.jpa.sync.test.server;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;

import org.jboss.errai.jpa.sync.client.shared.EntityComparator;
import org.jboss.errai.jpa.sync.server.JavaReflectionAttributeAccessor;
import org.jboss.errai.jpa.sync.test.client.entity.ChildEntity;
import org.jboss.errai.jpa.sync.test.client.entity.ParentEntity;
import org.jboss.errai.jpa.sync.test.client.entity.Zentity;
import org.junit.Before;
import org.junit.Test;

public class EntityComparatorTest extends AbstractServerSideDataSyncTest {

  EntityComparator comparator;

  @Before
  public void setupComparator() {
    comparator = new EntityComparator(em.getMetamodel(), new JavaReflectionAttributeAccessor());
  }

  @Test
  public void testPrimitiveBooleanDiffers() throws Exception {
    Zentity lhs = new Zentity();
    Zentity rhs = new Zentity();

    lhs.setPrimitiveBool(true);
    rhs.setPrimitiveBool(false);

    assertTrue(comparator.isDifferent(lhs, rhs));
  }

  @Test
  public void testBoxedIntegerDiffers() throws Exception {
    Zentity lhs = new Zentity();
    Zentity rhs = new Zentity();

    lhs.setBoxedInt(1234);
    rhs.setBoxedInt(4321);

    assertTrue(comparator.isDifferent(lhs, rhs));
  }

  @Test
  public void testBoxedIntegerDiffersLhsNull() throws Exception {
    Zentity lhs = new Zentity();
    Zentity rhs = new Zentity();

    lhs.setBoxedInt(null);
    rhs.setBoxedInt(4321);

    assertTrue(comparator.isDifferent(lhs, rhs));
  }

  @Test
  public void testBoxedIntegerDiffersRhsNull() throws Exception {
    Zentity lhs = new Zentity();
    Zentity rhs = new Zentity();

    lhs.setBoxedInt(4433);
    rhs.setBoxedInt(null);

    assertTrue(comparator.isDifferent(lhs, rhs));
  }

  @Test
  public void testTimestampDiffers() throws Exception {
    Zentity lhs = new Zentity();
    Zentity rhs = new Zentity();

    lhs.setSqlTimestamp(new Timestamp(123123123L));
    rhs.setSqlTimestamp(new Timestamp(123123124L));

    assertTrue(comparator.isDifferent(lhs, rhs));
  }

  @Test
  public void testTimestampDiffersLhsNull() throws Exception {
    Zentity lhs = new Zentity();
    Zentity rhs = new Zentity();

    lhs.setSqlTimestamp(null);
    rhs.setSqlTimestamp(new Timestamp(123123124L));

    assertTrue(comparator.isDifferent(lhs, rhs));
  }

  @Test
  public void testTimestampDiffersRhsNull() throws Exception {
    Zentity lhs = new Zentity();
    Zentity rhs = new Zentity();

    lhs.setSqlTimestamp(new Timestamp(123123123L));
    rhs.setSqlTimestamp(null);

    assertTrue(comparator.isDifferent(lhs, rhs));
  }

  @Test
  public void testCompareNestedEntitiesBothSame() throws Exception {
    ParentEntity lhs = new ParentEntity();
    ParentEntity rhs = new ParentEntity();

    lhs.getChildren().add(new ChildEntity("a string", 43));
    lhs.getChildren().add(new ChildEntity("another string", 42));

    rhs.getChildren().add(new ChildEntity("a string", 43));
    rhs.getChildren().add(new ChildEntity("another string", 42));

    assertFalse(comparator.isDifferent(lhs, rhs));
  }

  @Test
  public void testCompareNestedEntitiesChildCountsEqualAttributesDiffer() throws Exception {
    ParentEntity lhs = new ParentEntity();
    ParentEntity rhs = new ParentEntity();

    lhs.addChild(new ChildEntity("a string", 43));
    lhs.addChild(new ChildEntity("another string", 42));

    rhs.addChild(new ChildEntity("a different string", 43));
    rhs.addChild(new ChildEntity("another string", 42));

    assertTrue(comparator.isDifferent(lhs, rhs));
  }

  @Test
  public void testCompareNestedEntitiesChildCountsDiffer() throws Exception {
    ParentEntity lhs = new ParentEntity();
    ParentEntity rhs = new ParentEntity();

    lhs.getChildren().add(new ChildEntity("a string", 43));
    lhs.getChildren().add(new ChildEntity("another string", 42));

    rhs.getChildren().add(new ChildEntity("a string", 43));

    assertTrue(comparator.isDifferent(lhs, rhs));
  }

  @Test
  public void testNullLhs() throws Exception {
    Zentity lhs = null;
    Zentity rhs = new Zentity();

    assertTrue(comparator.isDifferent(lhs, rhs));
  }

  @Test
  public void testNullRhs() throws Exception {
    Zentity lhs = new Zentity();
    Zentity rhs = null;

    assertTrue(comparator.isDifferent(lhs, rhs));
  }

  @Test
  public void testNullNull() throws Exception {
    Zentity lhs = null;
    Zentity rhs = null;

    assertFalse(comparator.isDifferent(lhs, rhs));
  }

}
