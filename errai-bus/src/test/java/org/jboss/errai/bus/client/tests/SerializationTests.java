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

package org.jboss.errai.bus.client.tests;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.tests.support.AImpl1;
import org.jboss.errai.bus.client.tests.support.AImpl2;
import org.jboss.errai.bus.client.tests.support.AbstractClassA;
import org.jboss.errai.bus.client.tests.support.Boron;
import org.jboss.errai.bus.client.tests.support.BuilderEntity;
import org.jboss.errai.bus.client.tests.support.ClassWithNestedClass;
import org.jboss.errai.bus.client.tests.support.CustomList;
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
import org.jboss.errai.bus.client.tests.support.EnumContainer;
import org.jboss.errai.bus.client.tests.support.EnumContainerContainer;
import org.jboss.errai.bus.client.tests.support.EnumWithAbstractMethod;
import org.jboss.errai.bus.client.tests.support.EnumWithState;
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
import org.jboss.errai.bus.client.tests.support.OneDimensionalPrimitiveArrayPortable;
import org.jboss.errai.bus.client.tests.support.Outer;
import org.jboss.errai.bus.client.tests.support.Outer2;
import org.jboss.errai.bus.client.tests.support.Person;
import org.jboss.errai.bus.client.tests.support.Student;
import org.jboss.errai.bus.client.tests.support.StudyTreeNodeContainer;
import org.jboss.errai.bus.client.tests.support.SubInterface;
import org.jboss.errai.bus.client.tests.support.SubInterfaceImpl;
import org.jboss.errai.bus.client.tests.support.SubMoron;
import org.jboss.errai.bus.client.tests.support.TestEnumA;
import org.jboss.errai.bus.client.tests.support.TestSerializationRPCService;
import org.jboss.errai.bus.client.tests.support.TestingTick;
import org.jboss.errai.bus.client.tests.support.TestingTickCache;
import org.jboss.errai.bus.client.tests.support.TreeNodeContainer;
import org.jboss.errai.bus.client.tests.support.User;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.marshalling.client.Marshalling;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class SerializationTests extends AbstractErraiTest {
  public static final String ENT_SER1_RESPONSE_SERVICE = "SerializationResponse1";

  @Override
  public String getModuleName() {
    return "org.jboss.errai.bus.ErraiBusTests";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    Marshalling.getMarshaller(OneDimensionalPrimitiveArrayPortable.class);
  }

  public void testString() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final String expected = "This is a test string";

        MessageBuilder.createCall(new RemoteCallback<String>() {
          @Override
          public void callback(String response) {
            assertEquals(expected, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testString(expected);
      }
    });
  }

  public void testStringWithNonAsciiChars() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final String expected = "Th\u00efs is a test string";

        MessageBuilder.createCall(new RemoteCallback<String>() {
          @Override
          public void callback(String response) {
            assertEquals(expected, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testString(expected);
      }
    });
  }

  public void testStringWithUnclosedCurlyBracket() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final String expected = "{";
        MessageBuilder.createCall(new RemoteCallback<String>() {
          @Override
          public void callback(String response) {
            assertEquals(expected, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testString(expected);
      }
    });
  }

  public void testStringWithUnclosedCurlyBracketAndEscapedQuotes() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final String expected = "\"{\"";
        MessageBuilder.createCall(new RemoteCallback<String>() {
          @Override
          public void callback(String response) {
            assertEquals(expected, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testString(expected);
      }
    });
  }

  public void testInteger() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final int expected = Integer.MAX_VALUE;

        MessageBuilder.createCall(new RemoteCallback<Integer>() {
          @Override
          public void callback(Integer response) {
            assertEquals(expected, response.intValue());
            finishTest();
          }
        }, TestSerializationRPCService.class).testInteger(expected);
      }
    });
  }

  public void testLong() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final long expected = Long.MAX_VALUE;

        MessageBuilder.createCall(new RemoteCallback<Long>() {
          @Override
          public void callback(Long response) {
            assertEquals(expected, response.longValue());
            finishTest();
          }
        }, TestSerializationRPCService.class).testLong(expected);
      }
    });
  }

  public void testConstructorAndMethodMappedLong() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final EntityWithConstructorAndMethodMappedLong expected =
                EntityWithConstructorAndMethodMappedLong.instanceFor(Long.MAX_VALUE - 1);

        MessageBuilder.createCall(new RemoteCallback<EntityWithConstructorAndMethodMappedLong>() {
          @Override
          public void callback(EntityWithConstructorAndMethodMappedLong response) {
            assertEquals(expected.toString(), response.toString());
            finishTest();
          }
        }, TestSerializationRPCService.class).testConstructorAndMethodMappedLong(expected);
      }
    });
  }

  public void testDouble() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final double expected = Double.MAX_VALUE;

        MessageBuilder.createCall(new RemoteCallback<Double>() {
          @Override
          public void callback(Double response) {
            assertEquals(expected, response.doubleValue());
            finishTest();
          }
        }, TestSerializationRPCService.class).testDouble(expected);
      }
    });
  }

  public void testDoubleNegInf() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final double expected = Double.NEGATIVE_INFINITY;

        MessageBuilder.createCall(new RemoteCallback<Double>() {
          @Override
          public void callback(Double response) {
            assertTrue(response.isInfinite());
            assertTrue(response < 0);
            finishTest();
          }
        }, TestSerializationRPCService.class).testDouble(expected);
      }
    });
  }

  public void testFloat() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final float expected = Float.MAX_VALUE;

        MessageBuilder.createCall(new RemoteCallback<Float>() {
          @Override
          public void callback(Float response) {
            assertEquals(expected, response.floatValue());
            finishTest();
          }
        }, TestSerializationRPCService.class).testFloat(expected);
      }
    });
  }

  public void testFloatNegInf() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final float expected = Float.NEGATIVE_INFINITY;

        MessageBuilder.createCall(new RemoteCallback<Float>() {
          @Override
          public void callback(Float response) {
            assertTrue(response.isInfinite());
            assertTrue(response < 0);
            finishTest();
          }
        }, TestSerializationRPCService.class).testFloat(expected);
      }
    });
  }

  public void testShort() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final short expected = Short.MAX_VALUE;

        MessageBuilder.createCall(new RemoteCallback<Short>() {
          @Override
          public void callback(Short response) {
            assertEquals(expected, response.shortValue());
            finishTest();
          }
        }, TestSerializationRPCService.class).testShort(expected);
      }
    });
  }

  public void testBoolean() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final boolean expected = Boolean.TRUE;

        MessageBuilder.createCall(new RemoteCallback<Boolean>() {
          @Override
          public void callback(Boolean response) {
            assertEquals(expected, response.booleanValue());
            finishTest();
          }
        }, TestSerializationRPCService.class).testBoolean(expected);
      }
    });
  }

  public void testCharacter() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final char expected = 'z';

        MessageBuilder.createCall(new RemoteCallback<Character>() {
          @Override
          public void callback(Character response) {
            assertEquals(expected, response.charValue());
            finishTest();
          }
        }, TestSerializationRPCService.class).testCharacter(expected);
      }
    });
  }

  public void testByte() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final byte expected = (byte) 100;

        MessageBuilder.createCall(new RemoteCallback<Byte>() {
          @Override
          public void callback(Byte response) {
            assertEquals(expected, response.byteValue());
            finishTest();
          }
        }, TestSerializationRPCService.class).testByte(expected);
      }
    });
  }

  /**
   * Formats a failure message of the form "expected: <i>expect</i>; but was: <i>got</i>". Does not
   * cause a test failure. You still have to call Assert.fail() if you want that.
   *
   * @param expect
   *          The expected value.
   * @param got
   *          The actual value.
   *
   * @return A new String as described above.
   */
  private static String failMessage(Object expect, Object got) {
    return "expected: " + expect + "; but was: " + got;
  }

  private static final void assertStringArrayEqual(String[] a, String[] b) {
    if (a == null || b == null) {
      assertTrue(failMessage(a, b), a == null && b == null);
    }
    else if (a.length != b.length) {
      fail(failMessage(a, b));
    }
    else {
      for (int i = 0; i < a.length; i++) {
        assertEquals(a[i], b[i]);
      }
    }
  }

  private static final void assertIntArrayEqual(int[] a, int[] b) {
    if (a == null || b == null) {
      assertTrue(failMessage(a, b), a == null && b == null);
    }
    else if (a.length != b.length) {
      fail(failMessage(a, b));
    }
    else {
      for (int i = 0; i < a.length; i++) {
        assertEquals(a[i], b[i]);
      }
    }
  }

  private static final void assertLongArrayEqual(long[] a, long[] b) {
    if (a == null || b == null) {
      assertTrue(failMessage(a, b), a == null && b == null);
    }
    else if (a.length != b.length) {
      fail(failMessage(a, b));
    }
    else {
      for (int i = 0; i < a.length; i++) {
        assertEquals(a[i], b[i]);
      }
    }
  }

  private static final void assertDoubleArrayEqual(double[] a, double[] b) {
    if (a == null || b == null) {
      assertTrue(failMessage(a, b), a == null && b == null);
    }
    else if (a.length != b.length) {
      fail(failMessage(a, b));
    }
    else {
      for (int i = 0; i < a.length; i++) {
        assertEquals(a[i], b[i]);
      }
    }
  }

  private static final void assertFloatArrayEqual(float[] a, float[] b) {
    if (a == null || b == null) {
      assertTrue(failMessage(a, b), a == null && b == null);
    }
    else if (a.length != b.length) {
      fail(failMessage(a, b));
    }
    else {
      for (int i = 0; i < a.length; i++) {
        assertEquals(a[i], b[i]);
      }
    }
  }

  private static final void assertShortArrayEqual(short[] a, short[] b) {
    if (a == null || b == null) {
      assertTrue(failMessage(a, b), a == null && b == null);
    }
    else if (a.length != b.length) {
      fail(failMessage(a, b));
    }
    else {
      for (int i = 0; i < a.length; i++) {
        assertEquals(a[i], b[i]);
      }
    }
  }

  private static final void assertBooleanArrayEqual(boolean[] a, boolean[] b) {
    if (a == null || b == null) {
      assertTrue(failMessage(a, b), a == null && b == null);
    }
    else if (a.length != b.length) {
      fail(failMessage(a, b));
    }
    else {
      for (int i = 0; i < a.length; i++) {
        assertEquals(a[i], b[i]);
      }
    }
  }

  private static final void assertByteArrayEqual(byte[] a, byte[] b) {
    if (a == null || b == null) {
      assertTrue(failMessage(a, b), a == null && b == null);
    }
    else if (a.length != b.length) {
      fail(failMessage(a, b));
    }
    else {
      for (int i = 0; i < a.length; i++) {
        assertEquals(a[i], b[i]);
      }
    }
  }

  private static final void assertCharArrayEqual(char[] a, char[] b) {
    if (a == null || b == null) {
      assertTrue(failMessage(a, b), a == null && b == null);
    }
    else if (a.length != b.length) {
      fail(failMessage(a, b));
    }
    else {
      for (int i = 0; i < a.length; i++) {
        assertEquals(a[i], b[i]);
      }
    }
  }

  /**
   * array tests *
   */

  public void testStringArray() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final String[] expected = { "This is a test string", "And so is this" };

        MessageBuilder.createCall(new RemoteCallback<String[]>() {
          @Override
          public void callback(String[] response) {
            assertStringArrayEqual(expected, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testStringArray(expected);
      }
    });
  }

  public void testIntegerArray() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final int[] expected = { Integer.MIN_VALUE, Integer.MAX_VALUE };

        MessageBuilder.createCall(new RemoteCallback<int[]>() {
          @Override
          public void callback(int[] response) {
            assertIntArrayEqual(expected, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testIntegerArray(expected);
      }
    });
  }

  public void testLongArray() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final long[] expected = { Long.MIN_VALUE, Long.MAX_VALUE };

        MessageBuilder.createCall(new RemoteCallback<long[]>() {
          @Override
          public void callback(long[] response) {
            assertLongArrayEqual(expected, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testLongArray(expected);
      }
    });
  }

  public void testDoubleArray() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final double[] expected = { Double.MAX_VALUE, Double.MAX_VALUE };

        MessageBuilder.createCall(new RemoteCallback<double[]>() {
          @Override
          public void callback(double[] response) {
            assertDoubleArrayEqual(expected, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testDoubleArray(expected);
      }
    });
  }

  public void testFloatArray() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final float[] expected = { Float.MIN_VALUE, Float.MAX_VALUE };

        MessageBuilder.createCall(new RemoteCallback<float[]>() {
          @Override
          public void callback(float[] response) {
            assertFloatArrayEqual(expected, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testFloatArray(expected);
      }
    });
  }

  public void testShortArray() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final short[] expected = { Short.MIN_VALUE, Short.MAX_VALUE };

        MessageBuilder.createCall(new RemoteCallback<short[]>() {
          @Override
          public void callback(short[] response) {
            assertShortArrayEqual(expected, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testShortArray(expected);
      }
    });
  }

  public void testBooleanArray() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final boolean[] expected = { Boolean.FALSE, Boolean.TRUE };

        MessageBuilder.createCall(new RemoteCallback<boolean[]>() {
          @Override
          public void callback(boolean[] response) {
            assertBooleanArrayEqual(expected, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testBooleanArray(expected);
      }
    });
  }

  public void testCharacterArray() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final char[] expected = { 'a', 'z' };

        MessageBuilder.createCall(new RemoteCallback<char[]>() {
          @Override
          public void callback(char[] response) {
            assertCharArrayEqual(expected, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testCharacterArray(expected);
      }
    });
  }

  public void testByteArray() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final byte[] expected = { (byte) -100, (byte) 100 };

        MessageBuilder.createCall(new RemoteCallback<byte[]>() {
          @Override
          public void callback(byte[] response) {
            assertByteArrayEqual(expected, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testByteArray(expected);
      }
    });
  }

  /**
   * This test is disabled because it demonstrates a known limitation of Errai Marshalling. See
   * ERRAI-339 and ERRAI-341 for details.
   */
  public void testPortableArray() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final NeverDeclareAnArrayOfThisType[] expected = { new NeverDeclareAnArrayOfThisType() };

        MessageBuilder.createCall(new RemoteCallback<NeverDeclareAnArrayOfThisType[]>() {
          @Override
          public void callback(NeverDeclareAnArrayOfThisType[] response) {
            assertNotNull(response);
            assertEquals(1, response.length);
            assertNotNull(response[0]);
            finishTest();
          }
        }, TestSerializationRPCService.class).testPortableArray(expected);
      }
    });
  }

  public void testEntitySerialization() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final List<TreeNodeContainer> testList = new ArrayList<TreeNodeContainer>();
        testList.add(new TreeNodeContainer(10, "Foo\\", 0));
        testList.add(new TreeNodeContainer(15, "Bar", 10));
        testList.add(new StudyTreeNodeContainer(20, "Foobie", 15, 100));
        // test for correct serialization of null elements
        testList.add(null);

        MessageBuilder.createCall(new RemoteCallback<List<TreeNodeContainer>>() {
          @Override
          public void callback(List<TreeNodeContainer> response) {
            assertEquals(4, response.size());
            assertEquals(response, testList);

            finishTest();
          }
        }, TestSerializationRPCService.class).acceptTreeNodeContainers(testList);
      }
    });
  }

  public void testLongInCollection() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final List<Long> list = new ArrayList<Long>();

        list.add(10L);
        list.add(15L);
        list.add(20L);
        list.add(25L);
        list.add(30L);

        MessageBuilder.createCall(new RemoteCallback<List<Long>>() {
          @Override
          public void callback(List<Long> response) {
            assertEquals(list, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).listOfLong(list);
      }
    });
  }

  public void testIntegerInCollection() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final List<Integer> list = new ArrayList<Integer>();

        list.add(10);
        list.add(15);
        list.add(20);
        list.add(25);
        list.add(30);

        MessageBuilder.createCall(new RemoteCallback<List<Integer>>() {
          @Override
          public void callback(List<Integer> response) {
            assertEquals(list, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).listOfInteger(list);
      }
    });
  }

  public void testFloatInCollection() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final List<Float> list = new ArrayList<Float>();

        list.add(10.1f);
        list.add(15.12f);
        list.add(20.123f);
        list.add(25.1234f);
        list.add(30.12345f);

        MessageBuilder.createCall(new RemoteCallback<List<Float>>() {
          @Override
          public void callback(List<Float> response) {
            assertEquals(list, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).listOfFloat(list);
      }
    });
  }

  public void testShortInCollection() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final List<Short> list = new ArrayList<Short>();

        list.add((short) 10);
        list.add((short) 20);
        list.add((short) 30);
        list.add((short) 40);
        list.add((short) 50);

        MessageBuilder.createCall(new RemoteCallback<List<Float>>() {
          @Override
          public void callback(List<Float> response) {
            assertEquals(list, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).listOfShort(list);
      }
    });
  }

  public void testByteInCollection() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final List<Byte> list = new ArrayList<Byte>();

        list.add((byte) 10);
        list.add((byte) 20);
        list.add((byte) 30);
        list.add((byte) 40);
        list.add((byte) 50);

        MessageBuilder.createCall(new RemoteCallback<List<Byte>>() {
          @Override
          public void callback(List<Byte> response) {
            assertEquals(list, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).listOfByte(list);
      }
    });
  }

  public void testBooleanInCollection() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final List<Boolean> list = new ArrayList<Boolean>();

        list.add(true);
        list.add(true);
        list.add(false);
        list.add(false);
        list.add(true);

        MessageBuilder.createCall(new RemoteCallback<List<Float>>() {
          @Override
          public void callback(List<Float> response) {
            assertEquals(list, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).listOfBoolean(list);
      }
    });
  }

  public void testSet() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final Set<String> set = new HashSet<String>();

        set.add("foo");
        set.add("bar");
        set.add("foobar");
        set.add("foobar\\foobar");

        MessageBuilder.createCall(new RemoteCallback<Set<String>>() {
          @Override
          public void callback(Set<String> response) {
            assertEquals(set, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).setOfStrings(set);
      }
    });
  }

  public void testCharacterInCollection() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final List<Character> list = new ArrayList<Character>();

        list.add('a');
        list.add('c');
        list.add('e');
        list.add('g');
        list.add('i');

        MessageBuilder.createCall(new RemoteCallback<List<Character>>() {
          @Override
          public void callback(List<Character> response) {
            assertEquals(list, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).listOfCharacters(list);
      }
    });
  }

  public void testMapOfLongToString() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final Map<Long, String> map = new HashMap<Long, String>();

        map.put(1l, "foo");
        map.put(2l, "bar");
        map.put(3l, "baz\\qux");
        map.put(4l, null);
        map.put(null, "zap");

        MessageBuilder.createCall(new RemoteCallback<Map<Long, String>>() {
          @Override
          public void callback(Map<Long, String> response) {
            assertEquals(map, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).mapOfLongToString(map);
      }
    });
  }

  public void testMapOfLongToListOfStrings() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final Map<Long, List<String>> map = new HashMap<Long, List<String>>();

        List<String> l1 = new ArrayList<String>();
        l1.add("foo");
        l1.add("bar");

        List<String> l2 = new ArrayList<String>();
        l2.add("baz");
        l2.add("qux");

        map.put(1l, l1);
        map.put(2l, l2);

        MessageBuilder.createCall(new RemoteCallback<Map<Long, List<String>>>() {
          @Override
          public void callback(Map<Long, List<String>> response) {
            assertEquals(map, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).mapOfLongToListOfStrings(map);
      }
    });
  }

  public void testMapOfStringToFloat() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final Map<String, Float> map = new HashMap<String, Float>();

        map.put("foo", 1.0f);
        map.put("bar", 1.1f);
        map.put("baz", 1.2f);

        MessageBuilder.createCall(new RemoteCallback<Map<String, Float>>() {
          @Override
          public void callback(Map<String, Float> response) {
            assertEquals(map, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).mapOfStringToFloat(map);
      }
    });
  }

  public void testMapOfStringToListOfDoubles() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final Map<String, List<Double>> map = new HashMap<String, List<Double>>();

        List<Double> l1 = new ArrayList<Double>();
        l1.add(1.0);
        l1.add(1.1);

        List<Double> l2 = new ArrayList<Double>();
        l2.add(1.2);
        l2.add(1.3);

        map.put("foo", l1);
        map.put("bar", l2);

        MessageBuilder.createCall(new RemoteCallback<Map<String, List<Double>>>() {
          @Override
          public void callback(Map<String, List<Double>> response) {
            assertEquals(map, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).mapOfStringToListOfDoubles(map);
      }
    });
  }

  public void testMapOfCustomTypes() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final Map<Group, Group> map = new HashMap<Group, Group>();

        map.put(new Group(1, "fooKey"), new Group(2, "fooVal"));
        map.put(new Group(3, "barKey"), new Group(4, "barVal"));

        MessageBuilder.createCall(new RemoteCallback<Map<Group, Group>>() {
          @Override
          public void callback(Map<Group, Group> response) {
            assertEquals(map, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).mapOfCustomTypes(map);
      }
    });
  }

  public void testMapOfListOfStringsToCustomType() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final Map<List<String>, Group> map = new HashMap<List<String>, Group>();

        List<String> l1 = new ArrayList<String>();
        l1.add("foo");
        l1.add("bar");

        List<String> l2 = new ArrayList<String>();
        l1.add("baz");
        l1.add("qux");

        map.put(l1, new Group(1, "fooGroup"));
        map.put(l2, new Group(2, "barGroup"));

        MessageBuilder.createCall(new RemoteCallback<Map<List<String>, Group>>() {
          @Override
          public void callback(Map<List<String>, Group> response) {
            assertEquals(map, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).mapOfListOfStringsToCustomType(map);
      }
    });
  }

  public void testLinkedHashMap() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final LinkedHashMap<String, Integer> map = new LinkedHashMap<String, Integer>();
        map.put("jonathan", 1);
        map.put("christian", 2);
        map.put("mike", 3);
        map.put("lincoln", 4);
        map.put("marius", 5);
        map.put("banana", 6);
        map.put("fruit", 7);
        map.put("peas", 8);
        map.put("hamburger", 9);
        map.put("durian", 10);

        MessageBuilder.createCall(new RemoteCallback<LinkedHashMap<String, Integer>>() {
          @Override
          public void callback(LinkedHashMap<String, Integer> response) {
            String compareTo = map.toString();
            String compareFrom = response.toString();

            assertEquals(compareTo, compareFrom);
            finishTest();
          }
        }, TestSerializationRPCService.class).testLinkedHashMap(map);
      }
    });
  }

  public void testLinkedHashSet() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final LinkedHashSet<String> set = new LinkedHashSet<String>();
        set.add("foo");
        set.add("bar");
        set.add("foobar");

        MessageBuilder.createCall(new RemoteCallback<LinkedHashSet>() {
          @Override
          public void callback(LinkedHashSet response) {
            String compareTo = set.toString();
            String compareFrom = response.toString();

            assertEquals(compareTo, compareFrom);

            finishTest();
          }
        }, TestSerializationRPCService.class).testLinkedHashSet(set);
      }
    });
  }

  public void testNestedClassSerialization() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final ClassWithNestedClass clazz = new ClassWithNestedClass();
        clazz.setNested(new ClassWithNestedClass.Nested("foo"));

        MessageBuilder.createCall(new RemoteCallback<ClassWithNestedClass>() {
          @Override
          public void callback(ClassWithNestedClass response) {
            assertEquals(clazz, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).nestedClass(clazz);
      }
    });
  }

  public void testEntityWithGenericCollections() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final EntityWithGenericCollections ent = new EntityWithGenericCollections();
        List<Float> listOffFloats = new ArrayList<Float>();
        listOffFloats.add(1.0f);
        listOffFloats.add(1.1f);
        listOffFloats.add(1.2f);

        List<String> listOfStrings = new ArrayList<String>();
        listOfStrings.add("str1");
        listOfStrings.add(null);
        listOfStrings.add("str2");

        ent.setObject(new Group());
        ent.setListOfFloats(listOffFloats);
        ent.setListWithLowerBoundWildcard(listOfStrings);

        MessageBuilder.createCall(new RemoteCallback<EntityWithGenericCollections>() {
          @Override
          public void callback(EntityWithGenericCollections response) {
            assertEquals(ent, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).genericCollections(ent);
      }
    });
  }

  public void testEmptyEntityWithGenericCollections() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final EntityWithGenericCollections ent = new EntityWithGenericCollections();

        MessageBuilder.createCall(new RemoteCallback<EntityWithGenericCollections>() {
          @Override
          public void callback(EntityWithGenericCollections response) {
            assertEquals(ent, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).genericCollections(ent);
      }
    });
  }

  public void testStringBufferAndStringBuilder() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final EntityWithStringBufferAndStringBuilder ent = new EntityWithStringBufferAndStringBuilder();
        ent.setStringBuffer(new StringBuffer("foo"));
        ent.setStringBuilder(new StringBuilder("bar"));

        MessageBuilder.createCall(new RemoteCallback<EntityWithStringBufferAndStringBuilder>() {
          @Override
          public void callback(EntityWithStringBufferAndStringBuilder response) {
            assertEquals(ent, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testStringBufferAndStringBuilder(ent);
      }
    });
  }

  public void testThrowable() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final Throwable c = new Throwable("bar");
        final Throwable t = new Throwable("foo", c);

        final StackTraceElement[] trace = new StackTraceElement[3];
        trace[0] = new StackTraceElement("DogClass", "bark", "DogoClass.java", 10);
        trace[1] = new StackTraceElement("KatClass", "meow", "KatClass.java", 43);
        trace[2] = new StackTraceElement("PigClass", "oink", "PigClass.java", 23);

        t.setStackTrace(trace);

        class EqualTester {
          public boolean isEqual(Throwable r) {
            if (r == null)
              return false;
            if (!r.getMessage().equals(t.getMessage()))
              return false;

            StackTraceElement[] st = r.getStackTrace();

            if (st == null || trace.length != st.length)
              return false;

            for (int i = 0; i < trace.length; i++) {
              if (!stackTraceEqual(trace[i], st[i]))
                return false;
            }

            return r.getCause() != null && c.getMessage().equals(r.getCause().getMessage());
          }

          private boolean stackTraceEqual(StackTraceElement el1, StackTraceElement el2) {
            return el1.getClassName().equals(el2.getClassName())
                && el1.getFileName().equals(el2.getFileName())
                && el1.getLineNumber() == el2.getLineNumber()
                && el1.getMethodName().equals(el2.getMethodName());
          }
        }

        MessageBuilder.createCall(new RemoteCallback<Throwable>() {
          @Override
          public void callback(Throwable response) {
            assertTrue(new EqualTester().isEqual(response));
            finishTest();
          }
        }, TestSerializationRPCService.class).testSerializeThrowable(t);
      }
    });
  }

  public void testAssertionError() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final AssertionError t = new AssertionError("foo");

        final StackTraceElement[] trace = new StackTraceElement[3];
        trace[0] = new StackTraceElement("DogClass", "bark", "DogoClass.java", 10);
        trace[1] = new StackTraceElement("KatClass", "meow", "KatClass.java", 43);
        trace[2] = new StackTraceElement("PigClass", "oink", "PigClass.java", 23);

        t.setStackTrace(trace);

        class EqualTester {
          public boolean isEqual(AssertionError r) {
            if (r == null)
              return false;
            if (r.getMessage() == null || !r.getMessage().equals(t.getMessage()))
              return false;

            StackTraceElement[] st = r.getStackTrace();

            if (st == null || trace.length != st.length)
              return false;

            for (int i = 0; i < trace.length; i++) {
              if (!stackTraceEqual(trace[i], st[i]))
                return false;
            }

            return true;
          }

          private boolean stackTraceEqual(StackTraceElement el1, StackTraceElement el2) {
            return el1.getClassName().equals(el2.getClassName())
                && el1.getFileName().equals(el2.getFileName())
                && el1.getLineNumber() == el2.getLineNumber()
                && el1.getMethodName().equals(el2.getMethodName());
          }
        }

        MessageBuilder.createCall(new RemoteCallback<AssertionError>() {
          @Override
          public void callback(AssertionError response) {
            assertTrue(new EqualTester().isEqual(response));
            finishTest();
          }
        }, TestSerializationRPCService.class).testSerializeAssertionError(t);
      }
    });
  }

  public void testFactorySerialization() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final FactoryEntity entity = FactoryEntity.create("foobar", 123);

        class EqualTester {
          public boolean isEqual(FactoryEntity r) {
            return r != null &&
                entity.getName().equals(r.getName()) &&
                entity.getAge() == r.getAge();
          }
        }

        MessageBuilder.createCall(new RemoteCallback<FactoryEntity>() {
          @Override
          public void callback(FactoryEntity response) {
            assertTrue(new EqualTester().isEqual(response));
            finishTest();
          }
        }, TestSerializationRPCService.class).testFactorySerialization(entity);
      }
    });
  }

  public void testBuilderSerializationWithPrivateConstructor() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final BuilderEntity entity = new BuilderEntity.Builder("foobar").age(123).build();

        MessageBuilder.createCall(new RemoteCallback<BuilderEntity>() {
          @Override
          public void callback(BuilderEntity response) {
            assertEquals("Failed to serialize entity with private constructor", entity, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testBuilderSerializationWithPrivateConstructor(entity);
      }
    });
  }

  public void testJavaUtilDate() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final java.util.Date d = new java.util.Date(System.currentTimeMillis());

        MessageBuilder.createCall(new RemoteCallback<java.util.Date>() {
          @Override
          public void callback(java.util.Date response) {
            assertEquals(d, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testJavaUtilDate(d);
      }
    });
  }

  public void testJavaSqlDate() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final java.sql.Date d = new java.sql.Date(System.currentTimeMillis());

        MessageBuilder.createCall(new RemoteCallback<java.sql.Date>() {
          @Override
          public void callback(java.sql.Date response) {
            assertEquals(d, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testJavaSqlDate(d);
      }
    });
  }

  public void testTimestampSerialization() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final Timestamp ts = new Timestamp(System.currentTimeMillis());

        MessageBuilder.createCall(new RemoteCallback<Timestamp>() {
          @Override
          public void callback(Timestamp response) {
            assertEquals(ts, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testTimestampSerialization(ts);
      }
    });
  }

  public void testTimeSerialization() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final Time ts = new Time(System.currentTimeMillis());

        MessageBuilder.createCall(new RemoteCallback<Time>() {
          @Override
          public void callback(Time response) {
            assertEquals(ts, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testTimeSerialization(ts);
      }
    });
  }

  public void testBigDecimalSerialization() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final BigDecimal bd = new BigDecimal(System.currentTimeMillis() * 1.04d);

        MessageBuilder.createCall(new RemoteCallback<BigDecimal>() {
          @Override
          public void callback(BigDecimal response) {
            assertEquals(bd, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testBigDecimalSerialization(bd);
      }
    });
  }

  public void testBigIntegerSerialization() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final BigInteger bi = new BigInteger(String.valueOf(System.currentTimeMillis()));

        MessageBuilder.createCall(new RemoteCallback<BigInteger>() {
          @Override
          public void callback(BigInteger response) {
            assertEquals(bi, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testBigIntegerSerialization(bi);
      }
    });
  }

  public void testQueueSerialization() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final Queue<String> queue = new LinkedList<String>();
        queue.add("test1");
        queue.add("test2");
        queue.add("test3");

        MessageBuilder.createCall(new RemoteCallback<Queue<String>>() {
          @Override
          public void callback(Queue<String> response) {
            assertEquals(queue, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testQueueSerialization(queue);
      }
    });
  }

  public void testPriorityQueueSerialization() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final Queue<String> queue = new PriorityQueue<String>();

        queue.add("test1");
        queue.add("test2");
        queue.add("test3");

        class EqualTester {
          public boolean isEqual(Queue<String> r) {
            if (r != null) {
              if (r.size() == queue.size()) {
                for (int i = 0; i < r.size(); i++) {
                  if (!r.poll().equals(queue.poll())) {
                    return false;
                  }
                }

              }
              return true;
            }
            return false;
          }
        }

        MessageBuilder.createCall(new RemoteCallback<Queue<String>>() {
          @Override
          public void callback(Queue<String> response) {
            assertTrue(new EqualTester().isEqual(response));
            finishTest();
          }
        }, TestSerializationRPCService.class).testQueueSerialization(queue);
      }
    });
  }

  public void testSortedMapSerialization() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final SortedMap<String, String> map = new TreeMap<String, String>();

        map.put("test1", "a");
        map.put("test2", "b");
        map.put("test3", "c");

        class EqualTester {
          public boolean isEqual(SortedMap<String, String> r) {
            if (r != null) {
              if (r.size() == map.size()) {
                if (!r.firstKey().equals(map.firstKey())) {
                  return false;
                }
                else if (!r.lastKey().equals(map.lastKey())) {
                  return false;
                }
              }

              return true;
            }
            return false;

          }
        }

        MessageBuilder.createCall(new RemoteCallback<SortedMap<String, String>>() {
          @Override
          public void callback(SortedMap<String, String> response) {
            assertTrue(new EqualTester().isEqual(response));
            finishTest();
          }
        }, TestSerializationRPCService.class).testSortedMapSerialization(map);
      }
    });
  }

  public void testSortedSetSerialization() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final SortedSet<String> set = new TreeSet<String>();

        set.add("test1");
        set.add("test2");
        set.add("test3");

        MessageBuilder.createCall(new RemoteCallback<SortedSet<String>>() {
          @Override
          public void callback(SortedSet<String> response) {
            assertEquals(set, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testSortedSetSerialization(set);
      }
    });
  }

  @SuppressWarnings("unchecked")
  public void testInheritedDefinitionFromExistingParent() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final CustomList customList = new CustomList();

        customList.add("test1");
        customList.add("test2");
        customList.add("test3");

        MessageBuilder.createCall(new RemoteCallback<List>() {
          @Override
          public void callback(List response) {
            assertEquals(customList, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testInheritedDefinitionFromExistingParent(customList);
      }
    });
  }

  public void testAliasedMarshaller() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final SubMoron subMoron = new SubMoron("ABCDEFG");
        subMoron.setDumbFieldThatShouldntBeMarshalled("Hello, There!");

        MessageBuilder.createCall(new RemoteCallback<SubMoron>() {
          @Override
          public void callback(SubMoron response) {
            assertEquals(subMoron.getValue(), response.getValue());
            assertNull(response.getDumbFieldThatShouldntBeMarshalled());

            finishTest();

          }
        }, TestSerializationRPCService.class).testSubMoron(subMoron);
      }
    });
  }

  public void testNakedEnum() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final TestEnumA e = TestEnumA.Christian;

        MessageBuilder.createCall(new RemoteCallback<TestEnumA>() {
          @Override
          public void callback(TestEnumA response) {
            assertEquals(e, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testNakedEnum(e);
      }
    });
  }

  public void testImplicitEnum() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final ImplicitEnum e = ImplicitEnum.LOU;

        MessageBuilder.createCall(new RemoteCallback<ImplicitEnum>() {
          @Override
          public void callback(ImplicitEnum response) {
            assertEquals(e, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testImplicitEnum(e);
      }
    });
  }

  public void testBoron() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final Boron.Bean boron = new Boron.Bean();

        MessageBuilder.createCall(new RemoteCallback<Boron.Bean>() {
          @Override
          public void callback(Boron.Bean response) {
            assertEquals(boron, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testPortableInnerClass(boron);
      }
    });
  }

  public void testKoron() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final Koron koron = new Koron();

        MessageBuilder.createCall(new RemoteCallback<Koron>() {
          @Override
          public void callback(Koron response) {
            assertEquals(koron, response);
            assertSame("someList is different from sameList", response.getSomeList(), response.getSameList());
            assertNotSame("otherList is not different from someList", response.getSomeList(), response.getOtherList());

            finishTest();
          }
        }, TestSerializationRPCService.class).testKoron(koron);
      }
    });
  }

  public void testMoron() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final TestingTickCache moron = new TestingTickCache(new LinkedList<TestingTick>());

        MessageBuilder.createCall(new RemoteCallback<TestingTickCache>() {
          @Override
          public void callback(TestingTickCache response) {
            assertEquals(moron, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testMoron(moron);
      }
    });
  }

  public void testEntityWithUnqualifiedFields() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final EntityWithUnqualifiedFields entity = new EntityWithUnqualifiedFields();
        entity.setField1("foo");
        entity.setField2(123);

        MessageBuilder.createCall(new RemoteCallback<EntityWithUnqualifiedFields>() {
          @Override
          public void callback(EntityWithUnqualifiedFields response) {
            assertEquals(entity, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testEntityWithUnqualifiedFieldTypes(entity);
      }
    });
  }

  public void testEntityWithGoodParts() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final EntityWithGoodParts entity = new EntityWithGoodParts();
        entity.setDoubleField(Double.NaN);
        entity.setBadDoubles(new Double[] { 1234d, Double.NEGATIVE_INFINITY, 12345d, null, Double.POSITIVE_INFINITY,
            Double.NaN });
        entity.setBadPrimitiveDoubles(new double[] { 1234d, Double.NEGATIVE_INFINITY, 12345d, Double.POSITIVE_INFINITY,
            Double.NaN });

        entity.setFloatField(Float.NaN);
        entity.setBadFloats(new Float[] { 1234.0f, Float.NEGATIVE_INFINITY, 12345.123f, null, Float.POSITIVE_INFINITY,
            Float.NaN });

        MessageBuilder.createCall(new RemoteCallback<EntityWithGoodParts>() {
          @Override
          public void callback(EntityWithGoodParts response) {
            assertEquals(entity.toString(), response.toString());
            finishTest();
          }
        }, TestSerializationRPCService.class).testEntityWithGoodParts(entity);
      }
    });
  }

  public void testGenericEntity() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final GenericEntity<String> entity = new GenericEntity<String>("foo");

        List<String> groups = new ArrayList<String>();
        groups.add("bar");
        groups.add("baz");
        entity.setList(groups);

        MessageBuilder.createCall(new RemoteCallback<GenericEntity<String>>() {
          @Override
          public void callback(GenericEntity<String> response) {
            assertEquals(entity, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testGenericEntity(entity);
      }
    });
  }

  public void testGenericEntityWithConstructorMapping() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        List<String> data = new ArrayList<String>();
        data.add("bar");
        data.add("baz");

        final GenericEntityWithConstructorMapping<String> entity =
            new GenericEntityWithConstructorMapping<String>(1l, data);

        MessageBuilder.createCall(new RemoteCallback<GenericEntityWithConstructorMapping<String>>() {
          @Override
          public void callback(GenericEntityWithConstructorMapping<String> response) {
            assertEquals(entity, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testGenericEntityWithConstructorMapping(entity);
      }
    });
  }

  public void testGenericEntitySubtypeInteger() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final GenericEntitySubtypeInteger entity = new GenericEntitySubtypeInteger();
        entity.setField(12);
        entity.setList(Arrays.asList(1, 2, 3, 4));

        MessageBuilder.createCall(new RemoteCallback<GenericEntitySubtypeInteger>() {
          @Override
          public void callback(GenericEntitySubtypeInteger response) {
            assertEquals(entity, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testGenericEntitySubtypeInteger(entity);
      }
    });
  }

  public void testGenericEntitySubtypeString() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final GenericEntitySubtypeString entity = new GenericEntitySubtypeString();
        entity.setField("12");
        entity.setList(Arrays.asList("1", "2", "3", "4"));

        MessageBuilder.createCall(new RemoteCallback<GenericEntitySubtypeString>() {
          @Override
          public void callback(GenericEntitySubtypeString response) {
            assertEquals(entity, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testGenericEntitySubtypeString(entity);
      }
    });
  }

  public void testGenericEntityUsingList() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        List<Group> groups = new ArrayList<Group>();
        groups.add(new Group(1, "foo"));
        groups.add(new Group(2, "bar"));

        final GenericEntity<List<Group>> entity = new GenericEntity<List<Group>>(groups);

        MessageBuilder.createCall(new RemoteCallback<GenericEntity<List<Group>>>() {
          @Override
          public void callback(GenericEntity<List<Group>> response) {
            assertEquals(entity, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testGenericEntity(entity);
      }
    });
  }

  public void testGenericEntityUsingSet() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        Set<Group> groups = new HashSet<Group>();
        groups.add(new Group(1, "foo"));
        groups.add(new Group(2, "bar"));

        final GenericEntity<Set<Group>> entity = new GenericEntity<Set<Group>>(groups);

        MessageBuilder.createCall(new RemoteCallback<GenericEntity<Set<Group>>>() {
          @Override
          public void callback(GenericEntity<Set<Group>> response) {
            assertEquals(entity, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testGenericEntity(entity);
      }
    });
  }

  public void testGenericEntityUsingStack() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        Stack<Group> stack = new Stack<Group>();
        stack.add(new Group(1, "foo"));
        stack.add(new Group(2, "bar"));

        final GenericEntity<Stack<Group>> entity = new GenericEntity<Stack<Group>>(stack);

        MessageBuilder.createCall(new RemoteCallback<GenericEntity<Set<Group>>>() {
          @Override
          public void callback(GenericEntity<Set<Group>> response) {
            assertEquals(entity, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testGenericEntity(entity);
      }
    });
  }

  public void testEntityWithSuperClassField() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        Student student = new Student(1, "smartStudent");
        final EntityWithSuperClassField entity = new EntityWithSuperClassField(student);

        MessageBuilder.createCall(new RemoteCallback<EntityWithSuperClassField>() {
          @Override
          public void callback(EntityWithSuperClassField response) {
            assertEquals(entity, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testEntityWithSuperClassField(entity);
      }
    });
  }

  public void testEntityWithNullField() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final User u = new User();
        u.setId(1);
        u.setName(null);

        MessageBuilder.createCall(new RemoteCallback<User>() {
          @Override
          public void callback(User response) {
            assertEquals(u, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testEntityWithNullField(u);
      }
    });
  }

  // Serves as regression test for ERRAI-389
  public void testEntityWithPublicSuperTypeField() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final User u = new User();

        MessageBuilder.createCall(new RemoteCallback<User>() {
          @Override
          public void callback(User response) {
            assertEquals(u.publicSuperField, "publicSuperField");
            finishTest();
          }
        }, TestSerializationRPCService.class).testEntityWithNullField(u);
      }
    });
  }

  public void testImmutableEntityWithEnum() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final ImmutableEnumContainer entity = new ImmutableEnumContainer(TestEnumA.Christian);

        MessageBuilder.createCall(new RemoteCallback<ImmutableEnumContainer>() {
          @Override
          public void callback(ImmutableEnumContainer response) {
            assertEquals(entity, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testImmutableEntityWithEnum(entity);
      }
    });
  }

  public void testImmutableEntityWithEnumAndNullValue() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final ImmutableEnumContainer entity = new ImmutableEnumContainer(null);

        MessageBuilder.createCall(new RemoteCallback<ImmutableEnumContainer>() {
          @Override
          public void callback(ImmutableEnumContainer response) {
            assertEquals(entity, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testImmutableEntityWithEnum(entity);
      }
    });
  }

  public void testEntityWithEnumContainerContainer() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        // we will nest this into "ecc"
        final EnumContainer ec = new EnumContainer();
        ec.setEnumA1(TestEnumA.Jonathan);
        ec.setStatefulEnum1(EnumWithState.THING1);
        ec.setStatefulEnum2(EnumWithState.THING1);

        // this is the object we'll be transmitting. it contains "ec"
        final EnumContainerContainer ecc = new EnumContainerContainer();
        ecc.setEnumA(TestEnumA.Jonathan);
        ecc.setEnumContainer(ec);
        ecc.setEnumWithAbstractMethod(EnumWithAbstractMethod.THING1);

        MessageBuilder.createCall(new RemoteCallback<EnumContainerContainer>() {
          @Override
          public void callback(EnumContainerContainer response) {
            assertEquals(ecc.toString(), response.toString());
            finishTest();
          }
        }, TestSerializationRPCService.class).testEntityWithEnumContainerContainer(ecc);
      }
    });
  }

  // Serves as regression test for ERRAI-403
  public void testEntityWithMapUsingAbstractValueType() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final EntityWithMapUsingAbstractValueType e = new EntityWithMapUsingAbstractValueType();

        final Map<String, AbstractClassA> data = new HashMap<String, AbstractClassA>();
        data.put("1", new AImpl1(4711));
        data.put("2", new AImpl2("4711"));
        e.setData(data);

        MessageBuilder.createCall(new RemoteCallback<EntityWithMapUsingAbstractValueType>() {
          @Override
          public void callback(EntityWithMapUsingAbstractValueType response) {
            assertEquals(e, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testEntityWithMapUsingAbstractValueType(e);
      }
    });
  }

  // Serves as regression test for ERRAI-403
  public void testEntityWithMapUsingAbstractKeyType() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final EntityWithMapUsingAbstractKeyType e = new EntityWithMapUsingAbstractKeyType();

        final Map<AbstractClassA, String> data = new HashMap<AbstractClassA, String>();
        data.put(new AImpl1(4711), "1");
        data.put(new AImpl2("4711"), "2");
        e.setData(data);

        MessageBuilder.createCall(new RemoteCallback<EntityWithMapUsingAbstractKeyType>() {
          @Override
          public void callback(EntityWithMapUsingAbstractKeyType response) {
            assertEquals(e, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testEntityWithMapUsingAbstractKeyType(e);
      }
    });
  }

  public void testEntityWithMapUsingSubtypeValues() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final Map<String, Person> map = new HashMap<String, Person>();
        map.put("1", new Student(1, "student"));
        map.put("2", new User(2, "user"));

        final EntityWithMapUsingSubtypeValues e = new EntityWithMapUsingSubtypeValues();
        e.setData(map);

        MessageBuilder.createCall(new RemoteCallback<EntityWithMapUsingSubtypeValues>() {
          @Override
          public void callback(EntityWithMapUsingSubtypeValues response) {
            assertEquals(e, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testEntityWithMapUsingSubtypeValues(e);
      }
    });
  }

  /**
   * Serves as a regressions test for ERRAI-463, see also https://community.jboss.org/thread/215933
   */
  public void testEntityWithTypesUsingNestedParamTypes() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final EntityWithTypesUsingNestedParameterizedTypes e = new EntityWithTypesUsingNestedParameterizedTypes();
        Map<String, String> deTranslations = new HashMap<String, String>();
        deTranslations.put("hello", "Hallo");
        deTranslations.put("one", "Eins");

        Map<String, String> enTranslations = new HashMap<String, String>();
        enTranslations.put("hello", "Hello");
        enTranslations.put("one", "One");

        Map<String, Map<String, String>> allTranslations = new HashMap<String, Map<String, String>>();
        allTranslations.put("de", deTranslations);
        allTranslations.put("en", enTranslations);
        e.setMap(allTranslations);

        // regression test for ERRAI-565
        Map<Long, Map<Object, String>> multiTypeMap = new HashMap<Long, Map<Object, String>>();
        Map<Object, String> entry123 = new HashMap<Object, String>();
        entry123.put("this is a key", "this is a value");
        multiTypeMap.put(123L, entry123);
        Map<Object, String> entry456 = new HashMap<Object, String>();
        entry456.put("this is another key", "this is another value");
        multiTypeMap.put(456L, entry456);
        e.setMapWithDifferentTypes(multiTypeMap);

        List<List<Integer>> list = new ArrayList<List<Integer>>();
        list.add(new ArrayList<Integer>(Arrays.asList(1, 2, null)));
        list.add(new ArrayList<Integer>(Arrays.asList(3, 4, null)));
        e.setList(list);

        MessageBuilder.createCall(new RemoteCallback<EntityWithTypesUsingNestedParameterizedTypes>() {
          @Override
          public void callback(EntityWithTypesUsingNestedParameterizedTypes response) {
            assertEquals(e, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testEntityWithTypesUsingNestedParamTypes(e);
      }
    });
  }

  public void testEntityWithInterfaceField() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final EntityWithInterfaceField ent = new EntityWithInterfaceField();
        ent.setField(new SubInterfaceImpl("value"));

        MessageBuilder.createCall(new RemoteCallback<EntityWithInterfaceField>() {
          @Override
          public void callback(EntityWithInterfaceField response) {
            assertEquals(ent, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testEntityWithInterfaceField(ent);
      }
    });
  }

  public void testEntityWithInterfaceArrayField() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final EntityWithInterfaceArrayField ent = new EntityWithInterfaceArrayField();
        ent.setArrayField(new SubInterface[] { new SubInterfaceImpl("value0"), null, new SubInterfaceImpl("value2") });

        MessageBuilder.createCall(new RemoteCallback<EntityWithInterfaceArrayField>() {
          @Override
          public void callback(EntityWithInterfaceArrayField response) {
            assertEquals(ent, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testEntityWithInterfaceArrayField(ent);
      }
    });
  }

  public void testImmutableEntityWithArray() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final ImmutableArrayContainer entity = new ImmutableArrayContainer(new String[] { "1", "2" });

        MessageBuilder.createCall(new RemoteCallback<ImmutableArrayContainer>() {
          @Override
          public void callback(ImmutableArrayContainer response) {
            assertEquals(entity, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testImmutableEntityWithArray(entity);
      }
    });
  }

  public void testCollectionWithInheritedTypeVariable() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final EntityWithInheritedTypeVariable<String> entity = new EntityWithInheritedTypeVariable<String>();
        entity.setList(new ArrayList<String>(Arrays.asList("one", "two", null)));

        MessageBuilder.createCall(new RemoteCallback<EntityWithInheritedTypeVariable<String>>() {
          @Override
          public void callback(EntityWithInheritedTypeVariable<String> response) {
            assertEquals(entity, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testEntityWithInheritedTypeVariable(entity);
      }
    });
  }

  public void testEntityWithUnderscore_InClassName() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final EntityWithUnderscore_InClassName entity = new EntityWithUnderscore_InClassName();
        entity.setText("foo");

        MessageBuilder.createCall(new RemoteCallback<EntityWithUnderscore_InClassName>() {
          @Override
          public void callback(EntityWithUnderscore_InClassName response) {
            assertEquals(entity, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testEntityWithUnderscore_InClassName(entity);
      }
    });
  }

  public void testEntityWithMixedMappingTypes() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final EntityWithMixedMappingTypes entity = new EntityWithMixedMappingTypes("f", "c", "m");

        MessageBuilder.createCall(new RemoteCallback<EntityWithMixedMappingTypes>() {
          @Override
          public void callback(EntityWithMixedMappingTypes response) {
            assertEquals("f", response.getFieldInjected());
            assertEquals("c", response.getConstructorInjected());
            assertEquals("m", response.getMethodInjected());
            assertFalse(response.wasNonMappingConstructorCalled());
            assertTrue(response.wasMappingConstructorCalled());
            assertTrue(response.wasSetterMethodCalled());
            finishTest();
          }
        }, TestSerializationRPCService.class).testEntityWithMixedMappingTypes(entity);
      }
    });
  }

  public void testEntityWithFactoryMethodAndMixedMappingTypes() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final EntityWithFactoryMethodAndMixedMappingTypes entity = new EntityWithFactoryMethodAndMixedMappingTypes("field", "factory", "method");

        MessageBuilder.createCall(new RemoteCallback<EntityWithFactoryMethodAndMixedMappingTypes>() {
          @Override
          public void callback(EntityWithFactoryMethodAndMixedMappingTypes response) {
            assertEquals("field", response.getFieldInjected());
            assertEquals("factory", response.getFactoryMethodInjected());
            assertEquals("method", response.getMethodInjected());
            assertFalse(response.wasNonMappingConstructorCalled());
            assertTrue(response.wasFactoryMethodCalled());
            assertTrue(response.wasSetterMethodCalled());
            finishTest();
          }
        }, TestSerializationRPCService.class).testEntityWithFactoryMethodAndMixedMappingTypes(entity);
      }
    });
  }

  public void testInheritedBuiltInMappings() {
    assertNotNull(Marshalling.getMarshaller(EmptyStackException.class));
  }
  
  // This is a regression test for ERRAI-794
  public void testBackReferenceOrderingWithMapsTo() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final Outer.Nested key = new Outer.Nested("exp");
        final Outer outer = new Outer (Arrays.asList(key), key);

        MessageBuilder.createCall(new RemoteCallback<Outer>() {
          @Override
          public void callback(Outer response) {
            assertEquals(outer, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testBackReferenceOrderingWithMapsTo(outer);
      }
    });
  }
  
  // This is a regression test for ERRAI-794
  public void testBackReferenceOrderingWithMapsToInverted() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final Outer2.Nested key = new Outer2.Nested("exp");
        final Outer2 outer = new Outer2(key, Arrays.asList(key));

        MessageBuilder.createCall(new RemoteCallback<Outer2>() {
          @Override
          public void callback(Outer2 response) {
            assertEquals(outer, response);
            finishTest();
          }
        }, TestSerializationRPCService.class).testBackReferenceOrderingWithMapsToInverted(outer);
      }
    });
  }
  
  public void testIncrediblyGenericRpcMethod() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        GenericEntity<Student> arg = new GenericEntity<Student> ();
        arg.setField(new Student(1, "smart"));
        
        MessageBuilder.createCall(new RemoteCallback<Student>() {
          @Override
          public void callback(Student response) {
            assertEquals("smarter", response.getName());
            finishTest();
          }
        }, TestSerializationRPCService.class).testIncrediblyGenericRpcMethod(arg);
      }
    });
  }
}
