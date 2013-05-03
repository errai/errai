package org.jboss.errai.otec.server;

import org.jboss.errai.otec.client.operation.OTOperation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Mike Brock
 */
public class ClientDemuxer {
  private final AtomicInteger lastSequence = new AtomicInteger(-1);
  private final SortedMap<Integer, OTOperation> outOfOrders = new ConcurrentSkipListMap<Integer, OTOperation>();

  private boolean updateRemoteSequence(final int remoteSequence) {
    final int i = lastSequence.get();
    if (remoteSequence - 1 == i || i == -1) {
      lastSequence.set(remoteSequence);
      return true;
    }
    else {
      return false;
    }
  }

  public Collection<OTOperation> getEnginePlanFor(final OTOperation operation) {
    final boolean updated = updateRemoteSequence(operation.getRevision());
    if (!outOfOrders.isEmpty()) {
      if (updated) {
        final List<OTOperation> ops = new ArrayList<OTOperation>();
        ops.add(operation);

        for (int i = operation.getRevision() + 1; outOfOrders.containsKey(i); i++) {
          ops.add(outOfOrders.remove(i));
          lastSequence.set(i);
        }

        return ops;
      }
      else {
        outOfOrders.put(operation.getRevision(), operation);
        return Collections.emptyList();
      }
    }
    else if (!updated) {
      System.out.println("*** WARNING! OUT OF ORDER OPS! ***" +
          "\nOUT OF ORDER REVISION: " + operation.getRevision() +
          "\n   LAST GOOD REVISION: " + lastSequence.get() +
          "\n          OP TO BLAME: " + operation + "\n\n");

      outOfOrders.put(operation.getRevision(), operation);
      return Collections.emptyList();
    }
    else {
      return Collections.singletonList(operation);
    }
  }
}
