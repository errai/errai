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

package org.jboss.errai.enterprise.jaxrs.client.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.enterprise.client.jaxrs.MarshallingWrapper;
import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;
import org.jboss.errai.enterprise.client.jaxrs.test.AbstractErraiJaxrsTest;
import org.jboss.errai.enterprise.jaxrs.client.shared.JacksonTestService;
import org.jboss.errai.enterprise.jaxrs.client.shared.entity.ByteArrayTestWrapper;
import org.jboss.errai.enterprise.jaxrs.client.shared.entity.ImmutableEntity;
import org.jboss.errai.enterprise.jaxrs.client.shared.entity.User;
import org.jboss.errai.enterprise.jaxrs.client.shared.entity.User.Gender;
import org.junit.Test;

/**
 * Tests to ensure Errai JAX-RS can marshal/demarshal Jackson generated JSON.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class JacksonIntegrationTest extends AbstractErraiJaxrsTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.enterprise.jaxrs.TestModule";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    RestClient.setJacksonMarshallingActive(true);
  }

  @Test
  @SuppressWarnings("serial")
  public void testJacksonMarshalling() {
    delayTestFinish(5000);

    final User user =
        new User(11l, "first", "last", 20, Gender.MALE, new User(12l, "first2", "last2", 40, Gender.FEMALE, null));
    final User friend1 =
        new User(13l, "friend1-first", "friend1-last", 1, Gender.MALE, null);
    final User friend2 =
        new User(14l, "friend2-first", "friend2-last", 2, Gender.FEMALE, null);

    user.setPetNames(new ArrayList<String>() {
      {
        add("pet1");
        add("pet2");
      }
    });
    user.setFriends(new ArrayList<User>() {
      {
        add(friend1);
        add(friend2);
      }
    });
    user.setFriendsNameMap(new HashMap<Integer, String>() {
      {
        put(13, "friend1-first");
        put(14, "friend2-frist");
      }
    });
    user.setFriendsMap(new HashMap<String, User>() {
      {
        put("friend1-first", friend1);
        put("friend2-first", friend2);
      }
    });

    String jackson = MarshallingWrapper.toJSON(user);

    call(JacksonTestService.class,
        new RemoteCallback<String>() {
          @Override
          public void callback(String jackson) {
            assertNotNull("Server failed to parse JSON using Jackson", jackson);
            assertEquals(user, MarshallingWrapper.fromJSON(jackson, User.class));
            finishTest();
          }
        }).postJackson(jackson);
  }

  @Test
  public void testJacksonMarshallingOfList() {
    delayTestFinish(5000);

    final List<User> users = new ArrayList<User>();
    users.add(new User(11l, "first", "last", 20, Gender.MALE, null));
    users.add(new User(12l, "first2", "last2", 40, Gender.MALE, null));

    String jackson = MarshallingWrapper.toJSON(users);

    call(JacksonTestService.class,
        new RemoteCallback<String>() {
          @Override
          public void callback(String jackson) {
            assertNotNull("Server failed to parse JSON using Jackson", jackson);
            assertEquals(users, MarshallingWrapper.fromJSON(jackson, List.class, User.class));
            finishTest();
          }
        }).postJacksonList(jackson);
  }

  @Test
  public void testJacksonMarshallingOfCollection() {
    delayTestFinish(5000);

    final List<User> users = new ArrayList<User>();
    users.add(new User(11l, "first", "last", 20, Gender.MALE, null));
    users.add(new User(12l, "first2", "last2", 40, Gender.MALE, null));

    String jackson = MarshallingWrapper.toJSON(users);

    call(JacksonTestService.class,
        new RemoteCallback<String>() {
          @Override
          public void callback(String jackson) {
            assertNotNull("Server failed to parse JSON using Jackson", jackson);
            assertEquals(users, MarshallingWrapper.fromJSON(jackson, Collection.class, User.class));
            finishTest();
          }
        }).postJacksonList(jackson);
  }

  @Test
  public void testJacksonMarshallingOfListOfBytes() {
    delayTestFinish(5000);

    final List<Byte> bytes = new ArrayList<Byte>();
    bytes.add(new Byte("10"));
    bytes.add(new Byte("4"));

    String jackson = MarshallingWrapper.toJSON(bytes);

    call(JacksonTestService.class,
        new RemoteCallback<String>() {
          @Override
          public void callback(String jackson) {
            assertNotNull("Server failed to parse JSON using Jackson", jackson);
            assertEquals(bytes, MarshallingWrapper.fromJSON(jackson, List.class, Byte.class));
            finishTest();
          }
        }).postJacksonListOfBytes(jackson);
  }

  @Test
  public void testJacksonMarshallingOfPortableWithByteArray() {
    delayTestFinish(5000);

    final ByteArrayTestWrapper entity = new ByteArrayTestWrapper();
    String jackson = MarshallingWrapper.toJSON(entity);

    call(JacksonTestService.class,
        new RemoteCallback<String>() {
          @Override
          public void callback(String jackson) {
            assertNotNull("Server failed to parse JSON using Jackson", jackson);
            assertEquals(entity, MarshallingWrapper.fromJSON(jackson, ByteArrayTestWrapper.class));
            finishTest();
          }
        }).postJacksonPortableWithByteArray(jackson);
  }

  @Test
  public void testJacksonMarshallingOfMap() {
    delayTestFinish(5000);

    final Map<String, User> users = new HashMap<String, User>();
    users.put("1", new User(11l, "first", "last", 20, Gender.MALE, null));
    users.put("2", new User(12l, "first2", "last2", 40, Gender.MALE, null));

    String jackson = MarshallingWrapper.toJSON(users);

    call(JacksonTestService.class,
        new RemoteCallback<String>() {
          @SuppressWarnings("unchecked")
          @Override
          public void callback(String jackson) {
            assertNotNull("Server failed to parse JSON using Jackson", jackson);
            Map<String, User> result = MarshallingWrapper.fromJSON(jackson, Map.class, String.class, User.class);
            assertEquals(users, result);
            finishTest();
          }
        }).postJacksonMap(jackson);
  }

  /**
   * This test ensures the assumed element type is correctly inferred when generating marshallers for types that use @MapsTo
   * on List<T> parameters. See https://issues.jboss.org/browse/ERRAI-436.
   */
  @Test
  public void testJacksonDemarshallingOfImmutableTypeWithList() {
    final ImmutableEntity ftd = new ImmutableEntity.Builder().build();
    String jackson = "{\"entities\":[{\"id\":1, \"name\":\"name1\"}, {\"id\":2, \"name\":\"name2\"}]}";
    assertEquals(ftd, MarshallingWrapper.fromJSON(jackson, ImmutableEntity.class));
  }

  /**
   * Guards against regressions of: https://issues.jboss.org/browse/ERRAI-443
   */
  @Test
  public void testJacksonDemarshallingOfMapWithArrayValue() {
    final String json =
        "{\"key1\" : null, \"key2\" : true, \"key3\" : false, \"key4\" : [ false, null, true ], \"key5\": { \"mapKey\": \"mapValue\"}}";
    Map<String, Object> result = MarshallingWrapper.fromJSON(json, Map.class, String.class, Object.class);

    assertEquals("Wrong result size: " + result.toString(), 5, result.size());
    assertNull("key1 should be null", result.get("key1"));
    assertTrue("key2 should be true", (Boolean) result.get("key2"));
    assertFalse("key3 should be false", (Boolean) result.get("key3"));

    List<?> list = (List<?>) result.get("key4");
    assertNotNull("key4 should not be null", list);
    assertEquals("Wrong list size", list.size(), 3);
    assertFalse((Boolean) list.get(0));
    assertNull(list.get(1));
    assertTrue((Boolean) list.get(2));

    Map<?, ?> map = (Map<?, ?>) result.get("key5");
    assertEquals("Wrong map size", map.size(), 1);
    assertNotNull("key5 should not be null", map);
    assertEquals("mapValue", map.get("mapKey"));
  }

}