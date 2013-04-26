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

import java.util.Arrays;
import java.util.List;

import org.jboss.errai.otec.OTEngine;
import org.jboss.errai.otec.OTEntity;
import org.jboss.errai.otec.mutation.Mutation;
import org.jboss.errai.otec.util.OTLogFormat;

/**
 * @author Christian Sadilek
 * @author Mike Brock
 */
public class OTOperationImpl implements OTOperation {
  private final OTEngine engine;
  private final List<Mutation> mutations;
  private final int entityId;
  private final boolean propagate;
  private boolean resolvedConflict;
  private String revisionHash;
  private boolean nonCanon;
  private int revision;

  private final OpPair transformedFrom;

  private OTOperationImpl(final OTEngine engine, final List<Mutation> mutationList,
                          final int entityId,
                          final int revision,
                          final String revisionHash,
                          final OpPair transformedFrom,
                          final boolean propagate) {

    this.engine = engine;
    this.mutations = mutationList;
    this.entityId = entityId;
    this.revision = revision;
    this.revisionHash = revisionHash;
    this.transformedFrom = transformedFrom;
    this.propagate = propagate;
  }

  public static OTOperation createOperation(final OTEngine engine,
                                            final List<Mutation> mutationList,
                                            final int entityId,
                                            final int revision,
                                            final String revisionHash,
                                            final OpPair transformedFrom) {

    return new OTOperationImpl(engine, mutationList, entityId, revision, revisionHash, transformedFrom, true);
  }

  public static OTOperation createLocalOnlyOperation(final OTEngine engine,
                                                     final List<Mutation> mutationList,
                                                     final int entityId,
                                                     final int revision,
                                                     final String revisionHash,
                                                     final OpPair transformedFrom) {

    return new OTOperationImpl(engine, mutationList, entityId, revision, revisionHash, transformedFrom, false);
  }

  public static OTOperation createLocalOnlyOperation(final OTEngine engine, final OTOperation operation) {
    return new OTOperationImpl(engine, operation.getMutations(), operation.getEntityId(), operation.getRevision(),
        operation.getRevisionHash(), operation.getTransformedFrom(), false);
  }
  
  public static OTOperation createLocalOnlyOperation(final OTOperation operation) {
    return new OTOperationImpl(operation.getEngine(), operation.getMutations(), operation.getEntityId(), -1,
        operation.getRevisionHash(), operation.getTransformedFrom(), false);
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
    revisionHash = entity.getState().getHash();
    if (revision == -1) {
      revision = entity.getRevision();
    }

    if (nonCanon)
      return shouldPropagate();

    for (final Mutation mutation : mutations) {
      mutation.apply(entity.getState());
    }

    OTLogFormat.log("APPLY", toString(), "-", engine.toString(), revision, "\"" + entity.getState().get() + "\"");
    entity.getTransactionLog().appendLog(this);
    entity.incrementRevision();
    return shouldPropagate();
  }

  @Override
  public void removeFromCanonHistory() {
    nonCanon = true;
  }

  @Override
  public void markAsResolvedConflict() {
    resolvedConflict = true;
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
  public boolean isResolvedConflict() {
    return resolvedConflict;
  }

  @Override
  public OTOperation getBasedOn(final int revision) {
    return new OTOperationImpl(engine, mutations, entityId, revision, revisionHash, transformedFrom, propagate);
  }

  @Override
  public OpPair getTransformedFrom() {
    return transformedFrom;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o)
      return true;
    if (!(o instanceof OTOperationImpl))
      return false;

    final OTOperationImpl that = (OTOperationImpl) o;

    return entityId == that.entityId && mutations.equals(that.mutations);
  }

  @Override
  public int hashCode() {
    int result = mutations.hashCode();
    result = 31 * result + entityId;
    return result;
  }

  @Override
  public String toString() {
    return Arrays.toString(mutations.toArray());
  }

  @Override
  public String getRevisionHash() {
    return revisionHash;
  }
}
