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

import java.util.Collections;

/**
 * @author Mike Brock
 */
public class EntityChangeStreamImpl implements EntityChangeStream {
  private final OTEngine engine;
  private final OTEntity entity;

  private MutationType type;

  private int start = 0;
  private int cursor = 0;
  private final StringBuilder builder = new StringBuilder();

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
    checkIfMustSend(index);
    if (type == null) {
      start = index;
      type = MutationType.Insert;
    }

    builder.append(data);

    cursor = index + data.length();
  }

  @Override
  public void notifyDelete(final int index, final String data) {
    flush();

    type = MutationType.Delete;

    builder.insert(0, data);

    flush();
  }

  @Override
  public void flush() {
    if (type == null) {
      return;
    }

    engine.notifyOperation(toOperation());
  }

  private void checkIfMustSend(final int index) {
    if (type == null) {
      return;
    }

    if (type == MutationType.Insert) {
      if (index <= cursor || index > cursor + 1) {
        flush();
      }
    }
    else {
      if (index > cursor || index < cursor - 1) {
        flush();
      }
    }
  }

  private OTOperation toOperation() {

    final OTOperation operation;
    if (type == MutationType.Insert) {
      operation = createOperation(engine,
          Collections.<Mutation>singletonList(StringMutation.of(MutationType.Insert, start, builder.toString())),
          entity.getId(), entity.getRevision(), entity.getState().getHash()
      );
      builder.delete(0, builder.length());

    }
    else {
      operation = createOperation(engine,
          Collections.<Mutation>singletonList(StringMutation.of(MutationType.Delete, start, builder.toString())),
          entity.getId(), entity.getRevision(), entity.getState().getHash()
      );
    }

    start = -1;
    cursor = -1;

    type = null;

    return operation;
  }
}
