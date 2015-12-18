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

package org.jboss.errai.security.shared.spi;

import java.util.Set;

import org.jboss.errai.security.shared.api.RequiredRolesProvider;
import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.annotation.RestrictedAccess;

/**
 * A client-server agnostic interface for extracting {@link Role Roles} from protected resources.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public interface RequiredRolesExtractor {

  /**
   * Get all roles required to access a particular resource with the given {@link RestrictedAccess}
   * annotation.
   *
   * @param annotation
   *          The annotation from which roles will be extracted. Never <code>null</code>.
   * @return The set of all {@link Role Roles} required to access the resource with the given
   *         annotation. Never <code>null</code>. This method returns exactly the union of roles
   *         from {@link #extractSimpleRoles(RestrictedAccess)} and
   *         {@link #extractProvidedRoles(RestrictedAccess)}.
   */
  Set<Role> extractAllRoles(RestrictedAccess annotation);

  /**
   * Get all simple roles from the given annotation. A simple role is a String-based role supplied
   * via {@link RestrictedAccess#roles()}.
   *
   * @param annotation
   *          The annotation from which roles will be extracted. Never <code>null</code>.
   * @return The set of {@link Role Roles} created from the simple roles in the given annotation.
   *         Never <code>null</code>.
   */
  Set<Role> extractSimpleRoles(RestrictedAccess annotation);

  /**
   * Get the roles specified by all {@link RequiredRolesProvider RequiredRolesProviders} from the
   * given annotation via {@link RestrictedAccess#providers()}.
   *
   * @param annotation
   *          The annotation from which roles will be extracted. Never <code>null</code>.
   * @return The set of {@link Role Roles} from all {@link RequiredRolesProvider
   *         RequiredRolesProviders} of an annotation. Never <code>null</code>.
   */
  Set<Role> extractProvidedRoles(RestrictedAccess annotation);

}
