/*
 * Copyright 2012 JBoss Inc
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

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * <p>
 * Represents a category this user belongs for the purposes of access control. This can also be
 * thought of as a group or permission.
 *
 * <p>
 * The default implementation of this role in Errai is {@link RoleImpl}, but a different
 * implementation may be used so long as it is {@link Portable} and overrides the
 * {@link Object#equals(Object)} method. Errai Security guarantees that all security access checks
 * are performed by comparing user and resource roles with the {@link Object#equals(Object)} method.
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
