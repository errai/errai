/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.server;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import org.jboss.errai.bus.client.tests.support.Boron;
import org.jboss.errai.bus.client.tests.support.BuilderEntity;
import org.jboss.errai.bus.client.tests.support.ClassWithNestedClass;
import org.jboss.errai.bus.client.tests.support.EntityWithClassFieldAndMap;
import org.jboss.errai.bus.client.tests.support.EntityWithConstructorAndMethodMappedLong;
import org.jboss.errai.bus.client.tests.support.EntityWithFactoryMethodAndMixedMappingTypes;
import org.jboss.errai.bus.client.tests.support.EntityWithGenericCollections;
import org.jboss.errai.bus.client.tests.support.EntityWithGoodParts;
import org.jboss.errai.bus.client.tests.support.EntityWithInheritedTypeVariable;
import org.jboss.errai.bus.client.tests.support.EntityWithInterfaceArrayField;
import org.jboss.errai.bus.client.tests.support.EntityWithInterfaceField;
import org.jboss.errai.bus.client.tests.support.EntityWithMapUsingAbstractKeyType;
import org.jboss.errai.bus.client.tests.support.EntityWithMapUsingAbstractValueType;
import org.jboss.errai.bus.client.tests.support.EntityWithMapUsingSubtypeValues;
import org.jboss.errai.bus.client.tests.support.EntityWithMixedMappingTypes;
import org.jboss.errai.bus.client.tests.support.EntityWithStringBufferAndStringBuilder;
import org.jboss.errai.bus.client.tests.support.EntityWithSuperClassField;
import org.jboss.errai.bus.client.tests.support.EntityWithTypesUsingNestedParameterizedTypes;
import org.jboss.errai.bus.client.tests.support.EntityWithUnderscore_InClassName;
import org.jboss.errai.bus.client.tests.support.EntityWithUnqualifiedFields;
import org.jboss.errai.bus.client.tests.support.EnumContainerContainer;
import org.jboss.errai.bus.client.tests.support.FactoryEntity;
import org.jboss.errai.bus.client.tests.support.GenericEntity;
import org.jboss.errai.bus.client.tests.support.GenericEntitySubtypeInteger;
import org.jboss.errai.bus.client.tests.support.GenericEntitySubtypeString;
import org.jboss.errai.bus.client.tests.support.GenericEntityWithConstructorMapping;
import org.jboss.errai.bus.client.tests.support.Group;
import org.jboss.errai.bus.client.tests.support.ImmutableArrayContainer;
import org.jboss.errai.bus.client.tests.support.ImmutableEnumContainer;
import org.jboss.errai.bus.client.tests.support.ImplicitEnum;
import org.jboss.errai.bus.client.tests.support.Koron;
import org.jboss.errai.bus.client.tests.support.NeverDeclareAnArrayOfThisType;
import org.jboss.errai.bus.client.tests.support.ConcreteNonPortableParent;
import org.jboss.errai.bus.client.tests.support.Outer;
import org.jboss.errai.bus.client.tests.support.Outer2;
import org.jboss.errai.bus.client.tests.support.Student;
import org.jboss.errai.bus.client.tests.support.SubMoron;
import org.jboss.errai.bus.client.tests.support.TestEnumA;
import org.jboss.errai.bus.client.tests.support.TestSerializationRPCService;
import org.jboss.errai.bus.client.tests.support.TestingTickCache;
import org.jboss.errai.bus.client.tests.support.TreeNodeContainer;
import org.jboss.errai.bus.client.tests.support.User;
import org.jboss.errai.bus.client.tests.support.pkg.PortableType1;
import org.jboss.errai.bus.client.tests.support.pkg.serializablesubpkg.PortableType2;
import org.jboss.errai.bus.server.annotations.Service;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Service
public class TestSerializationRPCServiceImpl implements TestSerializationRPCService {

  @Override
  public String testString(final String str) {
    return str;
  }

  @Override
  public int testInteger(final int i) {
    return i;
  }

  @Override
  public long testLong(final long l) {
    return l;
  }

  @Override
  public EntityWithConstructorAndMethodMappedLong testConstructorAndMethodMappedLong(
          final EntityWithConstructorAndMethodMappedLong ewcamml) {
    return ewcamml;
  }

  @Override
  public double testDouble(final double d) {
    return d;
  }

  @Override
  public float testFloat(final float f) {
    return f;
  }

  @Override
  public short testShort(final short s) {
    return s;
  }

  @Override
  public boolean testBoolean(final boolean b) {
    return b;
  }

  @Override
  public char testCharacter(final char c) {
    return c;
  }

  @Override
  public byte testByte(final byte b) {
    return b;
  }

  @Override
  public String[] testStringArray(final String[] str) {
    return str;
  }

  @Override
  public int[] testIntegerArray(final int[] i) {
    return i;
  }

  @Override
  public long[] testLongArray(final long[] l) {
    return l;
  }

