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

package org.jboss.errai.bus.client.security.impl;

import org.jboss.errai.bus.client.security.AuthenticationContext;
import org.jboss.errai.bus.client.security.Role;

import java.util.Set;

public class BasicAuthenticationContext implements AuthenticationContext {
    private Set<Role> roles;
    private String name;
    private boolean valid;

    public BasicAuthenticationContext(Set<Role> roles, String name) {
        this.roles = roles;
        this.name = name;
        valid = true;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void logout() {
        valid = false;
    }

    public boolean isValid() {
        return valid;
    }
}
