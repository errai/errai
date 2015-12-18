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

package org.jboss.errai.jpa.sync.test.client;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContextType;
import javax.persistence.metamodel.EntityType;

import org.jboss.errai.ioc.client.Container;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCBeanManagerLifecycle;
import org.jboss.errai.jpa.sync.client.local.ErraiAttributeAccessor;
import org.jboss.errai.jpa.sync.client.shared.JpaAttributeAccessor;
import org.jboss.errai.jpa.sync.test.client.entity.MethodAccessedZentity;
import org.jboss.errai.jpa.sync.test.client.entity.Zentity;

import com.google.gwt.junit.client.GWTTestCase;

/**
 * Tests the Errai client-side implementation of the
 * {@link JpaAttributeAccessor} interface.
 * <p>
 * There is a subclass of this test which runs all the same tests against the
 * server-side Java reflection implementation.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
public class ErraiJpaAttributeAccessorTest extends GWTTestCase {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.jpa.sync.test.DataSyncTests";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();

    new IOCBeanManagerLifecycle().resetBeanManager();

    // We need to bootstrap the IoC container manually because GWTTestCase
    // doesn't call onModuleLoad() for us.
    new Container().bootstrapContainer();
  }

  @Override
  protected void gwtTearDown() throws Exception {
    Container.reset();
    IOC.reset();
  }

  protected EntityManager getEntityManager() {
    return JpaTestClient.INSTANCE.entityManager;
  }

  protected JpaAttributeAccessor getAttributeAccessor() {
    return new ErraiAttributeAccessor();
  }

  public void testReadBoxedTypesFieldAccess() throws Exception {
    Zentity zentity = new Zentity();
    zentity.setBoxedBool(true);
    zentity.setBoxedByte((byte) 4);
    zentity.setBoxedByteArray(new Byte[] { 1, null, 3, 4 });
    zentity.setBoxedChar('\u1234');
    zentity.setBoxedCharArray(new Character[] { 'a', '\u3030', 'c', null });
    zentity.setBoxedDouble(0.0000012329378462346d);
    zentity.setBoxedFloat(19873743.34442f);
    zentity.setBoxedInt(-3333);
    zentity.setBoxedLong(99999999999999L);
    zentity.setBoxedShort((short) -42);

    JpaAttributeAccessor a = getAttributeAccessor();
    EntityManager em = getEntityManager();
    EntityType<Zentity> metaZentity = em.getMetamodel().entity(Zentity.class);

    assertEquals(true, a.get(metaZentity.getAttribute("boxedBool"), zentity));
    assertEquals((byte) 4, a.get(metaZentity.getAttribute("boxedByte"), zentity));
    assertEquals(
            Arrays.toString(new Byte[] { 1, null, 3, 4 }),
            Arrays.toString((Byte[]) a.get(metaZentity.getAttribute("boxedByteArray"), zentity)));
    assertEquals('\u1234', a.get(metaZentity.getAttribute("boxedChar"), zentity));
    assertEquals(
            Arrays.toString(new Character[] { 'a', '\u3030', 'c', null }),
            Arrays.toString((Character[]) a.get(metaZentity.getAttribute("boxedCharArray"), zentity)));
    assertEquals(0.0000012329378462346d, a.get(metaZentity.getAttribute("boxedDouble"), zentity));
    assertEquals(19873743.34442f, a.get(metaZentity.getAttribute("boxedFloat"), zentity));
    assertEquals(-3333, a.get(metaZentity.getAttribute("boxedInt"), zentity));
    assertEquals(99999999999999L, a.get(metaZentity.getAttribute("boxedLong"), zentity));
    assertEquals((short) -42, a.get(metaZentity.getAttribute("boxedShort"), zentity));
  }

  public void testReadBoxedTypesMethodAccess() throws Exception {
    MethodAccessedZentity zentity = new MethodAccessedZentity();
    zentity.setBoxedBool(true);
    zentity.setBoxedByte((byte) 4);
    zentity.setBoxedByteArray(new Byte[] { 1, null, 3, 4 });
    zentity.setBoxedChar('\u1234');
    zentity.setBoxedCharArray(new Character[] { 'a', '\u3030', 'c', null });
    zentity.setBoxedDouble(0.0000012329378462346d);
    zentity.setBoxedFloat(19873743.34442f);
    zentity.setBoxedInt(-3333);
    zentity.setBoxedLong(99999999999999L);
    zentity.setBoxedShort((short) -42);

    JpaAttributeAccessor a = getAttributeAccessor();
    EntityManager em = getEntityManager();
    EntityType<MethodAccessedZentity> metaZentity = em.getMetamodel().entity(MethodAccessedZentity.class);

    assertEquals(true, a.get(metaZentity.getAttribute("boxedBool"), zentity));
    assertEquals((byte) 4, a.get(metaZentity.getAttribute("boxedByte"), zentity));
    assertEquals(
            Arrays.toString(new Byte[] { 1, null, 3, 4 }),
            Arrays.toString((Byte[]) a.get(metaZentity.getAttribute("boxedByteArray"), zentity)));
    assertEquals('\u1234', a.get(metaZentity.getAttribute("boxedChar"), zentity));
    assertEquals(
            Arrays.toString(new Character[] { 'a', '\u3030', 'c', null }),
            Arrays.toString((Character[]) a.get(metaZentity.getAttribute("boxedCharArray"), zentity)));
    assertEquals(0.0000012329378462346d, a.get(metaZentity.getAttribute("boxedDouble"), zentity));
    assertEquals(19873743.34442f, a.get(metaZentity.getAttribute("boxedFloat"), zentity));
    assertEquals(-3333, a.get(metaZentity.getAttribute("boxedInt"), zentity));
    assertEquals(99999999999999L, a.get(metaZentity.getAttribute("boxedLong"), zentity));
    assertEquals((short) -42, a.get(metaZentity.getAttribute("boxedShort"), zentity));
  }

  public void testWriteBoxedTypesFieldAccess() throws Exception {
    JpaAttributeAccessor a = getAttributeAccessor();
    EntityManager em = getEntityManager();
    EntityType<Zentity> metaZentity = em.getMetamodel().entity(Zentity.class);

    Zentity zentity = new Zentity();
    a.set(metaZentity.getSingularAttribute("boxedBool", Boolean.class), zentity, true);
    a.set(metaZentity.getSingularAttribute("boxedByte", Byte.class), zentity, (byte) 4);
    a.set(metaZentity.getSingularAttribute("boxedByteArray", Byte[].class), zentity, new Byte[] { 1, null, 3, 4 });
    a.set(metaZentity.getSingularAttribute("boxedChar", Character.class), zentity, '\u1234');
    a.set(metaZentity.getSingularAttribute("boxedCharArray", Character[].class), zentity, new Character[] { 'a', '\u3030', 'c', null });
    a.set(metaZentity.getSingularAttribute("boxedDouble", Double.class), zentity, 0.0000012329378462346d);
    a.set(metaZentity.getSingularAttribute("boxedFloat", Float.class), zentity, 19873743.34442f);
    a.set(metaZentity.getSingularAttribute("boxedInt", Integer.class), zentity, -3333);
    a.set(metaZentity.getSingularAttribute("boxedLong", Long.class), zentity, 99999999999999L);
    a.set(metaZentity.getSingularAttribute("boxedShort", Short.class), zentity, (short) -42);

    assertEquals(Boolean.valueOf(true), zentity.getBoxedBool());
    assertEquals(Byte.valueOf((byte) 4), zentity.getBoxedByte());
    assertEquals(
            Arrays.toString(new Byte[] { 1, null, 3, 4 }),
            Arrays.toString(zentity.getBoxedByteArray()));
    assertEquals(Character.valueOf('\u1234'), zentity.getBoxedChar());
    assertEquals(
            Arrays.toString(new Character[] { 'a', '\u3030', 'c', null }),
            Arrays.toString(zentity.getBoxedCharArray()));
    assertEquals(0.0000012329378462346d, zentity.getBoxedDouble());
    assertEquals(19873743.34442f, zentity.getBoxedFloat());
    assertEquals(Integer.valueOf(-3333), zentity.getBoxedInt());
    assertEquals(Long.valueOf(99999999999999L), zentity.getBoxedLong());
    assertEquals(Short.valueOf((short) -42), zentity.getBoxedShort());
  }

  public void testWriteBoxedTypesMethodAccess() throws Exception {
    JpaAttributeAccessor a = getAttributeAccessor();
    EntityManager em = getEntityManager();
    EntityType<MethodAccessedZentity> metaZentity = em.getMetamodel().entity(MethodAccessedZentity.class);

    MethodAccessedZentity zentity = new MethodAccessedZentity();
    a.set(metaZentity.getSingularAttribute("boxedBool", Boolean.class), zentity, true);
    a.set(metaZentity.getSingularAttribute("boxedByte", Byte.class), zentity, (byte) 4);
    a.set(metaZentity.getSingularAttribute("boxedByteArray", Byte[].class), zentity, new Byte[] { 1, null, 3, 4 });
    a.set(metaZentity.getSingularAttribute("boxedChar", Character.class), zentity, '\u1234');
    a.set(metaZentity.getSingularAttribute("boxedCharArray", Character[].class), zentity, new Character[] { 'a', '\u3030', 'c', null });
    a.set(metaZentity.getSingularAttribute("boxedDouble", Double.class), zentity, 0.0000012329378462346d);
    a.set(metaZentity.getSingularAttribute("boxedFloat", Float.class), zentity, 19873743.34442f);
    a.set(metaZentity.getSingularAttribute("boxedInt", Integer.class), zentity, -3333);
    a.set(metaZentity.getSingularAttribute("boxedLong", Long.class), zentity, 99999999999999L);
    a.set(metaZentity.getSingularAttribute("boxedShort", Short.class), zentity, (short) -42);

    assertEquals(Boolean.valueOf(true), zentity.getBoxedBool());
    assertEquals(Byte.valueOf((byte) 4), zentity.getBoxedByte());
    assertEquals(
            Arrays.toString(new Byte[] { 1, null, 3, 4 }),
            Arrays.toString(zentity.getBoxedByteArray()));
    assertEquals(Character.valueOf('\u1234'), zentity.getBoxedChar());
    assertEquals(
            Arrays.toString(new Character[] { 'a', '\u3030', 'c', null }),
            Arrays.toString(zentity.getBoxedCharArray()));
    assertEquals(0.0000012329378462346d, zentity.getBoxedDouble());
    assertEquals(19873743.34442f, zentity.getBoxedFloat());
    assertEquals(Integer.valueOf(-3333), zentity.getBoxedInt());
    assertEquals(Long.valueOf(99999999999999L), zentity.getBoxedLong());
    assertEquals(Short.valueOf((short) -42), zentity.getBoxedShort());
  }

  public void testReadNullBoxedTypesFieldAccess() throws Exception {
    Zentity zentity = new Zentity();

    JpaAttributeAccessor a = getAttributeAccessor();
    EntityManager em = getEntityManager();
    EntityType<Zentity> metaZentity = em.getMetamodel().entity(Zentity.class);

    assertEquals(null, a.get(metaZentity.getAttribute("boxedBool"), zentity));
    assertEquals(null, a.get(metaZentity.getAttribute("boxedByte"), zentity));
    assertEquals(null, a.get(metaZentity.getAttribute("boxedByteArray"), zentity));
    assertEquals(null, a.get(metaZentity.getAttribute("boxedChar"), zentity));
    assertEquals(null, a.get(metaZentity.getAttribute("boxedCharArray"), zentity));
    assertEquals(null, a.get(metaZentity.getAttribute("boxedDouble"), zentity));
    assertEquals(null, a.get(metaZentity.getAttribute("boxedFloat"), zentity));
    assertEquals(null, a.get(metaZentity.getAttribute("boxedInt"), zentity));
    assertEquals(null, a.get(metaZentity.getAttribute("boxedLong"), zentity));
    assertEquals(null, a.get(metaZentity.getAttribute("boxedShort"), zentity));
  }

  public void testReadNullBoxedTypesMethodAccess() throws Exception {
    MethodAccessedZentity zentity = new MethodAccessedZentity();

    JpaAttributeAccessor a = getAttributeAccessor();
    EntityManager em = getEntityManager();
    EntityType<MethodAccessedZentity> metaZentity = em.getMetamodel().entity(MethodAccessedZentity.class);

    assertEquals(null, a.get(metaZentity.getAttribute("boxedBool"), zentity));
    assertEquals(null, a.get(metaZentity.getAttribute("boxedByte"), zentity));
    assertEquals(null, a.get(metaZentity.getAttribute("boxedByteArray"), zentity));
    assertEquals(null, a.get(metaZentity.getAttribute("boxedChar"), zentity));
    assertEquals(null, a.get(metaZentity.getAttribute("boxedCharArray"), zentity));
    assertEquals(null, a.get(metaZentity.getAttribute("boxedDouble"), zentity));
    assertEquals(null, a.get(metaZentity.getAttribute("boxedFloat"), zentity));
    assertEquals(null, a.get(metaZentity.getAttribute("boxedInt"), zentity));
    assertEquals(null, a.get(metaZentity.getAttribute("boxedLong"), zentity));
    assertEquals(null, a.get(metaZentity.getAttribute("boxedShort"), zentity));
  }

  public void testReadPrimitiveTypesFieldAccess() throws Exception {
    Zentity zentity = new Zentity();
    zentity.setPrimitiveBool(true);
    zentity.setPrimitiveByte((byte) 4);
    zentity.setPrimitiveByteArray(new byte[] { 1, 2, 3, 4 });
    zentity.setPrimitiveChar('\u1234');
    zentity.setPrimitiveCharArray(new char[] { 'a', '\u3030', 'c', 'd' });
    zentity.setPrimitiveDouble(0.0000012329378462346d);
    zentity.setPrimitiveFloat(19873743.34442f);
    zentity.setPrimitiveInt(-3333);
    zentity.setPrimitiveLong(99999999999999L);
    zentity.setPrimitiveShort((short) -42);

    JpaAttributeAccessor a = getAttributeAccessor();
    EntityManager em = getEntityManager();
    EntityType<Zentity> metaZentity = em.getMetamodel().entity(Zentity.class);

    assertEquals(true, a.get(metaZentity.getAttribute("primitiveBool"), zentity));
    assertEquals((byte) 4, a.get(metaZentity.getAttribute("primitiveByte"), zentity));
    assertEquals(
            Arrays.toString(new byte[] { 1, 2, 3, 4 }),
            Arrays.toString((byte[]) a.get(metaZentity.getAttribute("primitiveByteArray"), zentity)));
    assertEquals('\u1234', a.get(metaZentity.getAttribute("primitiveChar"), zentity));
    assertEquals(
            Arrays.toString(new char[] { 'a', '\u3030', 'c', 'd' }),
            Arrays.toString((char[]) a.get(metaZentity.getAttribute("primitiveCharArray"), zentity)));
    assertEquals(0.0000012329378462346d, a.get(metaZentity.getAttribute("primitiveDouble"), zentity));
    assertEquals(19873743.34442f, a.get(metaZentity.getAttribute("primitiveFloat"), zentity));
    assertEquals(-3333, a.get(metaZentity.getAttribute("primitiveInt"), zentity));
    assertEquals(99999999999999L, a.get(metaZentity.getAttribute("primitiveLong"), zentity));
    assertEquals((short) -42, a.get(metaZentity.getAttribute("primitiveShort"), zentity));
  }

  public void testReadPrimitiveTypesMethodAccess() throws Exception {
    MethodAccessedZentity zentity = new MethodAccessedZentity();
    zentity.setPrimitiveBool(true);
    zentity.setPrimitiveByte((byte) 4);
    zentity.setPrimitiveByteArray(new byte[] { 1, 2, 3, 4 });
    zentity.setPrimitiveChar('\u1234');
    zentity.setPrimitiveCharArray(new char[] { 'a', '\u3030', 'c', 'd' });
    zentity.setPrimitiveDouble(0.0000012329378462346d);
    zentity.setPrimitiveFloat(19873743.34442f);
    zentity.setPrimitiveInt(-3333);
    zentity.setPrimitiveLong(99999999999999L);
    zentity.setPrimitiveShort((short) -42);

    JpaAttributeAccessor a = getAttributeAccessor();
    EntityManager em = getEntityManager();
    EntityType<MethodAccessedZentity> metaZentity = em.getMetamodel().entity(MethodAccessedZentity.class);

    assertEquals(true, a.get(metaZentity.getAttribute("primitiveBool"), zentity));
    assertEquals((byte) 4, a.get(metaZentity.getAttribute("primitiveByte"), zentity));
    assertEquals(
            Arrays.toString(new byte[] { 1, 2, 3, 4 }),
            Arrays.toString((byte[]) a.get(metaZentity.getAttribute("primitiveByteArray"), zentity)));
    assertEquals('\u1234', a.get(metaZentity.getAttribute("primitiveChar"), zentity));
    assertEquals(
            Arrays.toString(new char[] { 'a', '\u3030', 'c', 'd' }),
            Arrays.toString((char[]) a.get(metaZentity.getAttribute("primitiveCharArray"), zentity)));
    assertEquals(0.0000012329378462346d, a.get(metaZentity.getAttribute("primitiveDouble"), zentity));
    assertEquals(19873743.34442f, a.get(metaZentity.getAttribute("primitiveFloat"), zentity));
    assertEquals(-3333, a.get(metaZentity.getAttribute("primitiveInt"), zentity));
    assertEquals(99999999999999L, a.get(metaZentity.getAttribute("primitiveLong"), zentity));
    assertEquals((short) -42, a.get(metaZentity.getAttribute("primitiveShort"), zentity));
  }

  public void testWritePrimitiveTypesFieldAccess() throws Exception {
    JpaAttributeAccessor a = getAttributeAccessor();
    EntityManager em = getEntityManager();
    EntityType<Zentity> metaZentity = em.getMetamodel().entity(Zentity.class);

    Zentity zentity = new Zentity();
    a.set(metaZentity.getSingularAttribute("primitiveBool", boolean.class), zentity, true);
    a.set(metaZentity.getSingularAttribute("primitiveByte", byte.class), zentity, (byte) 4);
    a.set(metaZentity.getSingularAttribute("primitiveByteArray", byte[].class), zentity, new byte[] { 1, 2, 3, 4 });
    a.set(metaZentity.getSingularAttribute("primitiveChar", char.class), zentity, '\u1234');
    a.set(metaZentity.getSingularAttribute("primitiveCharArray", char[].class), zentity, new char[] { 'a', '\u3030', 'c', 'd' });
    a.set(metaZentity.getSingularAttribute("primitiveDouble", double.class), zentity, 0.0000012329378462346d);
    a.set(metaZentity.getSingularAttribute("primitiveFloat", float.class), zentity, 19873743.34442f);
    a.set(metaZentity.getSingularAttribute("primitiveInt", int.class), zentity, -3333);
    a.set(metaZentity.getSingularAttribute("primitiveLong", long.class), zentity, 99999999999999L);
    a.set(metaZentity.getSingularAttribute("primitiveShort", short.class), zentity, (short) -42);

    assertEquals(true, zentity.getPrimitiveBool());
    assertEquals((byte) 4, zentity.getPrimitiveByte());
    assertEquals(
            Arrays.toString(new byte[] { 1, 2, 3, 4 }),
            Arrays.toString(zentity.getPrimitiveByteArray()));
    assertEquals('\u1234', zentity.getPrimitiveChar());
    assertEquals(
            Arrays.toString(new char[] { 'a', '\u3030', 'c', 'd' }),
            Arrays.toString(zentity.getPrimitiveCharArray()));
    assertEquals(0.0000012329378462346d, zentity.getPrimitiveDouble());
    assertEquals(19873743.34442f, zentity.getPrimitiveFloat());
    assertEquals(-3333, zentity.getPrimitiveInt());
    assertEquals(99999999999999L, zentity.getPrimitiveLong());
    assertEquals((short) -42, zentity.getPrimitiveShort());
  }

  public void testWritePrimitiveTypesMethodAccess() throws Exception {
    JpaAttributeAccessor a = getAttributeAccessor();
    EntityManager em = getEntityManager();
    EntityType<MethodAccessedZentity> metaZentity = em.getMetamodel().entity(MethodAccessedZentity.class);

    MethodAccessedZentity zentity = new MethodAccessedZentity();
    a.set(metaZentity.getSingularAttribute("primitiveBool", boolean.class), zentity, true);
    a.set(metaZentity.getSingularAttribute("primitiveByte", byte.class), zentity, (byte) 4);
    a.set(metaZentity.getSingularAttribute("primitiveByteArray", byte[].class), zentity, new byte[] { 1, 2, 3, 4 });
    a.set(metaZentity.getSingularAttribute("primitiveChar", char.class), zentity, '\u1234');
    a.set(metaZentity.getSingularAttribute("primitiveCharArray", char[].class), zentity, new char[] { 'a', '\u3030', 'c', 'd' });
    a.set(metaZentity.getSingularAttribute("primitiveDouble", double.class), zentity, 0.0000012329378462346d);
    a.set(metaZentity.getSingularAttribute("primitiveFloat", float.class), zentity, 19873743.34442f);
    a.set(metaZentity.getSingularAttribute("primitiveInt", int.class), zentity, -3333);
    a.set(metaZentity.getSingularAttribute("primitiveLong", long.class), zentity, 99999999999999L);
    a.set(metaZentity.getSingularAttribute("primitiveShort", short.class), zentity, (short) -42);

    assertEquals(true, zentity.isPrimitiveBool());
    assertEquals((byte) 4, zentity.getPrimitiveByte());
    assertEquals(
            Arrays.toString(new byte[] { 1, 2, 3, 4 }),
            Arrays.toString(zentity.getPrimitiveByteArray()));
    assertEquals('\u1234', zentity.getPrimitiveChar());
    assertEquals(
            Arrays.toString(new char[] { 'a', '\u3030', 'c', 'd' }),
            Arrays.toString(zentity.getPrimitiveCharArray()));
    assertEquals(0.0000012329378462346d, zentity.getPrimitiveDouble());
    assertEquals(19873743.34442f, zentity.getPrimitiveFloat());
    assertEquals(-3333, zentity.getPrimitiveInt());
    assertEquals(99999999999999L, zentity.getPrimitiveLong());
    assertEquals((short) -42, zentity.getPrimitiveShort());
  }

  public void testReadObjectTypesFieldAccess() throws Exception {
    Zentity zentity = new Zentity();
    zentity.setBigDecimal(new BigDecimal("1231231223847628347692834.5762305986471348576298346753"));
    zentity.setBigInteger(new BigInteger("23874502938475023985763457692384756918324572345"));
    zentity.setDate(new Date(12345678L));
    zentity.setEnumeration(PersistenceContextType.EXTENDED);
    zentity.setString("this is \u3833 a string");

    JpaAttributeAccessor a = getAttributeAccessor();
    EntityManager em = getEntityManager();
    EntityType<Zentity> metaZentity = em.getMetamodel().entity(Zentity.class);

    assertEquals(new BigDecimal("1231231223847628347692834.5762305986471348576298346753"), a.get(metaZentity.getAttribute("bigDecimal"), zentity));
    assertEquals(new BigInteger("23874502938475023985763457692384756918324572345"), a.get(metaZentity.getAttribute("bigInteger"), zentity));
    assertEquals(new Date(12345678L), a.get(metaZentity.getAttribute("date"), zentity));
    assertEquals(PersistenceContextType.EXTENDED, a.get(metaZentity.getAttribute("enumeration"), zentity));
    assertEquals("this is \u3833 a string", a.get(metaZentity.getAttribute("string"), zentity));
  }

  public void testReadObjectTypesMethodAccess() throws Exception {
    MethodAccessedZentity zentity = new MethodAccessedZentity();
    zentity.setBigDecimal(new BigDecimal("1231231223847628347692834.5762305986471348576298346753"));
    zentity.setBigInteger(new BigInteger("23874502938475023985763457692384756918324572345"));
    zentity.setDate(new Date(12345678L));
    zentity.setEnumeration(PersistenceContextType.EXTENDED);
    zentity.setString("this is \u3833 a string");

    JpaAttributeAccessor a = getAttributeAccessor();
    EntityManager em = getEntityManager();
    EntityType<MethodAccessedZentity> metaZentity = em.getMetamodel().entity(MethodAccessedZentity.class);

    assertEquals(new BigDecimal("1231231223847628347692834.5762305986471348576298346753"), a.get(metaZentity.getAttribute("bigDecimal"), zentity));
    assertEquals(new BigInteger("23874502938475023985763457692384756918324572345"), a.get(metaZentity.getAttribute("bigInteger"), zentity));
    assertEquals(new Date(12345678L), a.get(metaZentity.getAttribute("date"), zentity));
    assertEquals(PersistenceContextType.EXTENDED, a.get(metaZentity.getAttribute("enumeration"), zentity));
    assertEquals("this is \u3833 a string", a.get(metaZentity.getAttribute("string"), zentity));
  }

}
