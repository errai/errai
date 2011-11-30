/*
 * Copyright 2011 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.bus.client.tests.support;

import org.jboss.errai.common.client.api.annotations.Portable;

import java.util.List;

/**
 * @author Mike Brock
 */
@Portable
public class EntityWithGenericCollections {
  private List<Float> listOfFloats;

  public EntityWithGenericCollections() {
  }

  public List<Float> getListOfFloats() {
    return listOfFloats;
  }

  public void setListOfFloats(List<Float> listOfFloats) {
    this.listOfFloats = listOfFloats;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof EntityWithGenericCollections)) return false;

    EntityWithGenericCollections that = (EntityWithGenericCollections) o;

    return !(listOfFloats != null ? !listOfFloats.equals(that.listOfFloats) : that.listOfFloats != null);

  }
}
