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

package org.jboss.errai.otec.operation;

import org.jboss.errai.otec.OTEngine;
import org.jboss.errai.otec.OTEntity;
import org.jboss.errai.otec.mutation.Mutation;

import java.util.Arrays;
import java.util.List;

/**
 * @author Christian Sadilek
 * @author Mike Brock
 */
public class OTOperationImpl implements OTOperation {
  private final OTEngine engine;
  private final List<Mutation> mutations;
  private final int entityId;
  private final int revision;
  private final boolean propagate;

  private boolean nonCanon;

  private OTOperationImpl(OTEngine engine, final List<Mutation> mutationList,
                          final int entityId,
                          final int revision,
                          final boolean propagate) {
    this.engine = engine;
    this.mutations = mutationList;
    this.entityId = entityId;
    this.revision = revision;
    this.propagate = propagate;
  }


  public static OTOperation createOperation(final OTEngine engine,
                                            final List<Mutation> mutationList,
                                            final int entityId) {


    return createOperation(engine, mutationList, entityId, -1);
  }

  public static OTOperation createOperation(final OTEngine engine,
                                            final List<Mutation> mutationList,
                                            final int entityId,
                                            final int revision) {

    return new OTOperationImpl(engine, mutationList, entityId, revision, true);
  }

  public static OTOperation createLocalOnlyOperation(final OTEngine engine,
                                                     final List<Mutation> mutationList,
                                                     final int entityId,
                                                     final int revision) {

    return new OTOperationImpl(engine, mutationList, entityId, revision, false);
  }

  public static OTOperation createLocalOnlyOperation(final OTEngine engine, final OTOperation operation) {
    return new OTOperationImpl(engine, operation.getMutations(), operation.getEntityId(), operation.getRevision(), false);
  }

  @Override
  public List<Mutation> getMutations() {
    return mutations;
  }

  @Override
  public int getEntityId() {
    return entityId;
  }

  @Override
  public int getRevision() {
    return revision;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean apply(final OTEntity entity) {
    if (nonCanon)
      return shouldPropagate();

    for (final Mutation mutation : mutations) {
      mutation.apply(entity.getState());
    }

    System.out.println("APPLY: " + toString() + "; on=" + engine + "; stateResult=[\"" + entity.getState().get() + "\"]");
    entity.incrementRevision();

    return shouldPropagate();
  }

  @Override
  public void removeFromCanonHistory() {
    nonCanon = true;
  }

  @Override
  public boolean isCanon() {
    return !nonCanon;
  }

  @Override
  public boolean shouldPropagate() {
    return propagate;
  }

  @Override
  public OTEngine getEngine() {
    return engine;
  }

  @Override
  public boolean isNoop() {
    return mutations.isEmpty();
  }

  @Override
  public OTOperation getBasedOn(final int revision) {
    return new OTOperationImpl(engine, mutations, entityId, revision, propagate);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof OTOperationImpl)) return false;

    OTOperationImpl that = (OTOperationImpl) o;

    if (entityId != that.entityId) return false;
    if (mutations != null ? !mutations.equals(that.mutations) : that.mutations != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = mutations != null ? mutations.hashCode() : 0;
    result = 31 * result + entityId;
    return result;
  }

  public String toString() {
    return Arrays.toString(mutations.toArray());
  }
}
