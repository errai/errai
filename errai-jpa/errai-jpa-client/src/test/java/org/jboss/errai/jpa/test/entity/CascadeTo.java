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

package org.jboss.errai.jpa.test.entity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import org.jboss.errai.databinding.client.api.Bindable;

@Bindable @Entity
public class CascadeTo {

  @Id @GeneratedValue
  private long id;

  @OneToOne(cascade=CascadeType.MERGE)
  private CascadeThirdGeneration cascadeAgain;

  private String string;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public void setCascadeAgain(CascadeThirdGeneration cascadeAgain) {
    this.cascadeAgain = cascadeAgain;
  }

  public CascadeThirdGeneration getCascadeAgain() {
    return cascadeAgain;
  }

  public String getString() {
    return string;
  }

  public void setString(String string) {
    this.string = string;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (id ^ (id >>> 32));
    result = prime * result + ((string == null) ? 0 : string.hashCode());
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
    CascadeTo other = (CascadeTo) obj;
    if (id != other.id)
      return false;
    if (string == null) {
      if (other.string != null)
        return false;
    }
    else if (!string.equals(other.string))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "CascadeTo [id=" + id + ", string=" + string + "]";
  }
}
