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

package org.jboss.errai.otec.client.atomizer;

import static org.jboss.errai.otec.client.operation.OTOperationImpl.createOperation;

import org.jboss.errai.otec.client.OTEngine;
import org.jboss.errai.otec.client.OTEntity;
import org.jboss.errai.otec.client.StringState;
import org.jboss.errai.otec.client.mutation.Mutation;
import org.jboss.errai.otec.client.mutation.MutationType;
import org.jboss.errai.otec.client.mutation.StringMutation;
import org.jboss.errai.otec.client.operation.OTOperation;

import java.util.Collections;

/**
 * @author Mike Brock
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class EntityChangeStreamImpl implements EntityChangeStream {
  private final OTEngine engine;
  private final OTEntity entity;

  private MutationType type;

  private int start = 0;
  private int cursor = 0;
  private final StringState insertState = StringState.of("");
  private final StringState deleteState = StringState.of("");

  public EntityChangeStreamImpl(final OTEngine engine, final OTEntity entity) {
    this.engine = engine;
    this.entity = entity;
  }

  @Override
  public int getEntityId() {
    return entity.getId();
  }

  @Override
  public void notifyInsert(final int index, final String data) {
    // cannot handle going from -1 of the start and back.
    if (deleteState.get().length() != 0) {
      flush();
    }
    else {
      checkIfMustFlush(index);
    }

    if (type == null) {
      start = index;
      type = MutationType.Insert;
    }

    cursor = (index - start);
    insertState.insert(cursor, data);

  //  System.out.println("insState=\"" + insertState.get() + "\";deleteState=\"" + deleteState.get() + "\"");
  }

  @Override
  public void notifyDelete(final int index, final String data) {
    checkIfMustFlush(index);
    if (type == null) {
      start = index;
      type = MutationType.Delete;
    }

    cursor = (index - start);
    if (!insertState.get().isEmpty()) {
      insertState.delete(cursor, data.length());
    }
    else {
      deleteState.insert(deleteState.get().length(), data);
    }

    System.out.println("insState=\"" + insertState.get() + "\";deleteState=\"" + deleteState.get() + "\"");
  }

  @Override
  public void flush() {
    if (type == null) {
      return;
    }

    try {
      final OTOperation operation = toOperation();

      System.out.println("FlushOp=" + operation);

      engine.notifyOperation(operation);
      insertState.clear();
      deleteState.clear();
    }
    catch (Throwable t) {
      t.printStackTrace();
    }

    System.out.println("EntityState=\"" + entity.getState().get() + "\"");
  }

  private void checkIfMustFlush(final int index) {
    if (type == null) {
      return;
    }

    if (index < start || index > start + insertState.get().length()) {
      flush();
    }
  }

  private OTOperation toOperation() {

    final OTOperation operation;
    if (!insertState.get().isEmpty()) {
      operation = createOperation(engine,
          Collections.<Mutation>singletonList(StringMutation.of(MutationType.Insert, start, insertState.get())),
          entity.getId(), entity.getRevision(), entity.getState().getHash()
      );
    }
    else {
      operation = createOperation(engine,
          Collections.<Mutation>singletonList(StringMutation.of(MutationType.Delete, start, deleteState.get())),
          entity.getId(), entity.getRevision(), entity.getState().getHash()
      );
    }

    start = -1;
    cursor = 0;

    type = null;

    return operation;
  }
}
