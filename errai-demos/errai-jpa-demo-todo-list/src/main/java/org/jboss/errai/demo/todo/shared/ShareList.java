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

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author edewit@redhat.com
 */
@Entity
@NamedQueries({
  @NamedQuery(name = "sharedWithMe", query = "SELECT s.user FROM ShareList s, in (s.sharedWith) w WHERE w.loginName = :loginName"),
  @NamedQuery(name = "mySharedLists", query = "SELECT s FROM ShareList s WHERE s.user.loginName = :loginName")
})
public class ShareList {
  @Id
  @GeneratedValue
  private Long id;

  @OneToOne
  private TodoListUser user;

  @ManyToMany
  private List<TodoListUser> sharedWith;

  public Long getId() {
    return id;
  }

  public TodoListUser getUser() {
    return user;
  }

  public void setUser(TodoListUser user) {
    this.user = user;
  }

  public List<TodoListUser> getSharedWith() {
    if (sharedWith == null) {
      sharedWith = new ArrayList<TodoListUser>();
    }
    return sharedWith;
  }

  public void setSharedWith(List<TodoListUser> sharedWith) {
    this.sharedWith = sharedWith;
  }

  @Override
  public String toString() {
    return "ShareList[" +
            "id=" + id +
            ", user=" + user +
            ", sharedWith=" + sharedWith +
            ']';
  }
}
