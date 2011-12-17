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

package org.jboss.errai.bus.client.tests;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.tests.support.*;

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

  public void testEntitySerialization() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        List<TreeNodeContainer> testList = new ArrayList<TreeNodeContainer>();
        testList.add(new TreeNodeContainer(10, "Foo\\", 0));
        testList.add(new TreeNodeContainer(15, "Bar", 10));
        testList.add(new StudyTreeNodeContainer(20, "Foobie", 15, 100));

        MessageBuilder.createCall(new RemoteCallback<List<TreeNodeContainer>>() {
          @Override
          public void callback(List<TreeNodeContainer> response) {
            if (response.size() == 3) {
              for (TreeNodeContainer tc : response) {
                System.out.println(tc);
              }

              finishTest();
            }
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
            try {
              assertEquals(list, response);
              finishTest();
            }
            catch (Throwable e) {
              e.printStackTrace();
              fail();
            }
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
            try {
              assertEquals(list, response);
              finishTest();
            }
            catch (Throwable e) {
              e.printStackTrace();
              fail();
            }
          }
        }, TestSerializationRPCService.class).listOfShort(list);
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
            try {
              assertEquals(list, response);
              finishTest();
            }
            catch (Throwable e) {
              e.printStackTrace();
              fail();
            }
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
            try {
              assertEquals(set, response);
              finishTest();
            }
            catch (Throwable e) {
              e.printStackTrace();
              fail();
            }
          }
        }, TestSerializationRPCService.class).setOfStrings(set);
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
        
        MessageBuilder.createCall(new RemoteCallback<Map<Long, String>>() {
          @Override
          public void callback(Map<Long, String> response) {
            try {
              assertEquals(map, response);
              finishTest();
            }
            catch (Throwable e) {
              e.printStackTrace();
              fail();
            }
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
            try {
              assertEquals(map, response);
              finishTest();
            }
            catch (Throwable e) {
              e.printStackTrace();
              fail();
            }
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
            try {
              assertEquals(map, response);
              finishTest();
            }
            catch (Throwable e) {
              e.printStackTrace();
              fail();
            }
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
            try {
              assertEquals(map, response);
              finishTest();
            }
            catch (Throwable e) {
              e.printStackTrace();
              fail();
            }
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
            try {
              assertEquals(map, response);
              finishTest();
            }
            catch (Throwable e) {
              e.printStackTrace();
              fail();
            }
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
            try {
              assertEquals(map, response);
              finishTest();
            }
            catch (Throwable e) {
              e.printStackTrace();
              fail();
            }
          }
        }, TestSerializationRPCService.class).mapOfListOfStringsToCustomType(map);
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
            try {
              assertEquals(clazz, response);
              finishTest();
            }
            catch (Throwable e) {
              e.printStackTrace();
              fail();
            }
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

        ent.setObject(new Group());
        ent.setListOfFloats(listOffFloats);
        MessageBuilder.createCall(new RemoteCallback<EntityWithGenericCollections>() {
          @Override
          public void callback(EntityWithGenericCollections response) {
            try {
              assertEquals(ent, response);
              finishTest();
            }
            catch (Throwable e) {
              e.printStackTrace();
              fail();
            }
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
            try {
              assertEquals(ent, response);
              finishTest();
            }
            catch (Throwable e) {
              e.printStackTrace();
              fail();
            }
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
            try {
              assertEquals(ent, response);
              finishTest();
            }
            catch (Throwable e) {
              e.printStackTrace();
              fail();
            }
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
            if (r == null) return false;
            if (!r.getMessage().equals(t.getMessage())) return false;

            StackTraceElement[] st = r.getStackTrace();

            if (st == null || trace.length != st.length) return false;

            for (int i = 0; i < trace.length; i++) {
              if (!stackTraceEqual(trace[i], st[i])) return false;
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
            try {
              assertTrue(new EqualTester().isEqual(response));
              finishTest();
            }
            catch (Throwable e) {
              e.printStackTrace();
              fail();
            }
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
            if (r == null) return false;
            if (r.getMessage() == null || !r.getMessage().equals(t.getMessage())) return false;

            StackTraceElement[] st = r.getStackTrace();

            if (st == null || trace.length != st.length) return false;

            for (int i = 0; i < trace.length; i++) {
              if (!stackTraceEqual(trace[i], st[i])) return false;
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
            try {
              assertTrue(new EqualTester().isEqual(response));
              finishTest();
            }
            catch (Throwable e) {
              e.printStackTrace();
              fail();
            }
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
            try {
              assertTrue(new EqualTester().isEqual(response));
              finishTest();
            }
            catch (Throwable e) {
              e.printStackTrace();
              fail();
            }
          }
        }, TestSerializationRPCService.class).testFactorySerialization(entity);
      }
    });
  }


  public void testTimestampSerialization() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final Timestamp ts = new Timestamp(System.currentTimeMillis());

        class EqualTester {
          public boolean isEqual(Timestamp r) {
            return r != null &&
                    r.equals(ts);
          }
        }

        MessageBuilder.createCall(new RemoteCallback<Timestamp>() {
          @Override
          public void callback(Timestamp response) {
            try {
              assertTrue(new EqualTester().isEqual(response));
              finishTest();
            }
            catch (Throwable e) {
              e.printStackTrace();
              fail();
            }
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

        class EqualTester {
          public boolean isEqual(Time r) {
            return r != null &&
                    r.equals(ts);
          }
        }

        MessageBuilder.createCall(new RemoteCallback<Time>() {
          @Override
          public void callback(Time response) {
            try {
              assertTrue(new EqualTester().isEqual(response));
              finishTest();
            }
            catch (Throwable e) {
              e.printStackTrace();
              fail();
            }
          }
        }, TestSerializationRPCService.class).testTimeSerialization(ts);
      }
    });
  }

  public void testBigDecimalSerialization() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final BigDecimal ts = new BigDecimal(((double) System.currentTimeMillis()) * 1.04d);

        class EqualTester {
          public boolean isEqual(BigDecimal r) {
            return r != null &&
                    r.equals(ts);
          }
        }

        MessageBuilder.createCall(new RemoteCallback<BigDecimal>() {
          @Override
          public void callback(BigDecimal response) {
            try {
              assertTrue(new EqualTester().isEqual(response));
              finishTest();
            }
            catch (Throwable e) {
              e.printStackTrace();
              fail();
            }
          }
        }, TestSerializationRPCService.class).testBigDecimalSerialization(ts);
      }
    });
  }

  public void testBigIntegerSerialization() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final BigInteger ts = new BigInteger(String.valueOf(System.currentTimeMillis()));

        class EqualTester {
          public boolean isEqual(BigInteger r) {
            return r != null &&
                    r.equals(ts);
          }
        }

        MessageBuilder.createCall(new RemoteCallback<BigInteger>() {
          @Override
          public void callback(BigInteger response) {
            try {
              assertTrue(new EqualTester().isEqual(response));
              finishTest();
            }
            catch (Throwable e) {
              e.printStackTrace();
              fail();
            }
          }
        }, TestSerializationRPCService.class).testBigIntegerSerialization(ts);
      }
    });
  }

  public void testQueueSerialization() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final Queue ts = new LinkedList();

        ts.add("test1");
        ts.add("test2");
        ts.add("test3");

        class EqualTester {
          public boolean isEqual(Queue r) {
            return r != null &&
                    r.equals(ts);
          }
        }

        MessageBuilder.createCall(new RemoteCallback<Queue>() {
          @Override
          public void callback(Queue response) {
            try {
              assertTrue(new EqualTester().isEqual(response));
              finishTest();
            }
            catch (Throwable e) {
              e.printStackTrace();
              fail();
            }
          }
        }, TestSerializationRPCService.class).testQueueSerialization(ts);
      }
    });
  }

  public void testInheritedDefinitionFromExistingParent() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final CustomList ts = new CustomList();

        ts.add("test1");
        ts.add("test2");
        ts.add("test3");

        class EqualTester {
          public boolean isEqual(List r) {
            return r != null &&
                    r.equals(ts);
          }
        }

        MessageBuilder.createCall(new RemoteCallback<List>() {
          @Override
          public void callback(List response) {
            try {
              assertTrue(new EqualTester().isEqual(response));
              finishTest();
            }
            catch (Throwable e) {
              e.printStackTrace();
              fail();
            }
          }
        }, TestSerializationRPCService.class).testInheritedDefinitionFromExistingParent(ts);
      }
    });
  }

  public void testNakedEnum() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final TestEnumA e = TestEnumA.Christian;


        class EqualTester {
          public boolean isEqual(TestEnumA r) {
            return r != null &&
                    r.equals(e);
          }
        }

        MessageBuilder.createCall(new RemoteCallback<TestEnumA>() {
          @Override
          public void callback(TestEnumA response) {
            try {
              assertTrue(new EqualTester().isEqual(response));
              finishTest();
            }
            catch (Throwable e) {
              e.printStackTrace();
              fail();
            }
          }
        }, TestSerializationRPCService.class).testNakedEnum(e);
      }
    });
  }

  public void testBoron() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final Boron.Bean boron = new Boron.Bean();


        class EqualTester {
          public boolean isEqual(Boron.Bean r) {
            return r != null &&
                    r.equals(boron);
          }
        }

        MessageBuilder.createCall(new RemoteCallback<Boron.Bean>() {
          @Override
          public void callback(Boron.Bean response) {
            try {
              assertTrue(new EqualTester().isEqual(response));
              finishTest();
            }
            catch (Throwable e) {
              e.printStackTrace();
              fail();
            }
          }
        }, TestSerializationRPCService.class).testPortableInnerClass(boron);
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

        class EqualTester {
          public boolean isEqual(EntityWithUnqualifiedFields r) {
            return r != null &&
                    r.equals(entity);
          }
        }

        MessageBuilder.createCall(new RemoteCallback<EntityWithUnqualifiedFields>() {
          @Override
          public void callback(EntityWithUnqualifiedFields response) {
            try {
              assertTrue(new EqualTester().isEqual(response));
              finishTest();
            }
            catch (Throwable e) {
              e.printStackTrace();
              fail();
            }
          }
        }, TestSerializationRPCService.class).testEntityWithUnqualifiedFieldTypes(entity);
      }
    });
  }
  
  public void testGenericEntity() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        final GenericEntity<String> entity = new GenericEntity<String>("foo");

        MessageBuilder.createCall(new RemoteCallback<GenericEntity<String>>() {
          @Override
          public void callback(GenericEntity<String> response) {
            try {
              assertEquals(entity, response);
              finishTest();
            }
            catch (Throwable e) {
              e.printStackTrace();
              fail();
            }
          }
        }, TestSerializationRPCService.class).testGenericEntity(entity);
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
        
        final GenericEntity<Group> entity = new GenericEntity<Group>(groups);

        MessageBuilder.createCall(new RemoteCallback<GenericEntity<Group>>() {
          @Override
          public void callback(GenericEntity<Group> response) {
            try {
              assertEquals(entity, response);
              finishTest();
            }
            catch (Throwable e) {
              e.printStackTrace();
              fail();
            }
          }
        }, TestSerializationRPCService.class).testGenericEntity(entity);
      }
    });
  }
}