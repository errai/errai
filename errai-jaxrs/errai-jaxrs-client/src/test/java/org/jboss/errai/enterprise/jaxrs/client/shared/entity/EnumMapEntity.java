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

import java.util.HashMap;
import java.util.Map;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class EnumMapEntity {

  public enum SomeEnum {
    ENUM_VALUE
  }
  
  private Map<SomeEnum, String> map = new HashMap<SomeEnum, String>();

  public Map<SomeEnum, String> getMap() {
    return map;
  }

  public void setMap(Map<SomeEnum, String> map) {
    this.map = map;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((map == null) ? 0 : map.hashCode());
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
    EnumMapEntity other = (EnumMapEntity) obj;
    if (map == null) {
      if (other.map != null)
        return false;
    }
    else if (!map.equals(other.map))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "EnumMapEntity [map=" + map + "]";
  }
}
