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

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Default implementation of {@link Role}. On the client, Errai should never reference
 * this type directly. The interface should be used instead to provide the
 * ability to plug in custom {@link Role} implementations.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Portable
public class RoleImpl implements Role {
  private final String name;

  public RoleImpl(@MapsTo("name") String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof RoleImpl)) return false;

    RoleImpl role = (RoleImpl) o;
    return name.equals(role.name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public String toString() {
    return name;
  }
}
