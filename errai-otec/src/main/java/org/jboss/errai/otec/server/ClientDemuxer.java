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

package org.jboss.errai.otec.server;

import org.jboss.errai.otec.client.OpDto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Mike Brock
 */
public class ClientDemuxer {
  private final AtomicInteger lastSequence = new AtomicInteger(-1);
  private final SortedSet<OpDto> outOfOrders
      = new TreeSet<OpDto>();

  private boolean updateRemoteSequence(final int lastTx, final int newTx) {
    if (lastSequence.get() == -1) {
      lastSequence.set(newTx);
      return true;
    }
    else {
      return lastSequence.compareAndSet(lastTx, newTx);
    }
  }

  public synchronized Collection<OpDto> getEnginePlanFor(final OpDto dto) {
    final boolean updated = updateRemoteSequence(dto.getLastRevisionTx(), dto.getRevision());
    if (!outOfOrders.isEmpty()) {
      if (updated) {
        final List<OpDto> ops = new ArrayList<OpDto>();
        ops.add(dto);

        final Iterator<OpDto> dtosHeld = outOfOrders.iterator();
        OpDto d = dto;
        while (dtosHeld.hasNext()) {
          final OpDto heldDto = dtosHeld.next();
          if (heldDto.getLastRevisionTx() == d.getRevision()) {
            ops.add(heldDto);
            dtosHeld.remove();
            lastSequence.set(heldDto.getRevision());
            d = heldDto;
          }
        }

        return ops;
      }
      else {
        outOfOrders.add(dto);
        System.out.println("out of orders: " + outOfOrders);

        return Collections.emptyList();
      }
    }
    else if (!updated) {
      System.out.println("*** WARNING! OUT OF ORDER OPS! ***" +
          "\nOUT OF ORDER REVISION: " + dto.getRevision() +
          "\n   LAST GOOD REVISION: " + lastSequence.get() +
          "\n          OP TO BLAME: " + dto + "\n\n");

      outOfOrders.add(dto);
      return Collections.emptyList();
    }
    else {
      return Collections.singletonList(dto);
    }
  }
}
