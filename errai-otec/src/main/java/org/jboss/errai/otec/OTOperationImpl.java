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

package org.jboss.errai.otec;

import java.util.List;

/**
 * @author Christian Sadilek
 * @author Mike Brock
 */
public class OTOperationImpl implements OTOperation {
  private final List<Mutation> mutations;
  private final Integer entityId;
  private final Integer revision;

  public OTOperationImpl(List<Mutation> mutationList, Integer entityId, Integer revision) {
    this.mutations = mutationList;
    this.entityId = entityId;
    this.revision = revision;
  }

  @Override
  public List<Mutation> getMutations() {
    return mutations;
  }

  @Override
  public Integer getEntityId() {
    return entityId;
  }

  @Override
  public Integer getRevision() {
    return revision;
  }

  @Override
  public void apply(OTEntity entity) {
    for (final Mutation mutation : mutations) {
      mutation.apply(entity.getState());
    }
    entity.setRevision(getRevision());
  }


}
