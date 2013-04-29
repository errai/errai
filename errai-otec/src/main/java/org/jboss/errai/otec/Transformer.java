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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jboss.errai.otec.mutation.CharacterMutation;
import org.jboss.errai.otec.mutation.Mutation;
import org.jboss.errai.otec.mutation.MutationType;
import org.jboss.errai.otec.operation.OTOperation;
import org.jboss.errai.otec.operation.OTOperationImpl;
import org.jboss.errai.otec.operation.OpPair;
import org.jboss.errai.otec.util.OTLogFormat;

/**
 * @author Mike Brock
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class Transformer {
  private final OTEngine engine;
  private final boolean remoteWins;
  private final OTEntity entity;
  private final OTOperation remoteOp;

  private Transformer(final OTEngine engine, final boolean remoteWins, final OTEntity entity,
                      final OTOperation remoteOp) {
    this.engine = engine;
    this.remoteWins = remoteWins;
    this.entity = entity;
    this.remoteOp = remoteOp;
  }

  public static Transformer createTransformerLocalPrecedence(final OTEngine engine, final OTEntity entity,
                                                             final OTOperation operation) {
    return new Transformer(engine, false, entity, operation);
  }

  public static Transformer createTransformerRemotePrecedence(final OTEngine engine, final OTEntity entity,
                                                              final OTOperation operation) {
    return new Transformer(engine, true, entity, operation);
  }

  @SuppressWarnings("unchecked")
  public OTOperation transform() {
    final TransactionLog transactionLog = entity.getTransactionLog();
    final List<OTOperation> localOps = transactionLog.getLogFromId(remoteOp.getRevision());

    if (localOps.isEmpty()) {
      OTOperationImpl.createOperation(remoteOp).apply(entity);
      return remoteOp;
    }
    else {
      if (localOps.size() > 1) {
        entity.getState().syncStateFrom(transactionLog.getEffectiveStateForRevision(remoteOp.getRevision() + 1));

        assert OTLogFormat.log("REWIND",
            "<<>>",
            "-",
            engine.getName(),
            remoteOp.getRevision() + 1,
            "\"" + entity.getState().get() + "\"");

        transactionLog.pruneFromOperation(localOps.get(1));
      }

      boolean first = true;
      boolean appliedRemoteOp = false;
      OTOperation applyOver = remoteOp;
      for (final OTOperation localOp : localOps) {
        if (first) {
          first = false;
          if (applyOver.getRevisionHash().equals(localOp.getRevisionHash())) {
            applyOver = transform(applyOver, localOp);
          }
          else {
            applyOver = transform(applyOver,
                transform(localOp.getTransformedFrom().getLocalOp(), localOp.getTransformedFrom().getRemoteOp()));
          }
        }
        else {
          final OTOperation ot = transform(localOp, applyOver);

          if (!appliedRemoteOp && !localOp.equals(ot)) {
            applyOver.apply(entity);
            appliedRemoteOp = true;
          }

          applyOver = transform(applyOver, ot);
          ot.apply(entity);
        }
      }

      if (!appliedRemoteOp) {
        applyOver = OTOperationImpl.createOperation(applyOver);
        applyOver.apply(entity);
      }

      if (applyOver.isResolvedConflict()) {
        return applyOver;
      }
      else {
        return remoteOp;
      }
    }
  }

  private OTOperation transform(final OTOperation remoteOp, final OTOperation localOp) {
    final OTOperation transformedOp;
    final List<Mutation> remoteMutations = remoteOp.getMutations();
    final List<Mutation> localMutations = localOp.getMutations();
    final List<Mutation> transformedMutations = new ArrayList<Mutation>(remoteMutations.size());

    final Iterator<Mutation> remoteOpMutations;
    final Iterator<Mutation> localOpMutations;

    if (remoteMutations.size() > localMutations.size()) {
      remoteOpMutations = noopPaddedIterator(remoteMutations, remoteMutations.size());
      localOpMutations = noopPaddedIterator(localMutations, remoteMutations.size());
    }
    else if (remoteMutations.size() < localMutations.size()) {
      remoteOpMutations = noopPaddedIterator(remoteMutations, localMutations.size());
      localOpMutations = noopPaddedIterator(localMutations, localMutations.size());
    }
    else {
      remoteOpMutations = remoteMutations.iterator();
      localOpMutations = localMutations.iterator();
    }

    int offset = 0;
    boolean resolvesConflict = false;

    while (remoteOpMutations.hasNext()) {
      final Mutation rm = remoteOpMutations.next();
      final Mutation lm = localOpMutations.next();

      final int rmIdx = rm.getPosition();
      final int diff = rmIdx - lm.getPosition();

      if (diff < 0) {
        if (rm.getType() != MutationType.Noop) {
          transformedMutations.add(rm);
        }
      }
      else if (diff == 0) {
        if (remoteOp.getRevision() != localOp.getRevision() && !remoteOp.isResolvedConflict()) {
          transformedMutations.add(rm);
        }
        else {
          switch (rm.getType()) {
          case Insert:
            if (!remoteWins && lm.getType() == MutationType.Insert) {
              offset += lm.length();
            }
            resolvesConflict = true;
            break;
          case Delete:
            if (lm.getType() == MutationType.Insert) {
              offset += lm.length();
              resolvesConflict = true;
            }
            break;
          }
          if (resolvesConflict) {
            transformedMutations.add(adjustMutationToIndex(rmIdx + offset, rm));
          }
        }
      }
      else if (diff > 0) {
        if (lm.getType() != MutationType.Noop && !localOp.isResolvedConflict()) {
          switch (rm.getType()) {
          case Insert:
            if (lm.getType() == MutationType.Insert) {
              offset += lm.length();
            }
            if (lm.getType() == MutationType.Delete) {
              offset -= lm.length();
            }
            break;
          case Delete:
            if (lm.getType() == MutationType.Insert) {
              offset += lm.length();
            }
            if (lm.getType() == MutationType.Delete) {
              offset -= lm.length();
            }
            break;
          }
        }

        transformedMutations.add(adjustMutationToIndex(rmIdx + offset, rm));
      }
    }

    transformedOp =
        OTOperationImpl.createLocalOnlyOperation(engine, transformedMutations, entity.getId(), entity.getRevision(),
            entity.getState().getHash(),
            OpPair.of(remoteOp, localOp));

    if (resolvesConflict || remoteOp.isResolvedConflict()) {
      transformedOp.markAsResolvedConflict();
    }

    assert OTLogFormat.log("TRANSFORM",
        remoteOp + " , " + localOp + " -> " + transformedOp,
        "-",
        engine.getName(),
        remoteOp.getRevision(),
        "\"" + entity.getState().get() + "\"");

    return transformedOp;
  }

  private static Iterator<Mutation> noopPaddedIterator(final List<Mutation> mutationList, final int largerSize) {
    return new Iterator<Mutation>() {
      int pos = 0;
      final CharacterMutation paddedMutation = CharacterMutation.noop(mutationList.get(mutationList.size() - 1)
          .getPosition());
      final Iterator<Mutation> iteratorDelegate = mutationList.iterator();

      @Override
      public boolean hasNext() {
        return pos < largerSize;
      }

      @Override
      public Mutation next() {
        if (pos++ < mutationList.size()) {
          return iteratorDelegate.next();
        }
        else {
          return paddedMutation;
        }
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }
  
  private Mutation adjustMutationToIndex(int idx, Mutation mutation) {
    if (idx == mutation.getPosition()) {
      return mutation;
    }
    else {
      return mutation.newBasedOn(idx);
    }
  }
}
