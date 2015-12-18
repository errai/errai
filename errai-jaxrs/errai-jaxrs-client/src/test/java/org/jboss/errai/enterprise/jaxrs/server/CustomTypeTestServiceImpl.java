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

package org.jboss.errai.enterprise.jaxrs.server;

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.enterprise.jaxrs.client.shared.CustomTypeTestService;
import org.jboss.errai.enterprise.jaxrs.client.shared.entity.Entity;
import org.jboss.errai.enterprise.jaxrs.client.shared.entity.SubEntity;

/**
 * Implementation of {@link CustomTypeTestService} returning test data.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class CustomTypeTestServiceImpl implements CustomTypeTestService {

  @Override
  public Entity getEntity() {
    return new Entity(1, "entity1");
  }
  
  @Override
  public Entity getSubEntity() {
    return new SubEntity("val");
  }

  @Override
  public Entity postEntity(Entity entity) {
    return entity;
  }
  
  @Override
  public Entity postEntityReturningNull(Entity entity) {
    return null;
  }
  
  @Override
  public Entity postEntityCustomJsonMediaType(Entity entity) {
    return entity;
  }

  @Override
  public Entity putEntity(Entity entity) {
    return entity;
  }
  
  @Override
  public void putEntityReturningVoid(Entity entity) {
    return;
  }

  @Override
  public Entity deleteEntity(long id) {
    return new Entity(id, "entity");
  }

  @Override
  public List<Entity> getEntities() {
    List<Entity> entities = new ArrayList<Entity>();
    entities.addAll(ENTITIES);
    return entities;
  }
}
