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

import java.util.Arrays;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class EntityWithInterfaceArrayField {

  private SubInterface[] arrayField;

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(getArrayField());
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
    EntityWithInterfaceArrayField other = (EntityWithInterfaceArrayField) obj;
    if (!Arrays.equals(getArrayField(), other.getArrayField()))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "EntityWithInterfaceArrayField [arrayField=" + Arrays.toString(getArrayField()) + "]";
  }

  public SubInterface[] getArrayField() {
    return arrayField;
  }

  public void setArrayField(SubInterface[] arrayField) {
    this.arrayField = arrayField;
  }

}
