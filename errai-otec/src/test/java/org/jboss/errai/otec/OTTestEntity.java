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

import org.jboss.errai.otec.client.OTEntity;
import org.jboss.errai.otec.client.OTEntityImpl;
import org.jboss.errai.otec.client.StringState;
import org.jboss.errai.otec.client.TransactionLog;
import org.jboss.errai.otec.client.operation.OTOperation;
import org.jboss.errai.otec.client.operation.OTOperationImpl;

/**
 * @author Christian Sadilek
 * @author Mike Brock
 */
@SuppressWarnings("unchecked")
public class OTTestEntity extends OTEntityImpl {
  public OTTestEntity(final OTEntity entity) {
    super(entity.getId(), StringState.of(((String) entity.getState().get())));

    final TransactionLog transactionLog = entity.getTransactionLog();
    for (final OTOperation operation : transactionLog.getLog()) {
      getTransactionLog().appendLog(
          OTOperationImpl.createOperation(operation.getEngine(), operation.getAgentId(), operation.getMutations(), operation.getEntityId(),
              operation.getRevision(), operation.getRevisionHash(), operation.getTransformedFrom(), operation.getLastRevisionTx()));
    }

    setRevision(entity.getRevision());
  }
}
