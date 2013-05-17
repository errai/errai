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

import java.util.*;

import org.jboss.errai.otec.client.mutation.Mutation;
import org.jboss.errai.otec.client.operation.OTOperation;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Mike Brock
 */
public class TransactionLogImpl implements TransactionLog {
  private final Object lock = new Object();

  private final List<StateSnapshot> stateSnapshots = new LinkedList<StateSnapshot>();
  private final List<OTOperation> transactionLog = new LinkedList<OTOperation>();
  private final OTEntity entity;

  private TransactionLogImpl(final OTEntity entity) {
    this.entity = entity;
    stateSnapshots.add(new StateSnapshot(entity.getRevision(), entity.getState().snapshot()));
  }

  public static TransactionLog createTransactionLog(final OTEntity entity) {
    return new TransactionLogImpl(entity);
  }

  @Override
  public Object getLock() {
    return lock;
  }

  @Override
  public List<OTOperation> getLog() {
    synchronized (lock) {
      return transactionLog;
    }
  }

  @Override
  public int purgeTo(final int revision) {
    if (revision < 0) {
      return 0;
    }
    synchronized (lock) {
      cleanLogTo(revision);

      final LogQuery effectiveStateForRevision = getEffectiveStateForRevision(revision);

      makeSnapshot(revision, effectiveStateForRevision.getEffectiveState());

      int purged = 0;
      final Iterator<OTOperation> iterator = transactionLog.iterator();
      while (iterator.hasNext()) {
        if (iterator.next().getRevision() < revision) {
          purged++;
          iterator.remove();
        }
        else {
          break;
        }
      }

      if (stateSnapshots.size() > 1) {
        final Iterator<StateSnapshot> iterator1 = stateSnapshots.iterator();
        while (iterator1.hasNext()) {
          if (iterator1.next().getRevision() < revision) {
            iterator1.remove();
          }
          else {
            break;
          }
        }
      }

      return purged;
    }
  }

  @Override
  public void pruneFromOperation(final OTOperation operation) {
    synchronized (lock) {
      final int index = transactionLog.indexOf(operation);
      if (index == -1) {
        return;
      }

      final ListIterator<OTOperation> delIter = transactionLog.listIterator(index);
      while (delIter.hasNext()) {
        entity.decrementRevisionCounter();
        final OTOperation next = delIter.next();

        final OTOperation outerPath = next.getOuterPath();
        if (outerPath != next && outerPath.getTransformedFrom() != null) {
          if (outerPath.getTransformedFrom().getRemoteOp().equals(next)) {
            outerPath.removeFromCanonHistory();
            continue;
          }
        }

        next.removeFromCanonHistory();
      }
    }
  }

  @Override
  public List<OTOperation> getLocalOpsSinceRemoteOperation(final OTOperation operation, final boolean includeNonCanon) {
    synchronized (lock) {
      if (transactionLog.isEmpty()) {
        return Collections.emptyList();
      }

      final ListIterator<OTOperation> operationListIterator = transactionLog.listIterator(transactionLog.size());
      final List<OTOperation> operationList = new ArrayList<OTOperation>();
      final int revision = operation.getRevision();

      while (operationListIterator.hasPrevious()) {
        final OTOperation previous = operationListIterator.previous();

        if (!includeNonCanon && !previous.isCanon()) {
          continue;
        }

        operationList.add(previous);

        if (previous.getRevision() == revision || previous.getRevisionHash().equals(operation.getRevisionHash())) {
          Collections.reverse(operationList);
          return operationList;
        }
      }

      if ((revision - 1) == transactionLog.get(transactionLog.size() - 1).getRevision()) {
        return Collections.emptyList();
      }
      else {
        throw new OTException("unable to find revision in log: " + revision + "; op=" + operation);
      }
    }
  }

  @Override
  public List<OTOperation> getRemoteOpsSinceRevision(final String agentId, final int rev) {
    synchronized (lock) {
      final ListIterator<OTOperation> iter = transactionLog.listIterator(transactionLog.size());

      final List<OTOperation> collect = new LinkedList<OTOperation>();
      while (iter.hasPrevious()) {
        final OTOperation previous = iter.previous();
        if (previous.getRevision() < rev) {
          break;
        }
        if (agentId.equals(previous.getAgentId())) {
          collect.add(previous);
        }
      }

      Collections.reverse(collect);

      return collect;
    }
  }

