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
import org.jboss.errai.otec.mutation.StringMutation;
import org.jboss.errai.otec.operation.OTOperation;
import org.jboss.errai.otec.operation.OTOperationImpl;

import java.util.ArrayList;
import java.util.Collection;
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

  private Transformer(final OTEngine engine, final boolean remoteWins, final OTEntity entity, final OTPeer peer, final OTOperation remoteOp) {
    this.engine = engine;
    this.remoteWins = remoteWins;
    this.entity = entity;
    this.peer = peer;
    this.remoteOp = remoteOp;
  }

  public static Transformer createTransformerLocalPrecedence(final OTEngine engine, final OTEntity entity, final OTPeer peer, final OTOperation operation) {
    return new Transformer(engine, false, entity, peer, operation);
  }

  public static Transformer createTransformerRemotePrecedence(final OTEngine engine, final OTEntity entity, final OTPeer peer, final OTOperation operation) {
    return new Transformer(engine, true, entity, peer, operation);
  }

  public List<OTOperation> transform() {
    final List<OTOperation> remoteOps = new ArrayList<OTOperation>();
    final TransactionLog transactionLog = entity.getTransactionLog();
    final Collection<OTOperation> localOps = transactionLog.getLogFromId(remoteOp.getRevision());

    // if no operation was carried out we can just apply the new operations
    if (localOps.isEmpty()) {
      remoteOp.apply(entity);
      transactionLog.appendLog(remoteOp);
      remoteOps.add(remoteOp);
    }
    else {
      for (final OTOperation localOp : localOps) {
        final OTOperation localOpPrime = transform(remoteOp, localOp);
        localOpPrime.apply(entity);
        transactionLog.appendLog(localOpPrime);
        remoteOps.add(localOpPrime);
      }
    }

    return remoteOps;
  }

  private OTOperation transform(final OTOperation remoteOp, final OTOperation localOp) {
    OTOperation transformedOp = null;
    final List<Mutation> transformedMutations = new ArrayList<Mutation>();

    final Iterator<Mutation> remoteOpMutations = remoteOp.getMutations().iterator();
    final Iterator<Mutation> localOpMutations = localOp.getMutations().iterator();


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
        switch (rm.getType()) {
          case Insert:
            if (!remoteWins) {
              offset++;
            }
            break;
          case Delete:
            if (!remoteWins) {
              offset--;
            }
            break;
        }
        transformedMutations.add(new StringMutation(rm.getType(), IndexPosition.of(rmIdx.getPosition() + offset), (CharacterData) rm.getData()));
      }
      else if (diff >= 0) {

        switch (rm.getType()) {
          case Insert:
            offset--;
            break;
          case Delete:
            offset++;
            break;
        }
        transformedMutations.add(new StringMutation(rm.getType(), IndexPosition.of(rmIdx.getPosition() + offset), (CharacterData) rm.getData()));
      }

      transformedOp = OTOperationImpl.createLocalOnlyOperation(engine, transformedMutations, entity.getId(), entity.getRevision());
    }

    return transformedOp;
  }

}
