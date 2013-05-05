package org.jboss.errai.otec;


import junit.framework.Assert;
import org.jboss.errai.otec.client.OTClientEngine;
import org.jboss.errai.otec.client.OTEngine;
import org.jboss.errai.otec.client.mutation.Mutation;
import org.jboss.errai.otec.client.operation.OTOperation;
import org.jboss.errai.otec.client.operation.OTOperationImpl;
import org.jboss.errai.otec.server.ClientDemuxer;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Mike Brock
 */
public class OrderFixerTests {
  @Test
  public void testOutOfOrderFix() {
    final ClientDemuxer clientStats = new ClientDemuxer();

    final OTEngine engine = OTClientEngine.createEngineWithSinglePeer();

    final OTOperation op0 = OTOperationImpl.createOperation(engine, engine.getId(), Collections.<Mutation>emptyList(), 0, 0, "");
    final OTOperation op1 = OTOperationImpl.createOperation(engine, engine.getId(), Collections.<Mutation>emptyList(), 0, 1, "");
    final OTOperation op2 = OTOperationImpl.createOperation(engine, engine.getId(), Collections.<Mutation>emptyList(), 0, 2, "");
    final OTOperation op3 = OTOperationImpl.createOperation(engine, engine.getId(), Collections.<Mutation>emptyList(), 0, 3, "");
    final OTOperation op4 = OTOperationImpl.createOperation(engine, engine.getId(), Collections.<Mutation>emptyList(), 0, 4, "");

    final Collection<OTOperation> engineSubmissionPlan = clientStats.getEnginePlanFor(op0);
    final Collection<OTOperation> engineSubmissionPlan1 = clientStats.getEnginePlanFor(op2);
    final Collection<OTOperation> engineSubmissionPlan2 = clientStats.getEnginePlanFor(op3);
    final Collection<OTOperation> engineSubmissionPlan3 = clientStats.getEnginePlanFor(op1);
    final Collection<OTOperation> engineSubmissionPlan4 = clientStats.getEnginePlanFor(op4);

    final Collection<OTOperation> effectiveOpsOrder = new ArrayList<OTOperation>();
    effectiveOpsOrder.addAll(engineSubmissionPlan);
    effectiveOpsOrder.addAll(engineSubmissionPlan1);
    effectiveOpsOrder.addAll(engineSubmissionPlan2);
    effectiveOpsOrder.addAll(engineSubmissionPlan3);
    effectiveOpsOrder.addAll(engineSubmissionPlan4);

    Assert.assertEquals(1, engineSubmissionPlan.size());
    Assert.assertEquals(0, engineSubmissionPlan1.size());
    Assert.assertEquals(0, engineSubmissionPlan2.size());
    Assert.assertEquals(3, engineSubmissionPlan3.size());
    Assert.assertEquals(1, engineSubmissionPlan4.size());

    int ver = 0;
    for (final OTOperation otOperation : effectiveOpsOrder) {
      Assert.assertEquals(ver++, otOperation.getRevision());
    }
  }
}
