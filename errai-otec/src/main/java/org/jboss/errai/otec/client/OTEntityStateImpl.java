/*
 * Copyright 2013 JBoss, by Red Hat, Inc
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

package org.jboss.errai.otec.client;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Mike Brock
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class OTEntityStateImpl implements OTEntityState {
  private volatile int entityIdCounter = 0;
  private final Map<Integer, OTEntity> entityMap = new ConcurrentHashMap<Integer, OTEntity>();

  @Override
  public OTEntity getEntity(final int id) {
    return entityMap.get(id);
  }

  @SuppressWarnings("unchecked")
  @Override
  public OTEntity addEntity(final State objectReference) {
    final OTEntityImpl entity = new OTEntityImpl(nextEntityId(), objectReference);
    addEntity(entity);
    return entity;
  }

  @Override
  public void addEntity(final OTEntity entity) {
    entityMap.put(entity.getId(), entity);
    entity.incrementRevision();
  }


  @Override
  public Collection<OTEntity> getEntities() {
    return entityMap.values();
  }

  @Override
  public void removeEntity(final int entityId) {
    entityMap.remove(entityId);
  }

  private synchronized int nextEntityId() {
    return ++entityIdCounter;
  }
}
