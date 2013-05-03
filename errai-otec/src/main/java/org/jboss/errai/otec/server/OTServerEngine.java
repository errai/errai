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

package org.jboss.errai.otec.server;

import org.jboss.errai.otec.client.AbstractOTEngine;
import org.jboss.errai.otec.client.OTEngine;
import org.jboss.errai.otec.client.OTEngineMode;
import org.jboss.errai.otec.client.OTEntity;
import org.jboss.errai.otec.client.OTPeer;
import org.jboss.errai.otec.client.OTQueuedOperation;
import org.jboss.errai.otec.client.PeerState;
import org.jboss.errai.otec.client.operation.OTOperation;

import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author Mike Brock
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class OTServerEngine extends AbstractOTEngine {
  private PollingThread pollingThread;
  private HousekeeperThread housekeeperThread;

  private final ArrayBlockingQueue<OTQueuedOperation> incomingQueue
      = new ArrayBlockingQueue<OTQueuedOperation>(100, true);

  protected OTServerEngine(final String name, final PeerState peerState) {
    super(name, peerState);
  }

  public static OTEngine createEngineWithMultiplePeers(final String name) {
    final OTServerEngine otServerEngine = new OTServerEngine(name, new MultiplePeerState());
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
    setMode(OTEngineMode.Online);
    this.incomingQueue.clear();
    this.pollingThread = new PollingThread(this);
    this.pollingThread.start();
    this.housekeeperThread = new HousekeeperThread(this);
    this.housekeeperThread.setPriority(Thread.MIN_PRIORITY);
    this.housekeeperThread.start();

  }

  @Override
  public void stop(final boolean wait) {
    if (!wait) {
      setMode(OTEngineMode.Offline);
    }
    incomingQueue.offer(new OTQueuedOperation(-1, null, null, -1));

    try {
      pollingThread.join();
      if (wait) {
        setMode(OTEngineMode.Offline);
      }
    }
    catch (InterruptedException e) {
      // ignore
    }
  }

  private static class HousekeeperThread extends Thread {
    private final OTServerEngine serverEngine;

    private HousekeeperThread(final OTServerEngine serverEngine) {
      this.serverEngine = serverEngine;
    }

    @Override
    public void run() {
      while (serverEngine.mode == OTEngineMode.Online) {
        try {
          Thread.sleep(10000);

          for (final OTEntity otEntity : serverEngine.getEntityStateSpace().getEntities()) {
            int lastKnown = -1;
            final Set<OTPeer> peersFor = serverEngine.getPeerState().getPeersFor(otEntity.getId());
            for (final OTPeer otPeer : peersFor) {
              final int sequence = otPeer.getLastKnownRemoteSequence(otEntity.getId());
              if (lastKnown == -1 || sequence < lastKnown) {
                lastKnown = sequence;
              }
            }

            if (lastKnown != -1) {
              final int i = otEntity.getTransactionLog().purgeTo(lastKnown);
              if (i > 0) {
                System.out.println("purged " + i + " old entries from transaction log.");
                for (final OTPeer otPeer : peersFor) {
                  otPeer.sendPurgeHint(otEntity.getId(), lastKnown);
                }
              }
            }
          }
        }
        catch (InterruptedException e) {
          // fall though.
        }
      }
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
//
//      System.out.println("POLL_FROM_QUEUE:" + queuedOp.getOperation()
//          + ";rev" + queuedOp.getOperation().getRevision() + ";entity:" + queuedOp.getEntityId());

      handleOperation(queuedOp);
    }
    catch (Throwable t) {
      t.printStackTrace();
    }
    return true;
  }

  protected void handleOperation(final OTQueuedOperation queuedOp) {
    final OTOperation transformedOp = applyFromRemote(queuedOp.getOperation());
    final OTPeer peer = getPeerState().getPeer(queuedOp.getPeerId());

    // broadcast to all other peers subscribed to this entity
    final Set<OTPeer> peers = getPeerState().getPeersFor(queuedOp.getEntityId());
    for (final OTPeer otPeer : peers) {
      if (otPeer != peer && !transformedOp.isNoop()) {
        otPeer.send(transformedOp);
      }
    }
  }

  @Override
  public void receive(final String peerId, final OTOperation remoteOp) {
    if (remoteOp.getEntityId() == -1) {
      return;
    }


    System.out.println("RECV:" + remoteOp + ";rev:" + remoteOp.getRevision() + ";peerId=" + peerId);

    // System.out.println("ADD_TO_QUEUE:" + remoteOp + ":rev:" + remoteOp.getRevision());

    incomingQueue.offer(new OTQueuedOperation(remoteOp.getRevision(), remoteOp, peerId, remoteOp.getEntityId()));
    getPeerState().getPeer(peerId).setLastKnownRemoteSequence(remoteOp.getEntityId(), remoteOp.getRevision());
  }
}
