/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

package org.jboss.errai.enterprise.jaxrs.client.shared.entity;

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Test entity for jackson marshalling tests.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Portable
public class User {

  public enum Gender {
    MALE, FEMALE
  }

  private Long id;
  private String firstName;
  private String lastName;
  private Gender gender;

  private User parent;
  private User parentRef; // This is to test back references

  private List<String> petNames = new ArrayList<String>();
  private List<User> friends = new ArrayList<User>();
  private Integer age;
  private boolean alive = true;

  public User() {}

  public User(Long id, String firstName, String lastName, Integer age, Gender gender, User parent) {
    super();
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
    this.age = age;
    this.gender = gender;
    this.parent = parent;
    this.parentRef = parent;
    
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }
  
  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public User getParent() {
    return parent;
  }

  public void setParent(User parent) {
    this.parent = parent;
  }

  public User getParentRef() {
    return parentRef;
  }

  public void setParentRef(User parentRef) {
    this.parentRef = parentRef;
  }

  public List<String> getPetNames() {
    return petNames;
  }

  public void setPetNames(List<String> petNames) {
    this.petNames = petNames;
  }

  public List<User> getFriends() {
    return friends;
  }

  public void setFriends(List<User> friends) {
    this.friends = friends;
  }

  public Integer getAge() {
    return age;
  }

  public void setAge(Integer age) {
    this.age = age;
  }

  public boolean isAlive() {
    return alive;
  }

  public void setAlive(boolean alive) {
    this.alive = alive;
  }

  public Gender getGender() {
    return gender;
  }

  public void setGender(Gender gender) {
    this.gender = gender;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((age == null) ? 0 : age.hashCode());
    result = prime * result + (alive ? 1231 : 1237);
    result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
    result = prime * result + ((friends == null) ? 0 : friends.hashCode());
    result = prime * result + ((gender == null) ? 0 : gender.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
    result = prime * result + ((parent == null) ? 0 : parent.hashCode());
    result = prime * result + ((parentRef == null) ? 0 : parentRef.hashCode());
    result = prime * result + ((petNames == null) ? 0 : petNames.hashCode());
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
    if (age == null) {
      if (other.age != null)
        return false;
    }
    else if (!age.equals(other.age))
      return false;
    if (alive != other.alive)
      return false;
    if (firstName == null) {
      if (other.firstName != null)
        return false;
    }
    else if (!firstName.equals(other.firstName))
      return false;
    if (friends == null) {
      if (other.friends != null)
        return false;
    }
    else if (!friends.equals(other.friends))
      return false;
    if (gender != other.gender)
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    }
    else if (!id.equals(other.id))
      return false;
    if (lastName == null) {
      if (other.lastName != null)
        return false;
    }
    else if (!lastName.equals(other.lastName))
      return false;
    if (parent == null) {
      if (other.parent != null)
        return false;
    }
    else if (!parent.equals(other.parent))
      return false;
    if (parentRef == null) {
      if (other.parentRef != null)
        return false;
    }
    else if (!parentRef.equals(other.parentRef))
      return false;
    if (petNames == null) {
      if (other.petNames != null)
        return false;
    }
    else if (!petNames.equals(other.petNames))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "User [id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + ", gender=" + gender
        + ", parent=" + parent + ", parentRef=" + parentRef + ", petNames=" + petNames + ", friends=" + friends
        + ", age=" + age + ", alive=" + alive + "]";
  }
}