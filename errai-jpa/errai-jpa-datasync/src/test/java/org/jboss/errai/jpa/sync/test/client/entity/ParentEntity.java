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

package org.jboss.errai.jpa.sync.test.client.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class ParentEntity implements Cloneable {

  @Id @GeneratedValue
  private int id;

  private String string;
  private Integer integer;

  @OneToMany(mappedBy="parent")
  private List<ChildEntity> children = new ArrayList<ChildEntity>();

  public ParentEntity() {
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getString() {
    return string;
  }

  public void setString(String string) {
    this.string = string;
  }

  public Integer getInteger() {
    return integer;
  }

  public void setInteger(Integer integer) {
    this.integer = integer;
  }

  public List<ChildEntity> getChildren() {
    return children;
  }

  public void setChildren(List<ChildEntity> children) {
    this.children = children;
  }

  public void addChild(ChildEntity child) {
    child.setParent(this);
    children.add(child);
  }

  @Override
  public String toString() {
    return "ParentEntity [id=" + id + ", string=" + string + ", integer=" + integer + ", children=" + children + "]";
  }
}
