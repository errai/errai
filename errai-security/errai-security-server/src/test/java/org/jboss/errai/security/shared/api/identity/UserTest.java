/**
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.security.shared.api.identity;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.jboss.errai.marshalling.client.Marshalling;
import org.jboss.errai.marshalling.client.MarshallingSessionProviderFactory;
import org.jboss.errai.marshalling.server.MappingContextSingleton;
import org.jboss.errai.security.shared.api.RoleImpl;
import org.junit.Test;

public class UserTest {

  @Test
  public void userImplShouldCaptureRolesFromConstructor() {
    User user = new UserImpl("test", Arrays.asList(new RoleImpl("a"), new RoleImpl("b"), new RoleImpl("c")));
    assertTrue(user.getRoles().contains(new RoleImpl("a")));
  }

  @Test
  public void testHasAnyRoles() {
    User user = new UserImpl("test", Arrays.asList(new RoleImpl("a"), new RoleImpl("b"), new RoleImpl("c")));
    assertTrue(user.hasAnyRoles("a"));
    assertTrue(user.hasAnyRoles("b", "c"));
    assertTrue(user.hasAnyRoles("a", "f"));

    assertFalse(user.hasAnyRoles("f"));
    assertFalse(user.hasAnyRoles("f", "d"));
    assertFalse(user.hasAnyRoles());
  }

  @Test
  public void testHasAllRoles() throws Exception {
    User user = new UserImpl("test", Arrays.asList(new RoleImpl("a"), new RoleImpl("b"), new RoleImpl("c")));
    assertTrue(user.hasAllRoles("a"));
    assertTrue(user.hasAllRoles("a", "b"));
    assertTrue(user.hasAllRoles("c", "a", "b"));

    assertFalse(user.hasAllRoles("f"));
    assertFalse(user.hasAllRoles("a", "f"));
    assertFalse(user.hasAllRoles("a", "b", "f", "c"));
    assertTrue(user.hasAllRoles());
  }

  @Test
  public void userShouldBePortable() {
    Map<String, String> randomProperties = new HashMap<String, String>();
    randomProperties.put("rand1", "RAND1");
    randomProperties.put("rand2", "RAND2");
    randomProperties.put("rand3", "RAND3");
    User user = new UserImpl("test", Arrays.asList(new RoleImpl("a"), new RoleImpl("b"), new RoleImpl("c")), randomProperties);

    if (!MarshallingSessionProviderFactory.isMarshallingSessionProviderRegistered()) {
      MappingContextSingleton.loadDynamicMarshallers();
    }

    String userAsJson = Marshalling.toJSON(user);
    User unmarshalledUser = (User) Marshalling.fromJSON(userAsJson);

    assertEquals(user.getIdentifier(), unmarshalledUser.getIdentifier());
    assertEquals(user.getRoles(), unmarshalledUser.getRoles());
    assertEquals(user.getProperties(), unmarshalledUser.getProperties());
  }
}
