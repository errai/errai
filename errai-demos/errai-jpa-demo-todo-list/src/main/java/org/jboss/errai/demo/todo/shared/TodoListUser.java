/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.demo.todo.shared;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.common.client.logging.util.StringFormat;
import org.jboss.errai.databinding.client.api.Bindable;
import org.jboss.errai.security.shared.api.Group;
import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.validation.client.shared.GwtCompatibleEmail;

/**
 * Represents a user of the To-do List application.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
@Portable @Bindable @Entity(name="User")
@NamedQuery(name="userByEmail", query="SELECT u FROM User u WHERE u.email = :email")
public class TodoListUser implements User {

  private static final long serialVersionUID = 1L;

  public static final String SHORT_NAME = "shortName";
  public static final String FULL_NAME = "longName";

  @Id
  private String loginName;

  /**
   * The name the user wants us to call them, both to themselves and other users.
   */
  @NotNull
  @Size(min=1, max=60)
  private String shortName;

  /**
   * The user's full name.
   */
  @NotNull
  @Size(min=1, max=60, message="Is that really your name? I'd like to meet your parents.")
  private String fullName;

  /**
   * The user's email address.
   */
  @Column(nullable=false, unique=true)
  @NotNull
  @GwtCompatibleEmail
  private String email;

  private transient Map<String, String> properties = new HashMap<String, String>();

  public String getLoginName() {
    return loginName;
  }

  public void setLoginName(String loginName) {
    this.loginName = loginName;
  }

  public String getShortName() {
    return shortName;
  }

  public void setShortName(String shortName) {
    this.shortName = shortName;
    properties.put(SHORT_NAME, shortName);
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
    properties.put(FULL_NAME, fullName);
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
    properties.put(StandardUserProperties.EMAIL, email);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((loginName == null) ? 0 : loginName.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof User))
      return false;

    final User user = (User) obj;

    return getIdentifier().equals(user.getIdentifier());
  }

  @Override
  public String getIdentifier() {
    return getLoginName();
  }

  @Override
  public Set<Role> getRoles() {
    return Collections.emptySet();
  }

  public boolean hasAllRoles(String... roleNames) {
    return roleNames.length == 0;
  }

  public boolean hasAnyRoles(String... roleNames) {
    return false;
  }
  
  @Override
  public Set<Group> getGroups() {
    return Collections.<Group> emptySet();
  }

  @Override
  public Map<String, String> getProperties() {
    return properties;
  }

  @Override
  public void setProperty(String name, String value) {
    properties.put(name, value);
  }

  @Override
  public void removeProperty(String name) {
    properties.remove(name);
  }

  @Override
  public String getProperty(String name) {
    return properties.get(name);
  }

  @Override
  public String toString() {
    return StringFormat.format(
            "TodoListUser: { loginName = '%s', shortName = '%s', fullName = '%s', email = '%s' }",
            loginName,
            shortName,
            fullName,
            email);
  }
}