  @Override
  public double[] testDoubleArray(final double[] d) {
    return d;
  }

  @Override
  public float[] testFloatArray(final float[] f) {
    return f;
  }

  @Override
  public short[] testShortArray(final short[] s) {
    return s;
  }

  @Override
  public boolean[] testBooleanArray(final boolean[] b) {
    return b;
  }

  @Override
  public char[] testCharacterArray(final char[] c) {
    return c;
  }

  @Override
  public byte[] testByteArray(final byte[] b) {
    return b;
  }

  @Override
  public NeverDeclareAnArrayOfThisType[] testPortableArray(final NeverDeclareAnArrayOfThisType[] p) {
    return p;
  }

  @Override
  public List<Long> listOfLong(final List<Long> list) {
    return list;
  }

  @Override
  public List<Integer> listOfInteger(final List<Integer> list) {
    return list;
  }

  @Override
  public List<Float> listOfFloat(final List<Float> list) {
    return list;
  }

  @Override
  public List<Short> listOfShort(final List<Short> list) {
    return list;
  }

  @Override
  public List<Byte> listOfByte(final List<Byte> list) {
    return list;
  }

  @Override
  public List<Boolean> listOfBoolean(final List<Boolean> list) {
    return list;
  }

  @Override
  public List<Character> listOfCharacters(final List<Character> list) {
    return list;
  }

  @Override
  public Set<String> setOfStrings(final Set<String> set) {
    return set;
  }

  @Override
  public Map<Long, String> mapOfLongToString(final Map<Long, String> map) {
    return map;
  }

  @Override
  public Map<Long, List<String>> mapOfLongToListOfStrings(final Map<Long, List<String>> map) {
    return map;
  }

  @Override
  public Map<String, Float> mapOfStringToFloat(final Map<String, Float> map) {
    return map;
  }

  @Override
  public Map<String, List<Double>> mapOfStringToListOfDoubles(final Map<String, List<Double>> map) {
    return map;
  }

  @Override
  public Map<Group, Group> mapOfCustomTypes(final Map<Group, Group> map) {
    return map;
  }

  @Override
  public Map<List<String>, Group> mapOfListOfStringsToCustomType(final Map<List<String>, Group> map) {
    return map;
  }

  @Override
  public ClassWithNestedClass nestedClass(final ClassWithNestedClass clazz) {
    return clazz;
  }

  @Override
  public EntityWithGenericCollections genericCollections(final EntityWithGenericCollections ent) {
    return ent;
  }

  @Override
  public EntityWithStringBufferAndStringBuilder testStringBufferAndStringBuilder(
      final EntityWithStringBufferAndStringBuilder entity) {
    return entity;
  }

  @Override
  public Throwable testSerializeThrowable(final Throwable t) {
    return t;
  }

  @Override
  public AssertionError testSerializeAssertionError(final AssertionError t) {
    return t;
  }

  @Override
  public Date testJavaUtilDate(final Date d) {
    return d;
  }

  @Override
  public java.sql.Date testJavaSqlDate(final java.sql.Date d) {
    return d;
  }

  @Override
  public FactoryEntity testFactorySerialization(final FactoryEntity e) {
    return e;
  }

  @Override
  public Timestamp testTimestampSerialization(final Timestamp ts) {
    return ts;
  }

  @Override
  public Time testTimeSerialization(final Time time) {
    return time;
  }

  @Override
  public BigDecimal testBigDecimalSerialization(final BigDecimal time) {
    return time;
  }

  @Override
  public BigInteger testBigIntegerSerialization(final BigInteger time) {
    return time;
  }

  @Override
  public Queue testQueueSerialization(final Queue queue) {
    return queue;
  }

  @Override
  public SortedMap testSortedMapSerialization(final SortedMap sm) {
    return sm;
  }

  @Override
  public SortedSet testSortedSetSerialization(final SortedSet sm) {
    return sm;
  }

  @Override
  public List<Byte> testListOfBytes(final List<Byte> lb) {
    return lb;
  }

  @Override
  public List testInheritedDefinitionFromExistingParent(final List list) {
    return list;
  }

  @Override
  public TestEnumA testNakedEnum(final TestEnumA e) {
    return e;
  }

  @Override
  public Boron.Bean testPortableInnerClass(final Boron.Bean b) {
    return b;
  }

  @Override
  public Koron testKoron(final Koron k) {
    return k;
  }

  @Override
  public TestingTickCache testMoron(final TestingTickCache m) {
    return m;
  }

  @Override
  public SubMoron testSubMoron(final SubMoron s) {
    return s;
  }

  @Override
  public List<TreeNodeContainer> acceptTreeNodeContainers(final List<TreeNodeContainer> listOfContainers) {
    int count = 0;
    for (final TreeNodeContainer tc : listOfContainers) {
      count++;
    }

    return listOfContainers;
  }

