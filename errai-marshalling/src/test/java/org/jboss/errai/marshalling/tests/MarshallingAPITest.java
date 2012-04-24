/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

package org.jboss.errai.marshalling.tests;

import java.util.HashSet;
import java.util.Set;

import org.jboss.errai.marshalling.server.MappingContextSingleton;
import org.jboss.errai.marshalling.server.ServerMarshalling;
import org.jboss.errai.marshalling.tests.res.shared.ItemWithEnum;
import org.jboss.errai.marshalling.tests.res.shared.NullBoxedNatives;
import org.jboss.errai.marshalling.tests.res.shared.Role;
import org.jboss.errai.marshalling.tests.res.shared.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Mike Brock
 */
public class MarshallingAPITest {

  @Before
  public void ensureMarshallingSystemInitialized() {
    MappingContextSingleton.get();
  }

  @SuppressWarnings("unchecked")
  private void testEncodeDecode(Object value) {
    if (value == null) return;
    Assert.assertEquals(value, ServerMarshalling.fromJSON(ServerMarshalling.toJSON(value)));
  }

  @Test
  public void testQualifiedInt() {
    testEncodeDecode(212);
  }

  @Test
  public void testQualifiedLong() {
    testEncodeDecode(3293L);
  }

  @Test
  public void testNullLong() {
    testEncodeDecode((Long) null);
  }

  @Test
  public void testQualifiedShort() {
    testEncodeDecode((short) 123);
  }

  @Test
  public void testQualifiedDouble() {
    testEncodeDecode(3232938.323d);
  }

  @Test
  public void testQualifiedFloat() {
    testEncodeDecode(32013910.32f);
  }

  @Test
  public void testQualifiedBoolean() {
    testEncodeDecode(true);
  }

  @Test
  public void testQualifiedByte() {
    testEncodeDecode((byte) 120);
  }

  @Test
  public void testQualifiedCharacter() {
    testEncodeDecode('a');
  }

  @Test
  public void testUserEntity() {
    User user = new User();
    user.setUserName("foo");
    user.setPassword("bar");

    Set<Role> roles = new HashSet<Role>();
    roles.add(new Role("admin"));
    roles.add(new Role("users"));

    user.setRoles(roles);

    testEncodeDecode(user);
  }

  @Test
  public void testEntityWithNullBoxedNatives() {
    NullBoxedNatives entity = new NullBoxedNatives();
    testEncodeDecode(entity);
  }

  @Test
  public void testNullEnumInEntity() {
    ItemWithEnum itemWithEnum = new ItemWithEnum();
    testEncodeDecode(itemWithEnum);
  }
}
