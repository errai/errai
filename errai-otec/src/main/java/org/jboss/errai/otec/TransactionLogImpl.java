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

import org.jboss.errai.otec.mutation.Mutation;
import org.jboss.errai.otec.operation.OTOperation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Mike Brock
 */
public class TransactionLogImpl implements TransactionLog {
  private final List<StateSnapshot> stateSnapshots = new ArrayList<StateSnapshot>();
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
  public List<OTOperation> getLog() {
    synchronized (transactionLog) {
      return transactionLog;
    }
  }

  @Override
  public void pruneFromOperation(final OTOperation operation) {
    synchronized (transactionLog) {
      final ListIterator<OTOperation> delIter = transactionLog.listIterator(transactionLog.indexOf(operation));
      while (delIter.hasNext()) {
        entity.decrementRevisionCounter();
        delIter.next().removeFromCanonHistory();
      }
    }
  }

  @Override
  public List<OTOperation> getLogLatestEntries(final int numberOfEntries) {
    synchronized (transactionLog) {
      return transactionLog.subList(transactionLog.size() - numberOfEntries - 1, transactionLog.size() - 1);
    }
  }

  @Override
  public List<OTOperation> getLogFromId(final int revision) {
    synchronized (transactionLog) {
      if (transactionLog.isEmpty()) {
        return Collections.emptyList();
      }

      final ListIterator<OTOperation> operationListIterator = transactionLog.listIterator(transactionLog.size());
      final List<OTOperation> operationList = new ArrayList<OTOperation>();

      while (operationListIterator.hasPrevious()) {
        final OTOperation previous = operationListIterator.previous();
        operationList.add(previous);
        if (previous.getRevision() == revision) {
          Collections.reverse(operationList);
          return operationList;
        }
      }

      if ((revision - 1) == transactionLog.get(transactionLog.size() - 1).getRevision()) {
        return Collections.emptyList();
      }
      else {
        throw new OTException("unable to find revision in log: " + revision);
      }
    }
  }

  @Override
  public List<OTOperation> getCanonLog() {
    synchronized (transactionLog) {
      final List<OTOperation> canonLog = new ArrayList<OTOperation>();
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
  public State getEffectiveStateForRevision(final int revision) {
    synchronized (transactionLog) {
      final StateSnapshot latestSnapshotState = getLatestParentSnapshot(revision);
      final State stateToTranslate = latestSnapshotState.getState().snapshot();
      final ListIterator<OTOperation> operationListIterator
          = transactionLog.listIterator(transactionLog.size());

      while (operationListIterator.hasPrevious()) {
        if (operationListIterator.previous().getRevision() == latestSnapshotState.getRevision()) {
          break;
        }
      }

      while (operationListIterator.hasNext()) {
        final OTOperation op = operationListIterator.next();
        if (!op.isCanon()) continue;

        if (op.getRevision() < revision) {
          for (final Mutation mutation : op.getMutations()) {
            mutation.apply(stateToTranslate);
          }
        }
      }

      return stateToTranslate;
    }
  }

  private StateSnapshot getLatestParentSnapshot(final int revision) {
    synchronized (transactionLog) {
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

  @Override
  public void appendLog(final OTOperation operation) {
    synchronized (transactionLog) {
      if (operation.isNoop()) {
        return;
      }

      transactionLog.add(operation);
    }
  }

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
