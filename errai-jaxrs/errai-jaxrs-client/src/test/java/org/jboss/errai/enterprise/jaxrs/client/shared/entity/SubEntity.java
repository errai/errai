/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
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

import org.jboss.errai.common.client.api.annotations.Portable;

@SuppressWarnings("serial")
@Portable
public class SubEntity extends Entity {
  
  private String val;

  public SubEntity() {
    
  }

  public SubEntity(String val) {
    super();
    this.val = val;
  }

  public String getVal() {
    return val;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((val == null) ? 0 : val.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    SubEntity other = (SubEntity) obj;
    if (val == null) {
      if (other.val != null)
        return false;
    }
    else if (!val.equals(other.val))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "SubEntity [val=" + val + "]";
  }
}
