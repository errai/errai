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
import org.jboss.errai.otec.operation.OpPair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author Mike Brock
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class Transformer {
  private final OTEntity entity;
  private final OTPeer peer;
  private final OTOperation remoteOp;

  private Transformer(final OTEntity entity, final OTPeer peer, final OTOperation remoteOp) {
    this.entity = entity;
    this.peer = peer;
    this.remoteOp = remoteOp;
  }

  public static Transformer createTransformer(final OTEntity entity, final OTPeer peer, final OTOperation operation) {
    return new Transformer(entity, peer, operation);
  }

  public List<OTOperation> transform() {
    final List<OTOperation> remoteOps = new ArrayList<OTOperation>();
    final TransactionLog transactionLog = entity.getTransactionLog();
    final Collection<OTOperation> localOps = transactionLog.getLogFromId(remoteOp.getRevision());

    // if no operation was carried out we can just apply the new operations
    if (localOps.isEmpty()) {
      remoteOp.apply(entity);
      transactionLog.appendLog(remoteOp);
    }
    else {
      for (final OTOperation localOp : localOps) {
        final OpPair opPair = transform(remoteOp, localOp);

        final OTOperation localOpPrime = opPair.getLocalOp();
        localOpPrime.apply(entity);
        transactionLog.appendLog(localOpPrime);

        remoteOps.add(opPair.getRemoteOp());
      }
    }

    return remoteOps;
  }

  private OpPair transform(final OTOperation remoteOp, final OTOperation localOp) {
    OTOperation remoteOpPrime;
    OTOperation localOpPrime = null;

    final List<Mutation> localMutations = new ArrayList<Mutation>();
    final List<Mutation> remoteMutations = new ArrayList<Mutation>();

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
        localMutations.add(rm);
      }
      else if (diff == 0) {
        System.out.println();
      }
      else {
        switch (rm.getType()) {
          case Insert:
            offset--;
            break;
          case Delete:
            offset++;
            break;
        }
        localMutations.add(new StringMutation(rm.getType(), IndexPosition.of(rmIdx.getPosition() + offset), (CharacterData) rm.getData()));
      }

      localOpPrime = OTOperationImpl.createLocalOnlyOperation(localMutations, entity.getId(), entity.getRevision());
    }


    return OpPair.of(remoteOp, localOpPrime);
  }

}
