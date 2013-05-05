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

import static org.jboss.errai.otec.client.operation.OTOperationImpl.createLocalOnlyOperation;

import org.jboss.errai.otec.client.mutation.CharacterMutation;
import org.jboss.errai.otec.client.mutation.Mutation;
import org.jboss.errai.otec.client.mutation.MutationType;
import org.jboss.errai.otec.client.operation.OTOperation;
import org.jboss.errai.otec.client.operation.OTOperationImpl;
import org.jboss.errai.otec.client.operation.OpPair;
import org.jboss.errai.otec.client.util.OTLogUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author Mike Brock
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class Transformer {
  private final OTEngine engine;
  private final boolean remoteWins;
  private final OTEntity entity;
  private final OTOperation remoteOp;

  private Transformer(final OTEngine engine,
                      final boolean remoteWins,
                      final OTEntity entity,
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
    final List<OTOperation> localOps;
    try {
      localOps = transactionLog.getLogFromId(remoteOp.getRevision(), true);
    }
    catch (OTException e) {
      e.printStackTrace();
      throw new BadSync("", entity.getId(), remoteOp.getAgentId());

      //LogUtil.log("failed while trying to transform: " + remoteOp + " rev:" + remoteOp.getRevision());
    }


    if (localOps.isEmpty()) {
      OTOperationImpl.createOperation(remoteOp).apply(entity);
      return remoteOp;
    }
    else {
      if (localOps.size() > 1) {
        entity.getState().syncStateFrom(transactionLog.getEffectiveStateForRevision(remoteOp.getRevision() + 1));

        assert OTLogUtil.log("REWIND",
            "<<>>",
            "-",
            engine.getName(),
            remoteOp.getRevision() + 1,
            "\"" + entity.getState().get() + "\"");


        transactionLog.pruneFromOperation(localOps.get(1));

        for (final OTOperation op : localOps) {
          if (op.getOuterPath() != op) {
            op.getOuterPath().invalidate();
          }
        }
      }

      boolean first = true;
      boolean appliedRemoteOp = false;
      OTOperation applyOver = remoteOp;
      OTOperation localOpPrime = null;
      final Set<OTOperation> exclude = new HashSet<OTOperation>();
      for (final OTOperation localOp : localOps) {
        if (!localOp.isValid() || exclude.contains(localOp)) {
          continue;
        }

        if (first) {
          first = false;
          if (applyOver.getRevisionHash().equals(localOp.getRevisionHash()) || localOp.isResolvedConflict()) {
            applyOver = transform(applyOver, localOp);
          }
          else {
            if (applyOver.getTransformedFrom() == null) {
              // we have a history divergence that we now must deal with.
              final List<OTOperation> previousRemoteOpsTo = transactionLog.getPreviousRemoteOpsTo(applyOver, localOp);
              boolean appliedLocal = false;

              localOpPrime = localOp;
              for (final OTOperation operation : previousRemoteOpsTo) {
                exclude.add(operation);
                final OTOperation replayRemoteOp = OTOperationImpl.createOperation(operation);

                if (transform(localOp, replayRemoteOp).equals(localOp)) {
                  localOpPrime.apply(entity);
                  appliedLocal = true;
                }

                replayRemoteOp.apply(entity);
              }
              applyOver = transform(applyOver, localOp);

              if (!appliedLocal) {
                localOpPrime = transform(localOpPrime, applyOver);
                localOpPrime.apply(entity);
              }

              applyOver = transform(applyOver, localOp);
              applyOver.apply(entity);
              appliedRemoteOp = true;
            }
            else {
              //  final OTOperation outerPath = localOp.getOuterPath();
              applyOver = transform(applyOver,
                  transform(localOp.getTransformedFrom().getLocalOp(),
                      localOp.getTransformedFrom().getRemoteOp()));
            }
          }
        }
        else {
          OTOperation ot = transform(localOp, applyOver);
          if (localOpPrime != null) {
            ot = transform(ot, localOpPrime);
          }

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

      final TransactionLog transactionLog = entity.getTransactionLog();
      if (diff < 0) {
        if (rm.getType() == MutationType.Delete) {
          if (rm.getPosition() + rm.length() > lm.getPosition()) {
            if (lm.getType() == MutationType.Insert) {
              // uh-oh.. our local insert is inside the range of this remote delete ... move the insert
              // to the beginning of the delete range to resolve this.

              final State rewind
                  = transactionLog.getEffectiveStateForRevision(localOp.getRevision());
              localOp.removeFromCanonHistory();
              transactionLog.markDirty();
              final Mutation mutation = lm.newBasedOn(rm.getPosition());
              mutation.apply(rewind);
              final OTOperation localOnlyOperation = createLocalOnlyOperation(engine, remoteOp.getAgentId(), Collections.singletonList(mutation), entity, localOp.getRevision(), OpPair.of(remoteOp, localOp));
              transactionLog.insertLog(localOp.getRevision(), localOnlyOperation);

              entity.getState().syncStateFrom(rewind);

              transformedMutations.add(rm.newBasedOn(mutation.getPosition() + lm.length()));

              continue;

              //transformedMutations.add(StringMutation.of(MutationType.Insert, rm.g));
            }
            else if (lm.getType() == MutationType.Delete) {
              final int truncate = lm.getPosition() - rm.getPosition();
              final Mutation mutation = rm.newBasedOn(rm.getPosition(), truncate);

              transformedMutations.add(mutation);
              resolvesConflict = true;

              continue;
            }
          }

        }

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
                if (remoteWins && (lm.getPosition() + lm.length()) > rm.getPosition()) {
                  final State rewind
                      = transactionLog.getEffectiveStateForRevision(localOp.getRevision());
                  final Mutation mutation = rm.newBasedOn(lm.getPosition());
                  //transactionLog.pruneFromOperation(localOp);

                  localOp.removeFromCanonHistory();
                  transactionLog.markDirty();

                  mutation.apply(rewind);
                  entity.getState().syncStateFrom(rewind);

                  transactionLog.insertLog(remoteOp.getRevision(),
                      createLocalOnlyOperation(engine, remoteOp.getAgentId(), Collections.singletonList(mutation), entity, remoteOp.getRevision(), OpPair.of(remoteOp, localOp)));
                  transformedMutations.add(lm.newBasedOn(mutation.getPosition() + rm.length()));

                  continue;
                }

                offset -= lm.length();
              }
              break;
            case Delete:
              if (lm.getType() == MutationType.Insert) {
                offset += lm.length();
              }
              if (lm.getType() == MutationType.Delete) {
                if (remoteWins && (lm.getPosition() + lm.length()) > rm.getPosition()) {
                  final State rewind
                      = transactionLog.getEffectiveStateForRevision(localOp.getRevision());

                  rm.apply(rewind);

                  transactionLog.insertLog(localOp.getRevision(),
                      remoteOp);

                  localOp.removeFromCanonHistory();

                  entity.getState().syncStateFrom(rewind);

                  final int truncate = rm.getPosition() - lm.getPosition();
                  final Mutation mutation = lm.newBasedOn(lm.getPosition(), truncate);

                  transformedMutations.add(mutation);
                  resolvesConflict = true;

                  continue;
                }

                offset -= lm.length();
              }
              break;
          }
        }

        transformedMutations.add(adjustMutationToIndex(rmIdx + offset, rm));
      }
    }

    transformedOp =
        createLocalOnlyOperation(engine, remoteOp.getAgentId(), transformedMutations, entity,
            OpPair.of(remoteOp, localOp));

    remoteOp.setOuterPath(transformedOp);

    if (resolvesConflict || remoteOp.isResolvedConflict()) {
      transformedOp.markAsResolvedConflict();
    }

    assert OTLogUtil.log("TRANSFORM",
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

  public static Collection<OTOperation> opCombinitator(OTEngine engine, List<OTOperation> toCombine) {
    if (toCombine.size() == 1) {
      return toCombine;
    }

    final List<Mutation> mutationList = new ArrayList<Mutation>();
    for (final OTOperation op : toCombine) {
      mutationList.addAll(op.getMutations());
    }

    final Mutation combined = mutationCombinitator(mutationList);

    if (combined == null) {
      return toCombine;
    }
    else {
      final List<OTOperation> combinedOps = new ArrayList<OTOperation>();
      final OTOperation operation
          = OTOperationImpl.createOperation(engine,
          toCombine.get(0).getAgentId(),
          Collections.singletonList(combined),
          toCombine.get(0).getEntityId(),
          -1,
          toCombine.get(0).getRevisionHash());

      return Collections.singletonList(operation);
    }
  }

  @SuppressWarnings("unchecked")
  public static Mutation mutationCombinitator(final Collection<Mutation> toCombine) {
    Mutation last = null;
    for (final Mutation m : toCombine) {
      if (last != null) {
        last = m.combineWith(last);
        if (last == null) {
          return null;
        }
      }
      else {
        last = m;
      }
    }

    return last;
  }

  private Mutation adjustMutationToIndex(int idx, Mutation mutation) {
    return (idx == mutation.getPosition()) ? mutation : mutation.newBasedOn(idx);
  }

  public String toString() {
    return String.valueOf(entity.getState().get());
  }
}
