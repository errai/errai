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

import java.util.List;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class GenericEntity<T> {
  // causes the generation of a field accessor to test subtype specific field accessors for generic types.
  private T privateField;
  
  T field;
  List<T> list;

  public GenericEntity(){}
  
  public GenericEntity(T field) {
    this.field = field;
  }
  
  public T getField() {
    return field;
  }

  public void setField(T field) {
    this.field = field;
  }

  public List<T> getList() {
    return list;
  }

  public void setList(List<T> list) {
    this.list = list;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((field == null) ? 0 : field.hashCode());
    result = prime * result + ((list == null) ? 0 : list.hashCode());
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
    GenericEntity other = (GenericEntity) obj;
    if (field == null) {
      if (other.field != null)
        return false;
    }
    else if (!field.equals(other.field))
      return false;
    if (list == null) {
      if (other.list != null)
        return false;
    }
    else if (!list.equals(other.list))
      return false;
    return true;
  }
}
