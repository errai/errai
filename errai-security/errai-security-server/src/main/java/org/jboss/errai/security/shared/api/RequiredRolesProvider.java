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

import java.util.Set;

import org.jboss.errai.security.shared.api.annotation.RestrictedAccess;

/**
 * Implementations of this type can supply {@link Role Roles} at runtime that are required for
 * resources protected by {@link RestrictedAccess}. The roles returned can be any implementation of
 * the {@link Role} interface that satisfies the interface contract.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public interface RequiredRolesProvider {
  /**
   * @return The set of {@link Role Roles} required to access a particular resource. Never <code>null</code>.
   */
  Set<Role> getRoles();
}
