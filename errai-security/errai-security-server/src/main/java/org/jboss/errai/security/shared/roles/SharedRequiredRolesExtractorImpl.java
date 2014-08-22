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
package org.jboss.errai.security.shared.roles;

import java.util.HashSet;
import java.util.Set;

import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.RoleImpl;
import org.jboss.errai.security.shared.api.annotation.RestrictedAccess;
import org.jboss.errai.security.shared.spi.RequiredRolesExtractor;

/**
 * Implements basic role extracting functionality common to client and server implementations of
 * {@link RequiredRolesExtractor}.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public abstract class SharedRequiredRolesExtractorImpl implements RequiredRolesExtractor {

  @Override
  public Set<Role> extractAllRoles(final RestrictedAccess annotation) {
    final Set<Role> simpleRoles = new HashSet<Role>(annotation.roles().length);

    for (final String roleName : annotation.roles()) {
      //XXX Maybe we should cache and reuse RoleImpls?
      simpleRoles.add(new RoleImpl(roleName));
    }

    return simpleRoles;
  }
}