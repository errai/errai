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

import java.util.Collections;

import org.jboss.errai.otec.client.OTEngine;
import org.jboss.errai.otec.client.OTEntity;
import org.jboss.errai.otec.client.StringState;
import org.jboss.errai.otec.client.mutation.Mutation;
import org.jboss.errai.otec.client.mutation.MutationType;
import org.jboss.errai.otec.client.mutation.StringMutation;
import org.jboss.errai.otec.client.operation.OTOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mike Brock
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class EntityChangeStreamImpl implements EntityChangeStream {
  private boolean open = true;

  private final OTEngine engine;
  private final OTEntity entity;

  private int start = -1;
  private int cursor = 0;
  private final StringState insertState = StringState.of("");
  private final StringState deleteState = StringState.of("");
  
  private static final Logger logger = LoggerFactory.getLogger(EntityChangeStreamImpl.class);

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
    if (!open) {
      return;
    }

    checkIfMustFlush(index, MutationType.Insert);

    if (start == -1) {
      start = index;
    }

    cursor = (index - start);
    insertState.insert(cursor, data);
  }

  @Override
  public void notifyDelete(final int index, final String data) {
    if (!open) {
         return;
       }

    checkIfMustFlush(index, MutationType.Delete);

    if (start == -1) {
      start = index;
    }

    cursor = (index - start);
    if (!insertState.get().isEmpty()) {
      insertState.delete(cursor, data.length());
    }
    else {
      deleteState.insert(deleteState.get().length(), data);
    }
  }

  private static boolean flushing = false;

  @Override
  public void flush() {
    if (!open || start == -1 || flushing) {
      return;
    }

    if (insertState.length() == 0 && deleteState.length() == 0) {
      return;
    }

    flushing = true;
    Atomizer.stopEvents();

    try {
      final OTOperation operation = toOperation();
      engine.notifyOperation(operation);
      insertState.clear();
      deleteState.clear();
      logger.debug("FLUSH: " + operation + ";rev=" + operation.getRevision());
    }
    catch (Throwable t) {
      t.printStackTrace();
    }
    finally {
      flushing = false;
      Atomizer.startEvents();
    }
  }

  private void checkIfMustFlush(final int index, final MutationType type) {
    if (!open || start == -1) {
      return;
    }

    // cannot handle going from -1 of the start and back.
    if (type == MutationType.Insert && !deleteState.get().isEmpty()) {
      flush();
    }
    else if (index < start || index > start + insertState.get().length()) {
      flush();
    }
  }

  private OTOperation toOperation() {

    final OTOperation operation;
    if (!insertState.get().isEmpty()) {
      operation = createOperation(engine, engine.getId(),
          Collections.<Mutation>singletonList(StringMutation.of(MutationType.Insert, start, insertState.get())),
          entity.getId(), entity.getRevision(), entity.getState().getHash()
      );
    }
    else {
      operation = createOperation(engine, engine.getId(),
          Collections.<Mutation>singletonList(StringMutation.of(MutationType.Delete, start, deleteState.get())),
          entity.getId(), entity.getRevision(), entity.getState().getHash()
      );
    }

    start = -1;
    cursor = 0;

    return operation;
  }

  @Override
  public void close() {
    open = false;
  }
}
