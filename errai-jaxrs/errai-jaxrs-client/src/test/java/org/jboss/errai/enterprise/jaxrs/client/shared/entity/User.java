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

package org.jboss.errai.enterprise.jaxrs.client.shared.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.ser.std.ToStringSerializer;
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

  @JsonSerialize(using=ToStringSerializer.class)
  private Long id;
  private String firstName;
  private String lastName;
  private Gender gender;

  private User parent;
  private User parentRef; // This is to test back references

  private List<String> petNames = new ArrayList<String>();
  private List<String> petNames2 = new ArrayList<String>(); // This is to test back references of collections

  private List<User> friends = new ArrayList<User>();
  private List<Integer> favoriteNumbers = new ArrayList<Integer>();

  private Map<Integer, String> friendsNameMap = new HashMap<Integer, String>();
  private Map<String, User> friendsMap = new HashMap<String, User>();
  
  private List<Gender> genders;

  public List<Gender> getGenders() {
    return genders;
  }

  public void setGenders(List<Gender> genders) {
    this.genders = genders;
  }

  private Integer age;
  private boolean alive = true;
  private Date date;
  private String jacksonRep;
  
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
    this.favoriteNumbers.add(17);
    this.favoriteNumbers.add(11);
    this.setDate(new Date());
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

  public List<String> getPetNames2() {
    return petNames2;
  }

  public void setPetNames2(List<String> petNames2) {
    this.petNames2 = petNames2;
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

  public List<Integer> getFavoriteNumbers() {
    return favoriteNumbers;
  }

  public void setFavoriteNumbers(List<Integer> favoriteNumbers) {
    this.favoriteNumbers = favoriteNumbers;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public Date getDate() {
    return date;
  }

  public Map<Integer, String> getFriendsNameMap() {
    return friendsNameMap;
  }

  public void setFriendsNameMap(Map<Integer, String> friendsNameMap) {
    this.friendsNameMap = friendsNameMap;
  }

  public Map<String, User> getFriendsMap() {
    return friendsMap;
  }

  public void setFriendsMap(Map<String, User> friendsMap) {
    this.friendsMap = friendsMap;
  }

  public String getJacksonRep() {
    return jacksonRep;
  }

  public void setJacksonRep(String jacksonRep) {
    this.jacksonRep = jacksonRep;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((age == null) ? 0 : age.hashCode());
    result = prime * result + (alive ? 1231 : 1237);
    result = prime * result + ((date == null) ? 0 : date.hashCode());
    result = prime * result + ((favoriteNumbers == null) ? 0 : favoriteNumbers.hashCode());
    result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
    result = prime * result + ((friends == null) ? 0 : friends.hashCode());
    result = prime * result + ((friendsMap == null) ? 0 : friendsMap.hashCode());
    result = prime * result + ((friendsNameMap == null) ? 0 : friendsNameMap.hashCode());
    result = prime * result + ((gender == null) ? 0 : gender.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
    result = prime * result + ((parent == null) ? 0 : parent.hashCode());
    result = prime * result + ((parentRef == null) ? 0 : parentRef.hashCode());
    result = prime * result + ((petNames == null) ? 0 : petNames.hashCode());
    result = prime * result + ((petNames2 == null) ? 0 : petNames2.hashCode());
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
    if (date == null) {
      if (other.date != null)
        return false;
    }
    else if (!date.equals(other.date))
      return false;
    if (favoriteNumbers == null) {
      if (other.favoriteNumbers != null)
        return false;
    }
    else if (!favoriteNumbers.equals(other.favoriteNumbers))
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
    if (friendsMap == null) {
      if (other.friendsMap != null)
        return false;
    }
    else if (!friendsMap.equals(other.friendsMap))
      return false;
    if (genders == null) {
      if (other.genders != null)
        return false;
    }
    else if (!genders.equals(other.genders))
      return false;
    if (friendsNameMap == null) {
      if (other.friendsNameMap != null)
        return false;
    }
    else if (!friendsNameMap.equals(other.friendsNameMap))
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
    if (petNames2 == null) {
      if (other.petNames2 != null)
        return false;
    }
    else if (!petNames2.equals(other.petNames2))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "User [id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + ", gender=" + gender
            + ", parent=" + parent + ", parentRef=" + parentRef + ", petNames=" + petNames + ", petNames2=" + petNames2
            + ", friends=" + friends + ", favoriteNumbers=" + favoriteNumbers + ", friendsNameMap=" + friendsNameMap
            + ", friendsMap=" + friendsMap + ", age=" + age + ", alive=" + alive + ", date=" + date + "]";
  }

}
