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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.jboss.errai.otec.operation.OTOperation;

/**
 * @author Mike Brock
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class OTServerEngine extends OTClientEngine {
  private PollingThread pollingThread;

  private final ArrayBlockingQueue<OTQueuedOperation> incomingQueue
      = new ArrayBlockingQueue<OTQueuedOperation>(100, true);

  private OTServerEngine(final PeerState peerState, final String name) {
    super(peerState, name);
  }

  public static OTEngine createEngineWithMultiplePeers(final String name) {
    final OTServerEngine otServerEngine = new OTServerEngine(new MultiplePeerState(), name);
    otServerEngine.start();
    return otServerEngine;
  }

  @SuppressWarnings("UnusedDeclaration")
  public static OTEngine createEngineWithMultiplePeers() {
    return createEngineWithMultiplePeers(null);
  }

  @Override
  public void start() {
    if (mode == OTEngineMode.Online) {
      return;
    }
    super.start();
    this.incomingQueue.clear();
    this.pollingThread = new PollingThread(this);
    this.pollingThread.start();
  }

  @Override
  public void stop(final boolean wait) {
    if (!wait) {
      super.stop(wait);
    }
    incomingQueue.offer(new OTQueuedOperation(null, null, -1));

    try {
      pollingThread.join();
      if (wait) {
        super.stop(false);
      }
    }
    catch (InterruptedException e) {
    }
  }

  private static class PollingThread extends Thread {
    private final OTServerEngine serverEngine;

    private PollingThread(final OTServerEngine serverEngine) {
      this.serverEngine = serverEngine;
    }

    @Override
    public void run() {
      for (; serverEngine.mode == OTEngineMode.Online; ) {
        try {
          while (serverEngine.mode == OTEngineMode.Online) {
            if (!serverEngine.pollQueue()) {
              return;
            }
          }
        }
        catch (InterruptedException e) {
          // probably
        }
      }
    }
  }

  private boolean pollQueue() throws InterruptedException {
    try {
      final OTQueuedOperation queuedOp = incomingQueue.poll(10, TimeUnit.MINUTES);

      if (queuedOp == null) {
        return true;
      }
      else if (queuedOp.getEntityId() == -1) {
        return false;
      }

      super.receive(queuedOp.getPeerId(), queuedOp.getEntityId(), queuedOp.getOperation());
    }
    catch (Throwable t) {
      t.printStackTrace();
    }
    return true;
  }

  @Override
  public void receive(final String peerId, final int entityId, final OTOperation remoteOp) {
    if (entityId == -1) {
      return;
    }

    incomingQueue.offer(new OTQueuedOperation(remoteOp, peerId, entityId));
  }
}
