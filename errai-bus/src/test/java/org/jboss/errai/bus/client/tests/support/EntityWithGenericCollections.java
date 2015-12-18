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

/**
 * @author Mike Brock
 */
@Portable
public class EntityWithGenericCollections {
  private List<Float> listOfFloats;

  private List<? extends String> listWithLowerBoundWildcard;

  private Object o;

  public EntityWithGenericCollections() {
  }

  public List<Float> getListOfFloats() {
    return listOfFloats;
  }

  public void setListOfFloats(List<Float> listOfFloats) {
    this.listOfFloats = listOfFloats;
  }

  public Object getObject() {
    return o;
  }

  public void setObject(Object o ){
    this.o = o;
  }

  public List<? extends String> getListWithLowerBoundWildcard() {
    return listWithLowerBoundWildcard;
  }

  public void setListWithLowerBoundWildcard(List<? extends String> list) {
    this.listWithLowerBoundWildcard = list;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((listOfFloats == null) ? 0 : listOfFloats.hashCode());
    result = prime * result + ((listWithLowerBoundWildcard == null) ? 0 : listWithLowerBoundWildcard.hashCode());
    result = prime * result + ((o == null) ? 0 : o.hashCode());
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
    EntityWithGenericCollections other = (EntityWithGenericCollections) obj;
    if (listOfFloats == null) {
      if (other.listOfFloats != null)
        return false;
    }
    else if (!listOfFloats.equals(other.listOfFloats))
      return false;
    if (listWithLowerBoundWildcard == null) {
      if (other.listWithLowerBoundWildcard != null)
        return false;
    }
    else if (!listWithLowerBoundWildcard.equals(other.listWithLowerBoundWildcard))
      return false;
    if (o == null) {
      if (other.o != null)
        return false;
    }
    else if (!o.equals(other.o))
      return false;
    return true;
  }

}
