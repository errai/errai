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

package org.jboss.errai.security.demo.client.shared;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;

import org.jboss.errai.security.shared.api.RequiredRolesProvider;
import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.RoleImpl;
import org.jboss.errai.security.shared.api.annotation.RestrictedAccess;

/**
 * <p>
 * This is a simple example of a {@link RequiredRolesProvider}. Role providers can be used to supply
 * {@link Role} implementations for resources that are more complex than simple String-based roles.
 *
 * <p>
 * In this case, the returned role implementations is the same as that used for String-based roles
 * passed to {@link RestrictedAccess#roles()}. But any implementation can be returned by a role
 * provider as long as it meets the requirements described on the {@link Role} interface.
 */
@Dependent
public class AdminRoleProvider implements RequiredRolesProvider {

  @Override
  public Set<Role> getRoles() {
    return new HashSet<Role>(Arrays.asList(new RoleImpl("admin")));
  }

}
