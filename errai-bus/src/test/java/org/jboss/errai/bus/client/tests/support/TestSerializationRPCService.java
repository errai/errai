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

package org.jboss.errai.bus.client.tests.support;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import org.jboss.errai.bus.server.annotations.Remote;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Remote
public interface TestSerializationRPCService {

  // This guard against regressions of https://issues.jboss.org/browse/ERRAI-476
  @Remote
  public interface DuplicateRemoteInterface {};

  public String testString(String str);

  public int testInteger(int i);

  public long testLong(long l);

  /**
   * @param ewcamml Pronounced "eww! camel!"
   */
  public EntityWithConstructorAndMethodMappedLong testConstructorAndMethodMappedLong(
          EntityWithConstructorAndMethodMappedLong ewcamml);

  public double testDouble(double d);

  public float testFloat(float f);

  public short testShort(short s);

  public boolean testBoolean(boolean b);

  public char testCharacter(char c);

  public byte testByte(byte b);


  public String[] testStringArray(String[] str);

  public int[] testIntegerArray(int[] i);

  public long[] testLongArray(long[] l);

  public double[] testDoubleArray(double[] d);

  public float[] testFloatArray(float[] f);

  public short[] testShortArray(short[] s);

  public boolean[] testBooleanArray(boolean[] b);

  public char[] testCharacterArray(char[] c);

  public byte[] testByteArray(byte[] b);

  public NeverDeclareAnArrayOfThisType[] testPortableArray(NeverDeclareAnArrayOfThisType[] expected);


  public List<Long> listOfLong(List<Long> list);

  public List<Integer> listOfInteger(List<Integer> list);

  public List<Float> listOfFloat(List<Float> list);

  public List<Short> listOfShort(List<Short> list);

  public List<Byte> listOfByte(List<Byte> list);

  public List<Boolean> listOfBoolean(List<Boolean> list);

  public List<Character> listOfCharacters(List<Character> list);

  public Set<String> setOfStrings(Set<String> set);

  public Map<Long, String> mapOfLongToString(Map<Long, String> map);

  public Map<Long, List<String>> mapOfLongToListOfStrings(Map<Long, List<String>> map);

  public Map<String, Float> mapOfStringToFloat(Map<String, Float> map);

  public Map<String, List<Double>> mapOfStringToListOfDoubles(Map<String, List<Double>> map);

  public Map<List<String>, Group> mapOfListOfStringsToCustomType(Map<List<String>, Group> map);

  public Map<Group, Group> mapOfCustomTypes(Map<Group, Group> map);

  public ClassWithNestedClass nestedClass(ClassWithNestedClass clazz);

  public EntityWithGenericCollections genericCollections(EntityWithGenericCollections ent);

  public EntityWithStringBufferAndStringBuilder testStringBufferAndStringBuilder(EntityWithStringBufferAndStringBuilder entity);

  public Throwable testSerializeThrowable(Throwable t);

  public AssertionError testSerializeAssertionError(AssertionError t);

  public FactoryEntity testFactorySerialization(FactoryEntity e);

  public BuilderEntity testBuilderSerializationWithPrivateConstructor(BuilderEntity e);

  public java.util.Date testJavaUtilDate(java.util.Date d);

  public Date testJavaSqlDate(Date d);

  public Timestamp testTimestampSerialization(Timestamp ts);

  public Time testTimeSerialization(Time time);

  public BigDecimal testBigDecimalSerialization(BigDecimal time);

  public BigInteger testBigIntegerSerialization(BigInteger time);

  public List<Byte> testListOfBytes(List<Byte> lb);

  public Queue testQueueSerialization(Queue queue);

  public SortedMap testSortedMapSerialization(SortedMap sm);

  public SortedSet testSortedSetSerialization(SortedSet sm);

  public List testInheritedDefinitionFromExistingParent(List list);

  public TestEnumA testNakedEnum(TestEnumA e);

  public Boron.Bean testPortableInnerClass(Boron.Bean b);

  public Koron testKoron(Koron k);

  public SubMoron testSubMoron(SubMoron s);

  public TestingTickCache testMoron(TestingTickCache moron);

  public List<TreeNodeContainer> acceptTreeNodeContainers(List<TreeNodeContainer> listOfContainers);

  public EntityWithUnqualifiedFields testEntityWithUnqualifiedFieldTypes(EntityWithUnqualifiedFields e);

  public EntityWithGoodParts testEntityWithGoodParts(EntityWithGoodParts e);

  public GenericEntity testGenericEntity(GenericEntity e);

  public GenericEntitySubtypeInteger testGenericEntitySubtypeInteger(GenericEntitySubtypeInteger e);

  public GenericEntitySubtypeString testGenericEntitySubtypeString(GenericEntitySubtypeString e);

  public EntityWithSuperClassField testEntityWithSuperClassField(EntityWithSuperClassField e);

  public User testEntityWithNullField(User u);

  public ImmutableEnumContainer testImmutableEntityWithEnum(ImmutableEnumContainer iec);

  public EnumContainerContainer testEntityWithEnumContainerContainer(EnumContainerContainer ecc);

  public EntityWithMapUsingAbstractValueType testEntityWithMapUsingAbstractValueType(EntityWithMapUsingAbstractValueType e);

  public EntityWithMapUsingAbstractKeyType testEntityWithMapUsingAbstractKeyType(EntityWithMapUsingAbstractKeyType e);

  public EntityWithMapUsingSubtypeValues testEntityWithMapUsingSubtypeValues(EntityWithMapUsingSubtypeValues e);

  public EntityWithTypesUsingNestedParameterizedTypes testEntityWithTypesUsingNestedParamTypes(EntityWithTypesUsingNestedParameterizedTypes e);

  public LinkedHashMap<String,Integer> testLinkedHashMap(LinkedHashMap<String,Integer> map);

  public LinkedHashSet<String> testLinkedHashSet(LinkedHashSet<String> set);

  public ImplicitEnum testImplicitEnum(ImplicitEnum e);

  public EntityWithInterfaceField testEntityWithInterfaceField(EntityWithInterfaceField e);

  public EntityWithInterfaceArrayField testEntityWithInterfaceArrayField(EntityWithInterfaceArrayField e);

  public ImmutableArrayContainer testImmutableEntityWithArray(ImmutableArrayContainer e);

  public EntityWithInheritedTypeVariable<String> testEntityWithInheritedTypeVariable(EntityWithInheritedTypeVariable<String> entity);

  public GenericEntityWithConstructorMapping<String> testGenericEntityWithConstructorMapping(GenericEntityWithConstructorMapping<String> entity);
  
  public <A extends GenericEntity<R>, R extends Student> R testIncrediblyGenericRpcMethod(A arg);

  public EntityWithUnderscore_InClassName testEntityWithUnderscore_InClassName(EntityWithUnderscore_InClassName e);

  public EntityWithMixedMappingTypes testEntityWithMixedMappingTypes(EntityWithMixedMappingTypes entity);

  public EntityWithFactoryMethodAndMixedMappingTypes testEntityWithFactoryMethodAndMixedMappingTypes(EntityWithFactoryMethodAndMixedMappingTypes entity);
  
  public Outer testBackReferenceOrderingWithMapsTo(Outer entity);
  
  public Outer2 testBackReferenceOrderingWithMapsToInverted(Outer2 entity);
}
