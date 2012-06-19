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
import java.util.List;

import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.enterprise.client.jaxrs.MarshallingWrapper;
import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;
import org.jboss.errai.enterprise.client.jaxrs.test.AbstractErraiJaxrsTest;
import org.jboss.errai.enterprise.jaxrs.client.shared.JacksonTestService;
import org.jboss.errai.enterprise.jaxrs.client.shared.entity.ByteArrayTestWrapper;
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
    user.setPetNames(new ArrayList<String>() {
      {
        add("pet1");
        add("pet2");
      }
    });
    user.setFriends(new ArrayList<User>() {
      {
        add(new User(13l, "friend1-first", "friend1-last", 1, Gender.MALE, null));
        add(new User(14l, "friend2-first", "friend2-last", 2, Gender.FEMALE, null));
      }
    });

    String jackson = MarshallingWrapper.toJSON(user);

    RestClient.create(JacksonTestService.class,
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
    users.add(new User(12l, "firs2", "las2", 40, Gender.MALE, null));

    String jackson = MarshallingWrapper.toJSON(users);

    RestClient.create(JacksonTestService.class,
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
  public void testJacksonMarshallingOfListOfBytes() {
    delayTestFinish(5000);

    final List<Byte> bytes = new ArrayList<Byte>();
    bytes.add(new Byte("10"));
    bytes.add(new Byte("4"));

    String jackson = MarshallingWrapper.toJSON(bytes);

    RestClient.create(JacksonTestService.class,
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
  @SuppressWarnings("serial")
  public void testJacksonMarshallingOfPortableWithByteArray() {
    delayTestFinish(5000);

    final ByteArrayTestWrapper entity = new ByteArrayTestWrapper();
    String jackson = MarshallingWrapper.toJSON(entity);

    RestClient.create(JacksonTestService.class,
        new RemoteCallback<String>() {
          @Override
          public void callback(String jackson) {
            assertNotNull("Server failed to parse JSON using Jackson", jackson);
            assertEquals(entity, MarshallingWrapper.fromJSON(jackson, ByteArrayTestWrapper.class));
            finishTest();
          }
        }).postJacksonPortableWithByteArray(jackson);
  }
  
}