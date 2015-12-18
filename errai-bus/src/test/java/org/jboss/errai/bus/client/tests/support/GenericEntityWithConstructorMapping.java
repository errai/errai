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

package org.jboss.errai.bus.client.tests.support;

import java.util.List;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class GenericEntityWithConstructorMapping<T> {

  private final Long n;
  private final List<T> data;

  public GenericEntityWithConstructorMapping(@MapsTo("n") Long nrItems,
                           @MapsTo("data") List<T> data) {
    this.n = nrItems;
    this.data = data;
  }

  public Long getNrItems() {
    return n;
  }

  public List<T> getData() {
    return data;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((data == null) ? 0 : data.hashCode());
    result = prime * result + ((n == null) ? 0 : n.hashCode());
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
    GenericEntityWithConstructorMapping other = (GenericEntityWithConstructorMapping) obj;
    if (data == null) {
      if (other.data != null)
        return false;
    }
    else if (!data.equals(other.data))
      return false;
    if (n == null) {
      if (other.n != null)
        return false;
    }
    else if (!n.equals(other.n))
      return false;
    return true;
  }

}
