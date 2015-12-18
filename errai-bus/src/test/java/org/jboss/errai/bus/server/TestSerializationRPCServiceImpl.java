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
import org.jboss.errai.bus.client.tests.support.Outer;
import org.jboss.errai.bus.client.tests.support.Outer2;
import org.jboss.errai.bus.client.tests.support.Student;
import org.jboss.errai.bus.client.tests.support.SubMoron;
import org.jboss.errai.bus.client.tests.support.TestEnumA;
import org.jboss.errai.bus.client.tests.support.TestSerializationRPCService;
import org.jboss.errai.bus.client.tests.support.TestingTickCache;
import org.jboss.errai.bus.client.tests.support.TreeNodeContainer;
import org.jboss.errai.bus.client.tests.support.User;
import org.jboss.errai.bus.server.annotations.Service;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Service
public class TestSerializationRPCServiceImpl implements TestSerializationRPCService {

  @Override
  public String testString(String str) {
    return str;
  }

  @Override
  public int testInteger(int i) {
    return i;
  }

  @Override
  public long testLong(long l) {
    return l;
  }

  @Override
  public EntityWithConstructorAndMethodMappedLong testConstructorAndMethodMappedLong(
          EntityWithConstructorAndMethodMappedLong ewcamml) {
    return ewcamml;
  }

  @Override
  public double testDouble(double d) {
    return d;
  }

  @Override
  public float testFloat(float f) {
    return f;
  }

  @Override
  public short testShort(short s) {
    return s;
  }

  @Override
  public boolean testBoolean(boolean b) {
    return b;
  }

  @Override
  public char testCharacter(char c) {
    return c;
  }

  @Override
  public byte testByte(byte b) {
    return b;
  }

  @Override
  public String[] testStringArray(String[] str) {
    return str;
  }

  @Override
  public int[] testIntegerArray(int[] i) {
    return i;
  }

  @Override
  public long[] testLongArray(long[] l) {
    return l;
  }

  @Override
  public double[] testDoubleArray(double[] d) {
    return d;
  }

  @Override
  public float[] testFloatArray(float[] f) {
    return f;
  }

  @Override
  public short[] testShortArray(short[] s) {
    return s;
  }

  @Override
  public boolean[] testBooleanArray(boolean[] b) {
    return b;
  }

  @Override
  public char[] testCharacterArray(char[] c) {
    return c;
  }

  @Override
  public byte[] testByteArray(byte[] b) {
    return b;
  }

  @Override
  public NeverDeclareAnArrayOfThisType[] testPortableArray(NeverDeclareAnArrayOfThisType[] p) {
    return p;
  }

  @Override
  public List<Long> listOfLong(List<Long> list) {
    return list;
  }

  @Override
  public List<Integer> listOfInteger(List<Integer> list) {
    return list;
  }

  @Override
  public List<Float> listOfFloat(List<Float> list) {
    return list;
  }

  @Override
  public List<Short> listOfShort(List<Short> list) {
    return list;
  }

  @Override
  public List<Byte> listOfByte(List<Byte> list) {
    return list;
  }

  @Override
  public List<Boolean> listOfBoolean(List<Boolean> list) {
    return list;
  }

  @Override
  public List<Character> listOfCharacters(List<Character> list) {
    return list;
  }

  @Override
  public Set<String> setOfStrings(Set<String> set) {
    return set;
  }

  @Override
  public Map<Long, String> mapOfLongToString(Map<Long, String> map) {
    return map;
  }

  @Override
  public Map<Long, List<String>> mapOfLongToListOfStrings(Map<Long, List<String>> map) {
    return map;
  }

  @Override
  public Map<String, Float> mapOfStringToFloat(Map<String, Float> map) {
    return map;
  }

  @Override
  public Map<String, List<Double>> mapOfStringToListOfDoubles(Map<String, List<Double>> map) {
    return map;
  }

  @Override
  public Map<Group, Group> mapOfCustomTypes(Map<Group, Group> map) {
    return map;
  }

  @Override
  public Map<List<String>, Group> mapOfListOfStringsToCustomType(Map<List<String>, Group> map) {
    return map;
  }

  @Override
  public ClassWithNestedClass nestedClass(ClassWithNestedClass clazz) {
    return clazz;
  }

  @Override
  public EntityWithGenericCollections genericCollections(EntityWithGenericCollections ent) {
    return ent;
  }

  @Override
  public EntityWithStringBufferAndStringBuilder testStringBufferAndStringBuilder(
      EntityWithStringBufferAndStringBuilder entity) {
    return entity;
  }

  @Override
  public Throwable testSerializeThrowable(Throwable t) {
    return t;
  }

  @Override
  public AssertionError testSerializeAssertionError(AssertionError t) {
    return t;
  }

  @Override
  public Date testJavaUtilDate(Date d) {
    return d;
  }

  @Override
  public java.sql.Date testJavaSqlDate(java.sql.Date d) {
    return d;
  }

  @Override
  public FactoryEntity testFactorySerialization(FactoryEntity e) {
    return e;
  }

  @Override
  public Timestamp testTimestampSerialization(Timestamp ts) {
    return ts;
  }

  @Override
  public Time testTimeSerialization(Time time) {
    return time;
  }

  @Override
  public BigDecimal testBigDecimalSerialization(BigDecimal time) {
    return time;
  }

  @Override
  public BigInteger testBigIntegerSerialization(BigInteger time) {
    return time;
  }

