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

import org.jboss.errai.otec.mutation.CharacterData;
import org.jboss.errai.otec.mutation.IndexPosition;
import org.jboss.errai.otec.mutation.Mutation;
import org.jboss.errai.otec.mutation.MutationType;
import org.jboss.errai.otec.mutation.StringMutation;
import org.jboss.errai.otec.operation.OTOperation;
import org.jboss.errai.otec.operation.OTOperationImpl;
import org.jboss.errai.otec.operation.OpPair;
import org.jboss.errai.otec.util.OTLogFormat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Mike Brock
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class Transformer {
  private final OTEngine engine;
  private final boolean remoteWins;
  private final OTEntity entity;
  private final OTPeer peer;
  private final OTOperation remoteOp;

  private Transformer(final OTEngine engine, final boolean remoteWins, final OTEntity entity, final OTPeer peer,
                      final OTOperation remoteOp) {
    this.engine = engine;
    this.remoteWins = remoteWins;
    this.entity = entity;
    this.peer = peer;
    this.remoteOp = remoteOp;
  }

  public static Transformer createTransformerLocalPrecedence(final OTEngine engine, final OTEntity entity,
                                                             final OTPeer peer, final OTOperation operation) {
    return new Transformer(engine, false, entity, peer, operation);
  }

  public static Transformer createTransformerRemotePrecedence(final OTEngine engine, final OTEntity entity,
                                                              final OTPeer peer, final OTOperation operation) {
    return new Transformer(engine, true, entity, peer, operation);
  }

  @SuppressWarnings("unchecked")
  public List<OTOperation> transform() {
    final List<OTOperation> transformedOps = new ArrayList<OTOperation>();
    final TransactionLog transactionLog = entity.getTransactionLog();
    final List<OTOperation> localOps = transactionLog.getLogFromId(remoteOp.getRevision());

    // if no operation was carried out we can just apply the new operations
    if (localOps.isEmpty()) {
      remoteOp.apply(entity);
      transactionLog.appendLog(remoteOp);
      transformedOps.add(remoteOp);
    }
    else {
      if (localOps.size() > 1) {
        final State revState = transactionLog.getEffectiveStateForRevision(remoteOp.getRevision() + 1);
        entity.getState().syncStateFrom(revState);

        System.out.printf(OTLogFormat.LOG_FORMAT,
            "REWIND",
            "<<>>",
            engine.getEngineName(),
            engine.getEngineName(),
            remoteOp.getRevision(),
            "\"" + entity.getState().get() + "\"");

        transactionLog.pruneFromOperation(localOps.get(1));
      }

      OTOperation applyOver = remoteOp;
      int idx = 0;
      for (final OTOperation localOp : localOps) {
        if (idx++ == 0) {
          if (applyOver.getRevisionHash().equals(localOp.getRevisionHash())) {
            applyOver = transform(applyOver, localOp);
          }
          else {
            applyOver = transform(applyOver,
                transform(localOp.getTransformedFrom().getLocalOp(), localOp.getTransformedFrom().getRemoteOp()));
          }

          applyOver.apply(entity);
          transactionLog.appendLog(applyOver);
        }
        else {
          final OTOperation ot = transform(localOp, applyOver);
          if (idx != localOps.size()) {
            applyOver = transform(applyOver, ot);
          }

          ot.apply(entity);
          transactionLog.appendLog(ot);
        }
      }

      if (applyOver.isResolvedConflict()) {
        transformedOps.add(applyOver);
      }
      else {
        transformedOps.add(remoteOp);
      }
    }

    return transformedOps;
  }

  private OTOperation transform(final OTOperation remoteOp, final OTOperation localOp) {
    OTOperation transformedOp = null;
    final List<Mutation> transformedMutations = new ArrayList<Mutation>();
    List<Mutation> remoteMutations = remoteOp.getMutations();
    List<Mutation> localMutations = localOp.getMutations();

    if (remoteMutations.size() > localMutations.size()) {
      localMutations = createPadded(localMutations, remoteMutations.size());
    }
    else if (remoteMutations.size() < localMutations.size()) {
      remoteMutations = createPadded(remoteMutations, localMutations.size());
    }

    final Iterator<Mutation> remoteOpMutations = remoteMutations.iterator();
    final Iterator<Mutation> localOpMutations = localMutations.iterator();

    int offset = 0;
    while (remoteOpMutations.hasNext()) {
      final Mutation rm = remoteOpMutations.next();
      final Mutation lm = localOpMutations.next();

      final IndexPosition rmIdx = (IndexPosition) rm.getPosition();
      final IndexPosition lmIdx = (IndexPosition) lm.getPosition();

      final int diff = rmIdx.getPosition() - lmIdx.getPosition();

      if (diff < 0) {
        transformedMutations.add(rm);
      }
      else if (diff == 0) {
        boolean doTransform = true;
        switch (rm.getType()) {
          case Insert:
            if (!remoteWins && lm.getType() == MutationType.Insert) {
              offset++;
            }
            break;
          case Delete:
            if (lm.getType() == MutationType.Insert) {
              offset++;
            }
            else if (lm.getType() == MutationType.Delete) {
              doTransform = false;
            }
            break;
        }
        if (doTransform) {
          transformedMutations.add(new StringMutation(rm.getType(), IndexPosition.of(rmIdx.getPosition() + offset),
              (CharacterData) rm.getData()));
        }
      }
      else if (diff >= 0) {
        if (lm.getType() != MutationType.Noop) {
          switch (rm.getType()) {
            case Insert:
              if (lm.getType() == MutationType.Insert) {
                offset++;
              }
              if (lm.getType() == MutationType.Delete) {
                offset--;
              }
              break;
            case Delete:
              if (lm.getType() == MutationType.Insert) {
                offset++;
              }
              if (lm.getType() == MutationType.Delete) {
                offset--;
              }
              break;
          }
        }

        transformedMutations.add(new StringMutation(rm.getType(), IndexPosition.of(rmIdx.getPosition() + offset),
            (CharacterData) rm.getData()));
      }

      transformedOp =
          OTOperationImpl.createLocalOnlyOperation(engine, transformedMutations, entity.getId(), entity.getRevision(),
              entity.getState().getStateId(),
              OpPair.of(remoteOp, localOp));

      if (!remoteWins && diff == 0) {
        transformedOp.markAsResolvedConflict();
      }
    }

    System.out.printf(OTLogFormat.LOG_FORMAT,
           "TRANSFORM",
           remoteOp + "→" + transformedOp,
           "-",
           engine.getEngineName(),
           remoteOp.getRevision(),
           "\"" + entity.getState().get() + "\"");

    return transformedOp;
  }

  private static List<Mutation> createPadded(final List<Mutation> mutationList, final int largerSize) {
    int paddingSize = largerSize - mutationList.size();

    final List<Mutation> mutations = new ArrayList<Mutation>(mutationList);
    final IndexPosition lastPosition = (IndexPosition) mutationList.get(mutationList.size() - 1).getPosition();
    for (int i = 0; i < paddingSize; i++) {
      mutations.add(StringMutation.noop(lastPosition));
    }

    return mutations;
  }

}
