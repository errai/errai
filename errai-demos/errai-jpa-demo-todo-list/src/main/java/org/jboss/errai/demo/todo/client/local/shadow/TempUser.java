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

package org.jboss.errai.demo.todo.client.local.shadow;

import org.jboss.errai.demo.todo.shared.TodoListUser;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;

/**
 * A temporary user object that gets stored on the client when the signup failed because of network disconnect
 * @author edewit@redhat.com
 */
@Entity
@NamedQuery(name = "allTempUsers", query = "SELECT u FROM TempUser u")
public class TempUser {
  @Id
  private String email;
  private String password;
  private String fullName;
  private String shortName;
  private String loginName;

  public TempUser() {
  }

  public TempUser(TodoListUser user, String password) {
    this.email = user.getEmail();
    this.fullName = user.getFullName();
    this.shortName = user.getShortName();
    this.loginName = user.getLoginName();
    this.password = password;
  }

  public TodoListUser asUser() {
    TodoListUser user = new TodoListUser();
    user.setEmail(email);
    user.setFullName(fullName);
    user.setShortName(shortName);
    user.setLoginName(loginName);
    return user;
  }

  public String getPassword() {
    return password;
  }
}
