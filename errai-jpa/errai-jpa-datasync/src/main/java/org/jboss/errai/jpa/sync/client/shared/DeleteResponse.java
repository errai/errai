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

package org.jboss.errai.jpa.sync.client.shared;

import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Sync response that indicates the deletion of an entity.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 *
 * @param <X> the entity type
 */
@Portable
public class DeleteResponse<X> extends SyncResponse<X> {

  private final X entity;

  public DeleteResponse(@MapsTo("entity") X entity) {
    this.entity = Assert.notNull(entity);
  }

  /**
   * Returns the entity that was deleted.
   *
   * @return
   */
  public X getEntity() {
    return entity;
  }

  @Override
  public String toString() {
    return "Delete " + entity;
  }
}