  @Override
  public List<OTOperation> getCanonLog() {
    synchronized (lock) {
      final List<OTOperation> canonLog = new ArrayList<OTOperation>(transactionLog.size());
      for (final OTOperation operation : transactionLog) {
        if (operation.isCanon()) {
          canonLog.add(operation);
        }
      }
      return canonLog;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public LogQuery getEffectiveStateForRevision(final int revision) {
    synchronized (lock) {
      final StateSnapshot latestSnapshotState = getLatestParentSnapshot(revision);
      final State stateToTranslate = latestSnapshotState.getState().snapshot();

      final ListIterator<OTOperation> operationListIterator
          = transactionLog.listIterator(transactionLog.size());

      while (operationListIterator.hasPrevious()) {
        if (operationListIterator.previous().getRevision() == latestSnapshotState.getRevision()) {
          break;
        }
      }

      final Set<OTOperation> contingent = new LinkedHashSet<OTOperation>();
      final List<OTOperation> needsMerge = new LinkedList<OTOperation>();
      while (operationListIterator.hasNext()) {
        final OTOperation op = operationListIterator.next();

        if (!op.isCanon()) {
          continue;
        }

        if (op.getRevision() < revision) {
          for (final Mutation mutation : op.getMutations()) {
            mutation.apply(stateToTranslate);
          }
          contingent.add(op.getOuterPath());
        }
        else {
          needsMerge.add(op);
        }
      }

      return new LogQuery(stateToTranslate, contingent, needsMerge);
    }
  }

  private StateSnapshot getLatestParentSnapshot(final int revision) {
    synchronized (lock) {
      final ListIterator<StateSnapshot> snapshotListIterator = stateSnapshots.listIterator(stateSnapshots.size());

      while (snapshotListIterator.hasPrevious()) {
        final StateSnapshot stateSnapshot = snapshotListIterator.previous();
        if (stateSnapshot.getRevision() <= revision) {
          return stateSnapshot;
        }
      }

      throw new RuntimeException("no parent state for: " + revision);
    }
  }

  private void makeSnapshot(final int revision, final State state) {
    stateSnapshots.add(new StateSnapshot(revision, state.getTransientState()));
    cleanLog();
  }

  @Override
  public void appendLog(final OTOperation operation) {
    synchronized (lock) {
      if (operation.isNoop()) {
        return;
      }

      transactionLog.add(operation);
    }
  }

  @Override
  public void insertLog(final int revision, final OTOperation operation) {
    synchronized (lock) {
      final ListIterator<OTOperation> operationListIterator
          = transactionLog.listIterator(transactionLog.size());

      while (operationListIterator.hasPrevious()) {
        if (operationListIterator.previous().getRevision() == revision) {
          operationListIterator.set(operation);
          break;
        }
      }
    }
  }

  @Override
  public void markDirty() {
    synchronized (lock) {
    }
  }

  @Override
  public void snapshot() {
    makeSnapshot(entity.getRevision(), entity.getState());
  }

  @Override
  public void cleanLog() {
    cleanLogTo(entity.getRevision());
  }

  private void cleanLogTo(final int rev) {
    synchronized (lock) {
      final Set<OTOperation> applied = new HashSet<OTOperation>();

      final Iterator<OTOperation> iterator = transactionLog.iterator();
      while (iterator.hasNext()) {
        final OTOperation next = iterator.next();
        if (next.getRevision() > rev) {
          return;
        }

        if (!next.isCanon() || applied.contains(next)) {
          iterator.remove();
        }
        else {
          applied.add(next.getOuterPath());
        }
      }
    }
  }

  @Override
  public String toString() {
    return Arrays.toString(getCanonLog().toArray());
  }

  private static class StateSnapshot {
    private final int revision;
    private final State state;

    private StateSnapshot(final int revision, final State state) {
      this.revision = revision;
      this.state = state;
    }

    private int getRevision() {
      return revision;
    }

    private State getState() {
      return state;
    }
  }
}
