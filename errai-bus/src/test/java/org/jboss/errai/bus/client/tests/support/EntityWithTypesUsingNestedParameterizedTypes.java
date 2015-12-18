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
import java.util.Map;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Portable type used to test marshalling of types using nested parameterized types.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Portable
public class EntityWithTypesUsingNestedParameterizedTypes {

  private Map<String, Map<String, String>> map = null;

  // regression test for ERRAI-565
  private Map<Long, Map<Object, String>> mapWithDifferentTypes = null;

  private List<List<Integer>> list = null;

  public Map<String, Map<String, String>> getMap() {
    return map;
  }

  public void setMap(Map<String, Map<String, String>> data) {
    this.map = data;
  }

  public Map<Long, Map<Object, String>> getMapWithDifferentTypes() {
    return mapWithDifferentTypes;
  }

  public void setMapWithDifferentTypes(Map<Long, Map<Object, String>> mapWithDifferentTypes) {
    this.mapWithDifferentTypes = mapWithDifferentTypes;
  }

  public List<List<Integer>> getList() {
    return list;
  }

  public void setList(List<List<Integer>> list) {
    this.list = list;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((list == null) ? 0 : list.hashCode());
    result = prime * result + ((map == null) ? 0 : map.hashCode());
    result = prime * result + ((mapWithDifferentTypes == null) ? 0 : mapWithDifferentTypes.hashCode());
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
    EntityWithTypesUsingNestedParameterizedTypes other = (EntityWithTypesUsingNestedParameterizedTypes) obj;
    if (list == null) {
      if (other.list != null)
        return false;
    }
    else if (!list.equals(other.list))
      return false;
    if (map == null) {
      if (other.map != null)
        return false;
    }
    else if (!map.equals(other.map))
      return false;
    if (mapWithDifferentTypes == null) {
      if (other.mapWithDifferentTypes != null)
        return false;
    }
    else if (!mapWithDifferentTypes.equals(other.mapWithDifferentTypes))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "EntityWithTypesUsingNestedParameterizedTypes [map=" + map + ", mapWithDifferentTypes="
            + mapWithDifferentTypes + ", list=" + list + "]";
  }
}
