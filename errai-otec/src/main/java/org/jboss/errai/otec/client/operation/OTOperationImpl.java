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

package org.jboss.errai.otec.client.operation;

import java.util.Arrays;
import java.util.List;

import org.jboss.errai.otec.client.OTEngine;
import org.jboss.errai.otec.client.OTEntity;
import org.jboss.errai.otec.client.OTException;
import org.jboss.errai.otec.client.State;
import org.jboss.errai.otec.client.mutation.Mutation;
import org.jboss.errai.otec.client.util.OTLogUtil;

/**
 * @author Christian Sadilek
 * @author Mike Brock
 */
public class OTOperationImpl implements OTOperation {
  private final OTEngine engine;
  private final String agentId;
  private final List<Mutation> mutations;
  private final int entityId;
  private final boolean propagate;
  private boolean resolvedConflict;
  private String revisionHash;
  private boolean nonCanon;
  private boolean invalid = false;
  private int revision;
  private final int lastRevisionTx;
  private OTOperation outerPath;

  private final OpPair transformedFrom;

  private OTOperationImpl(final OTEngine engine,
                          final String agentId,
                          final List<Mutation> mutationList,
                          final int entityId,
                          final int revision,
                          final String revisionHash,
                          final OpPair transformedFrom,
                          final boolean propagate,
                          final boolean resolvedConflict,
                          final int lastRevisionTx,
                          final OTOperation outerPath) {

    this.engine = engine;
    this.agentId = agentId;
    this.mutations = mutationList;
    this.entityId = entityId;
    this.revision = revision;
    this.revisionHash = revisionHash;
    this.transformedFrom = transformedFrom;
    this.propagate = propagate;
    this.resolvedConflict = resolvedConflict;
    this.lastRevisionTx = lastRevisionTx;
    this.outerPath = outerPath == null ? this : outerPath;
  }

  private OTOperationImpl(final OTEngine engine,
                          final String agentId,
                          final List<Mutation> mutationList,
                          final int entityId,
                          final int revision,
                          final String revisionHash,
                          final OpPair transformedFrom,
                          final boolean propagate,
                          final boolean resolvedConflict,
                          final int lastRevisionTx) {
    this(engine, agentId, mutationList, entityId, revision, revisionHash, transformedFrom, propagate, resolvedConflict,
        lastRevisionTx, null);

  }

  public static OTOperation createLocalOnlyOperation(final OTEngine engine,
                                                     final String agentId,
                                                     final List<Mutation> mutationList,
                                                     final OTEntity entity,
                                                     final OpPair pair) {
    return new OTOperationImpl(engine, agentId, mutationList, entity.getId(), entity.getRevision(), entity.getState()
        .getHash(), pair, false, false, -1);

  }

  public static OTOperation createLocalOnlyOperation(final OTEngine engine,
                                                     final String agentId,
                                                     final List<Mutation> mutationList,
                                                     final OTEntity entity,
                                                     final int revision,
                                                     final OpPair pair) {

    return new OTOperationImpl(engine, agentId, mutationList, entity.getId(), revision, entity.getState().getHash(),
        pair, false, false, -1);

  }

  public static OTOperation createOperation(final OTEngine engine,
                                            final String agentId,
                                            final List<Mutation> mutationList,
                                            final int entityId,
                                            final int revision,
                                            final String revisionHash) {
    return new OTOperationImpl(engine, agentId, mutationList, entityId, revision, revisionHash, null, true, false, -1);

  }

  public static OTOperation createOperation(final OTEngine engine,
                                            final String agentId,
                                            final List<Mutation> mutationList,
                                            final int entityId,
                                            final int revision,
                                            final String revisionHash,
                                            final OpPair transformedFrom,
                                            final int lastRevisionTx) {

    return new OTOperationImpl(engine, agentId, mutationList, entityId, revision, revisionHash, transformedFrom, true,
        false, lastRevisionTx);
  }

  public static OTOperation createLocalOnlyOperation(final OTEngine engine,
                                                     final OTOperation operation) {
    return new OTOperationImpl(engine, operation.getAgentId(), operation.getMutations(), operation.getEntityId(),
        operation.getRevision(),
        operation.getRevisionHash(), operation.getTransformedFrom(), false, operation.isResolvedConflict(), -1);
  }

  public static OTOperation createLocalOnlyOperation(final OTEngine engine,
      final OTOperation operation, final int lastRevisionTx) {
    return new OTOperationImpl(engine, operation.getAgentId(), operation.getMutations(), operation.getEntityId(),
        operation.getRevision(),
        operation.getRevisionHash(), operation.getTransformedFrom(), false, operation.isResolvedConflict(), lastRevisionTx);
  }

  public static OTOperation createOperation(final OTOperation op) {
    return new OTOperationImpl(op.getEngine(), op.getAgentId(), op.getMutations(), op.getEntityId(), -1,
        op.getRevisionHash(), op.getTransformedFrom(), op.shouldPropagate(), op.isResolvedConflict(), -1);
  }

  public static OTOperation createOperation(final OTOperation op, final OpPair transformedFrom) {
    return new OTOperationImpl(op.getEngine(), op.getAgentId(), op.getMutations(), op.getEntityId(), -1,
        op.getRevisionHash(), transformedFrom, op.shouldPropagate(), op.isResolvedConflict(), -1);
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

  @Override
  public String getAgentId() {
    return agentId;
  }

  @Override
  public boolean apply(OTEntity entity) {
    return apply(entity, false);
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean apply(final OTEntity entity, final boolean transiently) {
    try {
      revisionHash = entity.getState().getHash();
      if (revision == -1) {
        revision = entity.getRevision();
      }

      if (nonCanon)
        return shouldPropagate();

      final State state = entity.getState();

      for (final Mutation mutation : mutations) {
        mutation.apply(state);
      }

      OTLogUtil.log("APPLY", toString(), "-", engine.toString(), revision, "\"" + entity.getState().get() + "\"");

      if (transformedFrom != null) {
        transformedFrom.getRemoteOp().setOuterPath(this);
      }

      if (!transiently) {
        entity.getTransactionLog().appendLog(this);
        entity.incrementRevision();
      }

      return shouldPropagate();
    }
    catch (Throwable t) {
      // t.printStackTrace();
      throw new OTException("failed to apply op", t);
    }
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
  public void unmarkAsResolvedConflict() {
    resolvedConflict = false;
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
    return new OTOperationImpl(engine, agentId, mutations, entityId, revision, revisionHash, transformedFrom,
        propagate, resolvedConflict, lastRevisionTx, outerPath);
  }

  @Override
  public OpPair getTransformedFrom() {
    return transformedFrom;
  }

  @Override
  public void setOuterPath(final OTOperation outerPath) {
    // if (this.outerPath != this) {
    // this.outerPath.setOuterPath(outerPath);
    // }

    this.outerPath = outerPath;
  }

  @Override
  public OTOperation getOuterPath() {
    return outerPath;
  }

  @Override
  public boolean isValid() {
    return !invalid;
  }

  @Override
  public void invalidate() {
    invalid = true;
  }

  @Override
  public int compareTo(OTOperation o) {
    return revision - o.getRevision();
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

  @Override
  public int getLastRevisionTx() {
    return lastRevisionTx;
  }
}