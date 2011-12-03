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

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.tests.support.*;

/**
 * @author Mike Brock <cbrock@redhat.com>
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
        }, TestRPCServiceRemote.class).listOfLong(list);
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
        }, TestRPCServiceRemote.class).listOfInteger(list);
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
        }, TestRPCServiceRemote.class).listOfFloat(list);
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
        }, TestRPCServiceRemote.class).nestedClass(clazz);
      }
    });
  }
  
  public void testEmptyWithGenericCollections() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final EntityWithGenericCollections ent = new EntityWithGenericCollections();
        List<Float> listOffFloats = new ArrayList<Float>();
        listOffFloats.add(1.0f);
        listOffFloats.add(1.1f);
        listOffFloats.add(1.2f);
        
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
        }, TestRPCServiceRemote.class).genericCollections(ent);
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
        }, TestRPCServiceRemote.class).testStringBufferAndStringBuilder(ent);
      }
    });
  }
}
