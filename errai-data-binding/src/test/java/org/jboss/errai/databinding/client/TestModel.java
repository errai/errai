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

package org.jboss.errai.databinding.client;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;

/**
 * Simple bindable model for testing purposes.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Bindable
@Portable
public class TestModel {

  private int id;
  private String value;
  // the _ here is used to test proper JavaBean property discovery
  private String _name;
  private Integer _age;
  private boolean active;
  private TestModel child;
  
  // this field tests for the case there's a field name collision in the generated proxy
  @SuppressWarnings("unused")
  private String agent;
  
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getName() {
    return _name;
  }

  public void setName(String _name) {
    this._name = _name;
  }

  public Integer getAge() {
    return _age;
  }

  public TestModel setAge(Integer _age) {
    this._age = _age;
    return this;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public TestModel getChild() {
    return child;
  }

  public void setChild(TestModel child) {
    this.child = child;
  }

  // this method is used to test property changes using non accessor methods
  public void activate() {
    this.active = true;
    if (child != null) {
      child.activate();
    }
  }

  protected TestModel activate(boolean b) {
    this.active = b;
    return this;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_age == null) ? 0 : _age.hashCode());
    result = prime * result + ((_name == null) ? 0 : _name.hashCode());
    result = prime * result + (active ? 1231 : 1237);
    result = prime * result + ((child == null) ? 0 : child.hashCode());
    result = prime * result + id;
    result = prime * result + ((value == null) ? 0 : value.hashCode());
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
    TestModel other = (TestModel) obj;
    if (_age == null) {
      if (other._age != null)
        return false;
    }
    else if (!_age.equals(other._age))
      return false;
    if (_name == null) {
      if (other._name != null)
        return false;
    }
    else if (!_name.equals(other._name))
      return false;
    if (active != other.active)
      return false;
    if (child == null) {
      if (other.child != null)
        return false;
    }
    else if (!child.equals(other.child))
      return false;
    if (id != other.id)
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    }
    else if (!value.equals(other.value))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Model [id=" + id + ", value=" + value + ", _name=" + _name + ", _age=" + _age + ", active=" + active
        + ", child=" + child + "]";
  }
}