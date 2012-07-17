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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.jboss.errai.marshalling.server.MappingContextSingleton;
import org.jboss.errai.marshalling.server.ServerMarshalling;
import org.jboss.errai.marshalling.tests.res.AImpl1;
import org.jboss.errai.marshalling.tests.res.AImpl2;
import org.jboss.errai.marshalling.tests.res.ASubImpl1;
import org.jboss.errai.marshalling.tests.res.BImpl1;
import org.jboss.errai.marshalling.tests.res.BImpl2;
import org.jboss.errai.marshalling.tests.res.EntityWithAbstractFieldType;
import org.jboss.errai.marshalling.tests.res.EntityWithInterface;
import org.jboss.errai.marshalling.tests.res.EntityWithInterfaceArray;
import org.jboss.errai.marshalling.tests.res.EntityWithPortableSubtypesInArray;
import org.jboss.errai.marshalling.tests.res.EntityWithPublicFields;
import org.jboss.errai.marshalling.tests.res.InterfaceA;
import org.jboss.errai.marshalling.tests.res.shared.ItemWithEnum;
import org.jboss.errai.marshalling.tests.res.shared.NullBoxedNatives;
import org.jboss.errai.marshalling.tests.res.shared.Role;
import org.jboss.errai.marshalling.tests.res.shared.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Mike Brock
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class MarshallingAPITest {

  @Before
  public void ensureMarshallingSystemInitialized() {
    MappingContextSingleton.get();
  }

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
  

  @Test
  public void testEntityWithInterface() {
    EntityWithInterface ewi = new EntityWithInterface();
    ewi.setA(new AImpl1(4711));
    testEncodeDecode(ewi);
   
    ewi = new EntityWithInterface();
    ewi.setA(new AImpl2("admin"));
    testEncodeDecode(ewi);
  }
  
  @Test
  public void testEntityWithInterfaceArray() {
    EntityWithInterfaceArray ewia = new EntityWithInterfaceArray();
    
    InterfaceA[] a = new InterfaceA[4];
    a[0] = new AImpl1(4711);
    a[1] = null;
    a[2] = new AImpl2("admin");
    a[3] = new ASubImpl1(11f);
    ewia.setA(a);
    
    testEncodeDecode(ewia);
  }

  @Test
  public void testEntityWithPortableSubtypesInArray() {
    EntityWithPortableSubtypesInArray ewpsia = new EntityWithPortableSubtypesInArray();
    
    AImpl1[] a = new AImpl1[3];
    a[0] = new AImpl1(4711);
    a[1] = null;
    a[2] = new ASubImpl1(11f);
    ewpsia.setA(a);
    
    testEncodeDecode(ewpsia);
  }

  @Test
  public void testEntityWithAbstractFieldType() {
    EntityWithAbstractFieldType ewaft = new EntityWithAbstractFieldType();
    ewaft.setB(new BImpl1(4711));
    testEncodeDecode(ewaft);
   
    ewaft = new EntityWithAbstractFieldType();
    ewaft.setB(new BImpl2("admin"));
    testEncodeDecode(ewaft);
  }
  
  @Test
  public void testEntityWithPublicFields() {
    EntityWithPublicFields ewpf = new EntityWithPublicFields();
    
    ArrayList<String> values = new ArrayList<String>();
    values.add("1");
    values.add("2");
    values.add("3");
    
    ewpf.value = 17;
    ewpf.values = values;
    
    testEncodeDecode(ewpf);
  }
}
