/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.bus.client.security;

import java.util.Set;

/**
 * Defines the context of an authenticated session.
 */
public interface AuthenticationContext {
    public String getName();

    /**
     * Return any roles associated with the session.
     *
     * @return
     */
    public Set<Role> getRoles();

    /**
     * Terminate the context.
     */
    public void logout();

    /**
     * Returns true if the context is currently valid.
     *
     * @return - boolean indicating the validity of the session.
     */
    public boolean isValid();
}