  @Override
  public Queue testQueueSerialization(Queue queue) {
    return queue;
  }

  @Override
  public SortedMap testSortedMapSerialization(SortedMap sm) {
    return sm;
  }

  @Override
  public SortedSet testSortedSetSerialization(SortedSet sm) {
    return sm;
  }

  @Override
  public List<Byte> testListOfBytes(List<Byte> lb) {
    return lb;
  }

  @Override
  public List testInheritedDefinitionFromExistingParent(List list) {
    return list;
  }

  @Override
  public TestEnumA testNakedEnum(TestEnumA e) {
    return e;
  }

  @Override
  public Boron.Bean testPortableInnerClass(Boron.Bean b) {
    return b;
  }

  @Override
  public Koron testKoron(Koron k) {
    return k;
  }

  @Override
  public TestingTickCache testMoron(TestingTickCache m) {
    return m;
  }

  @Override
  public SubMoron testSubMoron(SubMoron s) {
    return s;
  }

  @Override
  public List<TreeNodeContainer> acceptTreeNodeContainers(List<TreeNodeContainer> listOfContainers) {
    int count = 0;
    for (TreeNodeContainer tc : listOfContainers) {
      count++;
    }

    return listOfContainers;
  }

  @Override
  public EntityWithUnqualifiedFields testEntityWithUnqualifiedFieldTypes(EntityWithUnqualifiedFields e) {
    return e;
  }

  @Override
  public GenericEntity testGenericEntity(GenericEntity e) {
    return e;
  }

  @Override
  public EntityWithSuperClassField testEntityWithSuperClassField(EntityWithSuperClassField e) {
    return e;
  }

  @Override
  public User testEntityWithNullField(User u) {
    return u;
  }

  @Override
  public EnumContainerContainer testEntityWithEnumContainerContainer(EnumContainerContainer ecc) {
    return ecc;
  }

  @Override
  public LinkedHashMap<String, Integer> testLinkedHashMap(LinkedHashMap<String, Integer> map) {
    return map;
  }

  @Override
  public LinkedHashSet<String> testLinkedHashSet(LinkedHashSet<String> set) {
    return set;
  }

  @Override
  public BuilderEntity testBuilderSerializationWithPrivateConstructor(BuilderEntity e) {
    return e;
  }

  @Override
  public ImplicitEnum testImplicitEnum(ImplicitEnum e) {
    return e;
  }

  @Override
  public EntityWithMapUsingAbstractValueType testEntityWithMapUsingAbstractValueType(
      EntityWithMapUsingAbstractValueType e) {
    return e;
  }

  @Override
  public EntityWithMapUsingAbstractKeyType testEntityWithMapUsingAbstractKeyType(EntityWithMapUsingAbstractKeyType e) {
    return e;
  }

  @Override
  public EntityWithMapUsingSubtypeValues testEntityWithMapUsingSubtypeValues(EntityWithMapUsingSubtypeValues e) {
    return e;
  }

  @Override
  public EntityWithInterfaceField testEntityWithInterfaceField(EntityWithInterfaceField e) {
    return e;
  }

  @Override
  public EntityWithInterfaceArrayField testEntityWithInterfaceArrayField(EntityWithInterfaceArrayField e) {
    return e;
  }

  @Override
  public ImmutableEnumContainer testImmutableEntityWithEnum(ImmutableEnumContainer iec) {
    return iec;
  }

  @Override
  public ImmutableArrayContainer testImmutableEntityWithArray(ImmutableArrayContainer e) {
    return e;
  }

  @Override
  public EntityWithInheritedTypeVariable<String> testEntityWithInheritedTypeVariable(EntityWithInheritedTypeVariable<String> e) {
    return e;
  }

  @Override
  public EntityWithTypesUsingNestedParameterizedTypes testEntityWithTypesUsingNestedParamTypes(EntityWithTypesUsingNestedParameterizedTypes e) {
    return e;
  }

  @Override
  public GenericEntitySubtypeInteger testGenericEntitySubtypeInteger(GenericEntitySubtypeInteger e) {
    return e;
  }

  @Override
  public GenericEntitySubtypeString testGenericEntitySubtypeString(GenericEntitySubtypeString e) {
    return e;
  }

  @Override
  public EntityWithGoodParts testEntityWithGoodParts(EntityWithGoodParts e) {
    return e;
  }

  @Override
  public GenericEntityWithConstructorMapping<String> testGenericEntityWithConstructorMapping(GenericEntityWithConstructorMapping<String> entity) {
    return entity;
  }

  @Override
  public EntityWithUnderscore_InClassName testEntityWithUnderscore_InClassName(EntityWithUnderscore_InClassName e) {
    return e;
  }

  @Override
  public EntityWithMixedMappingTypes testEntityWithMixedMappingTypes(EntityWithMixedMappingTypes entity) {
    return entity;
  }

  @Override
  public EntityWithFactoryMethodAndMixedMappingTypes testEntityWithFactoryMethodAndMixedMappingTypes(EntityWithFactoryMethodAndMixedMappingTypes entity) {
    return entity;
  }

  @Override
  public Outer testBackReferenceOrderingWithMapsTo(Outer entity) {
    return entity;
  }

  @Override
  public Outer2 testBackReferenceOrderingWithMapsToInverted(Outer2 entity) {
    return entity;
  }

  @Override
  public <A extends GenericEntity<R>, R extends Student> R testIncrediblyGenericRpcMethod(A arg) {
    arg.getField().setName("smarter");
    return arg.getField();
  }
}
