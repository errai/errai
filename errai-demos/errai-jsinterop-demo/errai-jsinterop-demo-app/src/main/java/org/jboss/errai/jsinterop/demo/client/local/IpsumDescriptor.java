/**
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.jsinterop.demo.client.local;

import org.jboss.errai.databinding.client.api.Bindable;

@Bindable
public class IpsumDescriptor {

  private String id;
  private String name;
  private String description;

  public IpsumDescriptor() {}

  public IpsumDescriptor(final String id, final String name, final String description) {
    this.id = id;
    this.name = name;
    this.description = description;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  @Override
  public int hashCode() {
    return id.hashCode() ^ name.hashCode() ^ description.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
     if (obj instanceof IpsumDescriptor) {
       final IpsumDescriptor other = (IpsumDescriptor) obj;

      return id.equals(other.getId()) && name.equals(other.getName())
              && description.equals(other.getDescription());
     }
     else {
       return false;
     }
  }

}