  @Override
  public EntityWithUnqualifiedFields testEntityWithUnqualifiedFieldTypes(final EntityWithUnqualifiedFields e) {
    return e;
  }

  @Override
  public GenericEntity testGenericEntity(final GenericEntity e) {
    return e;
  }

  @Override
  public EntityWithSuperClassField testEntityWithSuperClassField(final EntityWithSuperClassField e) {
    return e;
  }

  @Override
  public User testEntityWithNullField(final User u) {
    return u;
  }

  @Override
  public EnumContainerContainer testEntityWithEnumContainerContainer(final EnumContainerContainer ecc) {
    return ecc;
  }

  @Override
  public LinkedHashMap<String, Integer> testLinkedHashMap(final LinkedHashMap<String, Integer> map) {
    return map;
  }

  @Override
  public LinkedHashSet<String> testLinkedHashSet(final LinkedHashSet<String> set) {
    return set;
  }

  @Override
  public BuilderEntity testBuilderSerializationWithPrivateConstructor(final BuilderEntity e) {
    return e;
  }

  @Override
  public ImplicitEnum testImplicitEnum(final ImplicitEnum e) {
    return e;
  }

  @Override
  public EntityWithMapUsingAbstractValueType testEntityWithMapUsingAbstractValueType(
      final EntityWithMapUsingAbstractValueType e) {
    return e;
  }

  @Override
  public EntityWithMapUsingAbstractKeyType testEntityWithMapUsingAbstractKeyType(final EntityWithMapUsingAbstractKeyType e) {
    return e;
  }

  @Override
  public EntityWithMapUsingSubtypeValues testEntityWithMapUsingSubtypeValues(final EntityWithMapUsingSubtypeValues e) {
    return e;
  }

  @Override
  public EntityWithInterfaceField testEntityWithInterfaceField(final EntityWithInterfaceField e) {
    return e;
  }

  @Override
  public EntityWithInterfaceArrayField testEntityWithInterfaceArrayField(final EntityWithInterfaceArrayField e) {
    return e;
  }

  @Override
  public ImmutableEnumContainer testImmutableEntityWithEnum(final ImmutableEnumContainer iec) {
    return iec;
  }

  @Override
  public ImmutableArrayContainer testImmutableEntityWithArray(final ImmutableArrayContainer e) {
    return e;
  }

  @Override
  public EntityWithInheritedTypeVariable<String> testEntityWithInheritedTypeVariable(final EntityWithInheritedTypeVariable<String> e) {
    return e;
  }

  @Override
  public EntityWithTypesUsingNestedParameterizedTypes testEntityWithTypesUsingNestedParamTypes(final EntityWithTypesUsingNestedParameterizedTypes e) {
    return e;
  }

  @Override
  public GenericEntitySubtypeInteger testGenericEntitySubtypeInteger(final GenericEntitySubtypeInteger e) {
    return e;
  }

  @Override
  public GenericEntitySubtypeString testGenericEntitySubtypeString(final GenericEntitySubtypeString e) {
    return e;
  }

  @Override
  public EntityWithGoodParts testEntityWithGoodParts(final EntityWithGoodParts e) {
    return e;
  }

  @Override
  public GenericEntityWithConstructorMapping<String> testGenericEntityWithConstructorMapping(final GenericEntityWithConstructorMapping<String> entity) {
    return entity;
  }

  @Override
  public EntityWithUnderscore_InClassName testEntityWithUnderscore_InClassName(final EntityWithUnderscore_InClassName e) {
    return e;
  }

  @Override
  public EntityWithMixedMappingTypes testEntityWithMixedMappingTypes(final EntityWithMixedMappingTypes entity) {
    return entity;
  }

  @Override
  public EntityWithFactoryMethodAndMixedMappingTypes testEntityWithFactoryMethodAndMixedMappingTypes(final EntityWithFactoryMethodAndMixedMappingTypes entity) {
    return entity;
  }

  @Override
  public Outer testBackReferenceOrderingWithMapsTo(final Outer entity) {
    return entity;
  }

  @Override
  public Outer2 testBackReferenceOrderingWithMapsToInverted(final Outer2 entity) {
    return entity;
  }

  @Override
  public <A extends GenericEntity<R>, R extends Student> R testIncrediblyGenericRpcMethod(final A arg) {
    arg.getField().setName("smarter");
    return arg.getField();
  }

  @Override
  public EntityWithClassFieldAndMap testEntityWithClassField(final EntityWithClassFieldAndMap entity) {
    return entity;
  }

  @Override
  public Object testMapSuperTypesPropertyCausesMarshallerMappingCreationForSuperType(final Object entity) {
    return entity;
  }

  @Override
  public PortableType1 testPortableTypeInSerializablePackage(final PortableType1 entity) {
    return entity;
  }

  @Override
  public PortableType2 testPortableTypeInSerializableSubPackage(final PortableType2 entity) {
    return entity;
  }
}
