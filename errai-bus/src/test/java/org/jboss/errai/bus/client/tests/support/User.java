/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.client.tests.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@Portable
public class User extends Person {
  private int id;
  private String name;
  private List<Group> groups;
  private Map<User, String> userStringMap;
  private Map<String, User> userMapString;
  private Group group;
  
  public User() {}
  
  public User(int id, String name) {
    this.id = id;
    this.name = name;
  }
  
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Group> getGroups() {
    return groups;
  }

  public void setGroups(List<Group> groups) {
    this.groups = groups;
  }

  public Map<User, String> getUserStringMap() {
    return userStringMap;
  }

  public void setUserStringMap(Map<User, String> userStringMap) {
    this.userStringMap = userStringMap;
  }

  public Map<String, User> getUserMapString() {
    return userMapString;
  }

  public void setUserMapString(Map<String, User> userMapString) {
    this.userMapString = userMapString;
  }

 

  @Override
  public String toString() {
    return "User{" +
            "id=" + id +
            ", name='" + name + '\'' +
            '}';
  }

  public static User create() {
    User user = new User();
    user.id = 1;
    user.name = "mrfoo";

    user.groups = new ArrayList<Group>();

    Group adminGroup = new Group();
    adminGroup.setSubGroup(adminGroup);
    adminGroup.setGroupId(1);
    adminGroup.setName("Admin");
    adminGroup.setUsersInGroup(new ArrayList<User>());

    user.groups.add(adminGroup);
    adminGroup.getUsersInGroup().add(user);

    Map<Group, User> groupUser = new HashMap<Group, User>();
    groupUser.put(adminGroup, user);

    adminGroup.setGroupUserMap(groupUser);

    user.userStringMap = new HashMap<User, String>();
    user.userStringMap.put(user, "foo");

    user.userMapString = new HashMap<String, User>();
    user.userMapString.put("bar", user);

    return user;
  }

  @Override
  public Group getGroup() {
    return group;
  }

  @Override
  public void setGroup(Group group) {
    this.group = group;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((group == null) ? 0 : group.hashCode());
    result = prime * result + ((groups == null) ? 0 : groups.hashCode());
    result = prime * result + id;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((userMapString == null) ? 0 : userMapString.hashCode());
    result = prime * result + ((userStringMap == null) ? 0 : userStringMap.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    User other = (User) obj;
    if (group == null) {
      if (other.group != null)
        return false;
    }
    else if (!group.equals(other.group))
      return false;
    if (groups == null) {
      if (other.groups != null)
        return false;
    }
    else if (!groups.equals(other.groups))
      return false;
    if (id != other.id)
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    }
    else if (!name.equals(other.name))
      return false;
    if (userMapString == null) {
      if (other.userMapString != null)
        return false;
    }
    else if (!userMapString.equals(other.userMapString))
      return false;
    if (userStringMap == null) {
      if (other.userStringMap != null)
        return false;
    }
    else if (!userStringMap.equals(other.userStringMap))
      return false;
    return true;
  }
}
