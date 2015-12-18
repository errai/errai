/*
 * Copyright (C) 2014 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.security.shared.api;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.jboss.errai.marshalling.server.MappingContextSingleton;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.api.identity.UserImpl;
import org.junit.BeforeClass;
import org.junit.Test;

public class UserCookieEncoderTest {

  @BeforeClass
  public static void setupMarshalling() {
    MappingContextSingleton.get();
  }

  @Test
  public void encodeThenDecodeUserCookie() throws Exception {
    Collection<Role> roles = new ArrayList<Role>();
    roles.add(new RoleImpl("user"));
    roles.add(new RoleImpl("staff"));

    Map<String, String> properties = new LinkedHashMap<String, String>();
    properties.put("p1", "v1");
    properties.put("p2", "v2");

    User originalUser = new UserImpl("fred", roles, properties);
    User decodedUser = UserCookieEncoder.fromCookieValue(UserCookieEncoder.toCookieValue(originalUser));

    assertEquals(originalUser.getIdentifier(), decodedUser.getIdentifier());
    assertEquals(originalUser.getGroups(), decodedUser.getGroups());
    assertEquals(originalUser.getProperties(), decodedUser.getProperties());
    assertEquals(originalUser.getRoles(), decodedUser.getRoles());
  }

  /**
   * Jetty 6 quotes and escapes cookie values that contain "special" characters, which are always present in Errai's
   * marshalled object strings.
   */
  @Test
  public void testDecodeJettyQuotedEscapedUserCookie() throws Exception {
    String quotedAdmin = "\"{\\\"^EncodedType\\\":\\\"org.jboss.errai.security.shared.api.identity.UserImpl\\\",\\\"^ObjectID\\\":\\\"1\\\","
            + "\\\"name\\\":\\\"admin\\\","
            + "\\\"roles\\\":{\\\"^EncodedType\\\":\\\"java.util.Collections$UnmodifiableSet\\\","
            + "\\\"^ObjectID\\\":\\\"2\\\",\\\"^Value\\\":[{\\\"^EncodedType\\\":\\\"org.jboss.errai.security.shared.api.RoleImpl\\\","
            + "\\\"^ObjectID\\\":\\\"3\\\",\\\"name\\\":\\\"admin\\\"},{\\\"^EncodedType\\\":\\\"org.jboss.errai.security.shared.api.RoleImpl\\\","
            + "\\\"^ObjectID\\\":\\\"4\\\",\\\"name\\\":\\\"simple\\\"}]},"
            + "\\\"groups\\\":{\\\"^EncodedType\\\":\\\"java.util.Collections$UnmodifiableSet\\\","
            + "\\\"^ObjectID\\\":\\\"5\\\",\\\"^Value\\\":[]},"
            + "\\\"properties\\\":{\\\"^EncodedType\\\":"
            + "\\\"java.util.TreeMap\\\",\\\"^ObjectID\\\":\\\"6\\\",\\\"^Value\\\":"
            + "{\\\"org.jboss.errai.security.FIRST_NAME\\\":\\\"John\\\",\\\"org.jboss.errai.security.EMAIL\\\":\\\"john@doe.com\\\","
            + "\\\"org.jboss.errai.security.LAST_NAME\\\":\\\"Do\u00e9\\\"}}}\"";

    User admin = UserCookieEncoder.fromCookieValue(quotedAdmin);

    Set<Role> expectedRoles = new HashSet<Role>();
    expectedRoles.add(new RoleImpl("admin"));
    expectedRoles.add(new RoleImpl("simple"));

    Map<String, String> expectedProperties = new TreeMap<String, String>();
    expectedProperties.put("org.jboss.errai.security.FIRST_NAME", "John");
    expectedProperties.put("org.jboss.errai.security.LAST_NAME", "Do\u00e9");
    expectedProperties.put("org.jboss.errai.security.EMAIL", "john@doe.com");

    assertEquals("admin", admin.getIdentifier());
    assertEquals(expectedRoles, admin.getRoles());
    assertEquals(expectedProperties, admin.getProperties());
  }
}
