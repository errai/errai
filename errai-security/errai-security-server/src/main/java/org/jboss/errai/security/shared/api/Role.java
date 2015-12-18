/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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

import java.io.Serializable;
import java.util.HashSet;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.security.shared.api.identity.User;

/**
 * Represents a set of capabilities of a {@link User} for the purpose of access control. 
 *
 * <p>
 * The default implementation of this role in Errai is {@link RoleImpl}, but a different
 * implementation may be used so long as:
 * <ul>
 * <li>It is {@link Portable}.
 * <li>It overrides the {@link Object#equals(Object)} method (used to compare roles between
 * {@link User} and a secured resource).
 * <li>It overrides the {@link Object#hashCode()} method so that equal roles have the same hash.
 * (This is important if you use the default {@link User} implementation, which stores roles in a
 * {@link HashSet}.)
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public interface Role extends Serializable {

  public static final Role NOBODY = new RoleImpl("NOBODY");

  /**
   * @return The name of this role. This should <b>not</b> be used for comparing instances.
   */
  String getName();

}
