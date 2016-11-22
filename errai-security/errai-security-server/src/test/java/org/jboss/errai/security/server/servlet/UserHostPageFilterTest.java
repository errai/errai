/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.security.server.servlet;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.jboss.errai.marshalling.server.MappingContextSingleton;
import org.jboss.errai.security.shared.api.Group;
import org.jboss.errai.security.shared.api.GroupImpl;
import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.RoleImpl;
import org.jboss.errai.security.shared.api.identity.UserImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public class UserHostPageFilterTest {

  private UserHostPageFilter filter = new UserHostPageFilter();

  @Before
  public void setup() {
    MappingContextSingleton.get();
  }

  @Test
  public void testSecurityContextJsonWhenAGroupHasAnApostrophe() throws Exception {
    final UserImpl user = new UserImpl("Mary", roles("admin"), groups("girls'", "programmer", "admin"));

    final String json = filter.securityContextJson(user);

    assertTrue(isValid(json));
  }

  @Test
  public void testSecurityContextJsonWhenAGroupHasAQuote() throws Exception {
    final UserImpl user = new UserImpl("Mary", roles("admin"), groups("girls\"", "programmer", "admin"));

    final String json = filter.securityContextJson(user);

    assertTrue(isValid(json));
  }

  private Collection<Group> groups(String... groups) {
    return listOf(groups, GroupImpl::new);
  }

  private Collection<Role> roles(String... roles) {
    return listOf(roles, RoleImpl::new);
  }

  private <T> List<T> listOf(final String[] list,
                             final Function<String, T> mapper) {
    return Arrays
            .stream(list)
            .map(mapper)
            .collect(Collectors.toList());
  }

  private boolean isValid(String json) {
    try {
      new JsonParser().parse(json);
    } catch (JsonSyntaxException jse) {
      return false;
    }
    return true;
  }
}
