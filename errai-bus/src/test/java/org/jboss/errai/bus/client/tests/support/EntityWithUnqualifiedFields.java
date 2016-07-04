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

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * @author Mike Brock
 */
@Portable
public class EntityWithUnqualifiedFields {
  
  private Object field1;
  private Object field2;

  public EntityWithUnqualifiedFields() {
  }

  public Object getField1() {
    return field1;
  }

  public void setField1(Object field1) {
    this.field1 = field1;
  }

  public Object getField2() {
    return field2;
  }

  public void setField2(Object field2) {
    this.field2 = field2;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((field1 == null) ? 0 : field1.hashCode());
    result = prime * result + ((field2 == null) ? 0 : field2.hashCode());
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
    final EntityWithUnqualifiedFields other = (EntityWithUnqualifiedFields) obj;
    if (field1 == null) {
      if (other.field1 != null)
        return false;
    }
    else if (!field1.equals(other.field1))
      return false;
    if (field2 == null) {
      if (other.field2 != null)
        return false;
    }
    else if (!field2.equals(other.field2))
      return false;
    return true;
  }

}
