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

package org.jboss.errai.otec.atomizer;

import static org.jboss.errai.otec.operation.OTOperationImpl.createOperation;

import org.jboss.errai.otec.OTEngine;
import org.jboss.errai.otec.OTEntity;
import org.jboss.errai.otec.mutation.Mutation;
import org.jboss.errai.otec.mutation.MutationType;
import org.jboss.errai.otec.mutation.StringMutation;
import org.jboss.errai.otec.operation.OTOperation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Brock
 */
public class EntityChangeStreamImpl implements EntityChangeStream {
  private final OTEngine engine;
  private final OTEntity entity;

  private int cursor = 0;
  private final List<Mutation> mutations = new ArrayList<Mutation>();

  public EntityChangeStreamImpl(final OTEngine engine, final OTEntity entity) {
    this.engine = engine;
    this.entity = entity;
  }

  @Override
  public void notifyInsert(final int index, final String data) {
    checkIfMustSend(index);
    mutations.add(StringMutation.of(MutationType.Insert, cursor = index, data));
  }

  @Override
  public void notifyDelete(final int index, final String data) {
    mutations.add(StringMutation.of(MutationType.Delete, cursor = index, data));
  }

  @Override
  public void flush() {
    engine.notifyOperation(toOperation());
  }

  private void checkIfMustSend(final int index) {
    if (index <= cursor) {
      flush();
    }
  }

  private OTOperation toOperation() {
    final OTOperation operation = createOperation(engine, mutations, entity.getId(), entity.getRevision(), entity.getState().getHash());
    mutations.clear();
    cursor = 0;
    return operation;
  }
}
