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
 * Default implementation of {@link Group}. Errai's built-in security modules do
 * not assign any semantics to groups and therefore don't consider them when
 * checking for permissions.
 * 
 * On the client, Errai should never reference this type directly. The interface
 * should be used instead to provide the ability to plug in custom {@link Group}
 * implementations.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Portable
public class GroupImpl implements Group {
  private final String name;

  public GroupImpl(@MapsTo("group") String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof GroupImpl))
      return false;

    GroupImpl group = (GroupImpl) o;
    return name.equals(group.name);
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
