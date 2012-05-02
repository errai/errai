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

import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.enterprise.client.jaxrs.JacksonTransformer;
import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;
import org.jboss.errai.enterprise.client.jaxrs.test.AbstractErraiJaxrsTest;
import org.jboss.errai.enterprise.jaxrs.client.shared.JacksonTestService;
import org.jboss.errai.enterprise.jaxrs.client.shared.entity.User;
import org.jboss.errai.marshalling.client.Marshalling;
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

  @Test
  public void testJacksonMarshalling() {
   delayTestFinish(5000);
    
    final User user = new User("first", "last", 20, new User("first2", "last2", 40, null));
    user.setPetNames(new ArrayList<String>() {{
      add("pet1");
      add("pet2");
    }});
    user.setFriends(new ArrayList<User>() {{
      add(new User("friend1-first", "friend1-last", 1, null));
      add(new User("friend2-first", "friend2-last", 2, null));
    }});
   
    String erraiJson = Marshalling.toJSON(user);
    System.out.println(erraiJson);
    String jackson = new JacksonTransformer().toJackson(erraiJson);
    System.out.println(jackson);
    
    RestClient.create(JacksonTestService.class,
        new RemoteCallback<String>() {
          @Override
          public void callback(String jackson) {
            assertNotNull("Server failed to parse JSON", jackson);
            System.out.println(jackson);
            String erraiJson = new JacksonTransformer().fromJackson(jackson);
            System.out.println(erraiJson);
            assertEquals(user, (User) Marshalling.fromJSON(erraiJson, User.class));
            finishTest();
          }
    }).postJackson(jackson);
  }
}