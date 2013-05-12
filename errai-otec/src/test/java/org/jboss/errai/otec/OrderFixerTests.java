package org.jboss.errai.otec;


import junit.framework.Assert;
import org.jboss.errai.otec.client.OTClientEngine;
import org.jboss.errai.otec.client.OTEngine;
import org.jboss.errai.otec.client.OpDto;
import org.jboss.errai.otec.client.mutation.Mutation;
import org.jboss.errai.otec.client.operation.OTOperation;
import org.jboss.errai.otec.client.operation.OTOperationImpl;
import org.jboss.errai.otec.server.ClientDemuxer;
import org.junit.Ignore;
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
    final ClientDemuxer demux = new ClientDemuxer();

    final OTEngine engine = OTClientEngine.createEngineWithSinglePeer();

    final OTOperation op0 = OTOperationImpl.createOperation(engine, engine.getId(), Collections.<Mutation>emptyList(), 0, 0, "");
    final OTOperation op1 = OTOperationImpl.createOperation(engine, engine.getId(), Collections.<Mutation>emptyList(), 0, 3, "");
    final OTOperation op2 = OTOperationImpl.createOperation(engine, engine.getId(), Collections.<Mutation>emptyList(), 0, 5, "");
    final OTOperation op3 = OTOperationImpl.createOperation(engine, engine.getId(), Collections.<Mutation>emptyList(), 0, 10, "");
    final OTOperation op4 = OTOperationImpl.createOperation(engine, engine.getId(), Collections.<Mutation>emptyList(), 0, 11, "");

    final OpDto opDto0 = OpDto.fromOperation(op0, -1);
    final OpDto opDto1 = OpDto.fromOperation(op2, 3);
    final OpDto opDto2 = OpDto.fromOperation(op3, 5);
    final OpDto opDto3 = OpDto.fromOperation(op1, 0);
    final OpDto opDto4 = OpDto.fromOperation(op4, 10);


    final Collection<OpDto> engineSubmissionPlan = demux.getEnginePlanFor(opDto0);
    final Collection<OpDto> engineSubmissionPlan1 = demux.getEnginePlanFor(opDto1);
    final Collection<OpDto> engineSubmissionPlan2 = demux.getEnginePlanFor(opDto2);
    final Collection<OpDto> engineSubmissionPlan3 = demux.getEnginePlanFor(opDto3);
    final Collection<OpDto> engineSubmissionPlan4 = demux.getEnginePlanFor(opDto4);

    final Collection<OpDto> effectiveOpsOrder = new ArrayList<OpDto>();
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

    int ver = -1;
    for (final OpDto otOperation : effectiveOpsOrder) {
      Assert.assertTrue(otOperation.getRevision() > ver);
      ver = otOperation.getRevision();
    }
  }

  @Test @Ignore
  public void testOutOfOrderFix2() {
    final ClientDemuxer demux = new ClientDemuxer();

    final OTEngine engine = OTClientEngine.createEngineWithSinglePeer();

    final OTOperation op0 = OTOperationImpl.createOperation(engine, engine.getId(), Collections.<Mutation>emptyList(), 0, 0, "");
    final OTOperation op1 = OTOperationImpl.createOperation(engine, engine.getId(), Collections.<Mutation>emptyList(), 0, 1, "");
    final OTOperation op2 = OTOperationImpl.createOperation(engine, engine.getId(), Collections.<Mutation>emptyList(), 0, 2, "");
    final OTOperation op3 = OTOperationImpl.createOperation(engine, engine.getId(), Collections.<Mutation>emptyList(), 0, 3, "");
    final OTOperation op4 = OTOperationImpl.createOperation(engine, engine.getId(), Collections.<Mutation>emptyList(), 0, 4, "");
    final OTOperation op5 = OTOperationImpl.createOperation(engine, engine.getId(), Collections.<Mutation>emptyList(), 0, 5, "");
    final OTOperation op6 = OTOperationImpl.createOperation(engine, engine.getId(), Collections.<Mutation>emptyList(), 0, 6, "");
    final OTOperation op7 = OTOperationImpl.createOperation(engine, engine.getId(), Collections.<Mutation>emptyList(), 0, 7, "");

    final OpDto opDto0 = OpDto.fromOperation(op0, -1);
    final OpDto opDto1 = OpDto.fromOperation(op1, 0);
    final OpDto opDto2 = OpDto.fromOperation(op2, 1);
    final OpDto opDto3 = OpDto.fromOperation(op3, 2);
    final OpDto opDto4 = OpDto.fromOperation(op4, 3);
    final OpDto opDto5 = OpDto.fromOperation(op5, 4);
    final OpDto opDto6 = OpDto.fromOperation(op6, 5);
    final OpDto opDto7 = OpDto.fromOperation(op7, 6);

    final Collection<OpDto> engineSubmissionPlan = demux.getEnginePlanFor(opDto0);
    final Collection<OpDto> engineSubmissionPlan1 = demux.getEnginePlanFor(opDto1);
    final Collection<OpDto> engineSubmissionPlan2 = demux.getEnginePlanFor(opDto2);
    final Collection<OpDto> engineSubmissionPlan3 = demux.getEnginePlanFor(opDto3);
    final Collection<OpDto> engineSubmissionPlan4 = demux.getEnginePlanFor(opDto4);
    final Collection<OpDto> engineSubmissionPlan5 = demux.getEnginePlanFor(opDto5);
    final Collection<OpDto> engineSubmissionPlan6 = demux.getEnginePlanFor(opDto6);
    final Collection<OpDto> engineSubmissionPlan7 = demux.getEnginePlanFor(opDto7);

    final Collection<OpDto> effectiveOpsOrder = new ArrayList<OpDto>();
    effectiveOpsOrder.addAll(engineSubmissionPlan);
    effectiveOpsOrder.addAll(engineSubmissionPlan1);
    effectiveOpsOrder.addAll(engineSubmissionPlan2);
    effectiveOpsOrder.addAll(engineSubmissionPlan3);
    effectiveOpsOrder.addAll(engineSubmissionPlan4);
    effectiveOpsOrder.addAll(engineSubmissionPlan5);
    effectiveOpsOrder.addAll(engineSubmissionPlan6);
    effectiveOpsOrder.addAll(engineSubmissionPlan7);

    Assert.assertEquals(1, engineSubmissionPlan.size());
    Assert.assertEquals(0, engineSubmissionPlan1.size());
    Assert.assertEquals(0, engineSubmissionPlan2.size());
    Assert.assertEquals(3, engineSubmissionPlan3.size());
    Assert.assertEquals(1, engineSubmissionPlan4.size());

    int ver = 0;
    for (final OpDto otOperation : effectiveOpsOrder) {
      Assert.assertEquals(ver++, otOperation.getRevision());
    }
  }
}
