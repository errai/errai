/**
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.otec;

import static org.jboss.errai.otec.client.mutation.MutationType.Delete;
import static org.jboss.errai.otec.client.mutation.MutationType.Insert;

import junit.framework.Assert;
import org.jboss.errai.otec.client.LogQuery;
import org.jboss.errai.otec.client.OTClientEngine;
import org.jboss.errai.otec.client.OTEntity;
import org.jboss.errai.otec.client.StringState;
import org.jboss.errai.otec.client.TransactionLog;
import org.jboss.errai.otec.client.operation.OTOperationsFactory;
import org.junit.Test;

/**
 * @author Mike Brock
 */
public class LogConsistencyTests {
  @Test
  public void testLogConsistentAfterPruning() {
    final OTClientEngine engine = (OTClientEngine) OTClientEngine.createEngineWithSinglePeer("ClientA");
    final OTEntity entity = engine.getEntityStateSpace().addEntity(StringState.of("Hello!"));
    final TransactionLog log = entity.getTransactionLog();

    final OTOperationsFactory opFact = engine.getOperationsFactory();

    opFact.createOperation(entity).add(Insert, 0, "A").submit();
    opFact.createOperation(entity).add(Insert, 1, "B").submit();

    log.purgeTo(2);

    opFact.createOperation(entity).add(Insert, 2, "C").submit();
    opFact.createOperation(entity).add(Insert, 3, "D").submit();

    final LogQuery stateForRevision4 = log.getEffectiveStateForRevision(4);

    Assert.assertEquals("ABCDHello!", stateForRevision4.getEffectiveState().get());

    opFact.createOperation(entity).add(Insert, 4,"E").submit();
    log.purgeTo(entity.getRevision());

    opFact.createOperation(entity).add(Delete, 4, "E").submit();
    opFact.createOperation(entity).add(Insert, 4,"FG").submit();
    opFact.createOperation(entity).add(Insert, 6,"HIJK").submit();


    Assert.assertEquals("ABCDEHello!", log.getEffectiveStateForRevision(5).getEffectiveState().get());
    Assert.assertEquals("ABCDFGHello!", log.getEffectiveStateForRevision(7).getEffectiveState().get());
    Assert.assertEquals("ABCDFGHIJKHello!", log.getEffectiveStateForRevision(8).getEffectiveState().get());
  }
}
