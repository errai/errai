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

import org.jboss.errai.otec.client.OTEngine;
import org.jboss.errai.otec.client.operation.OTOperation;
import org.jboss.errai.otec.client.operation.OTOperationImpl;
import org.jboss.errai.otec.client.util.OTLogUtil;

/**
 * @author Mike Brock
 */
public class AsynchronousMockPeerlImpl extends AbstractMockPeer {

  final Thread thread;

  private final ArrayBlockingQueue<OTOperation> outboundQueue = new ArrayBlockingQueue<OTOperation>(100);

  public AsynchronousMockPeerlImpl(final OTEngine localEngine, final OTEngine engine) {
    this.localEngine = localEngine;
    this.remoteEngine = engine;

    this.thread = new Thread() {
      @Override
      public void run() {
        while (!outboundQueue.isEmpty()) {
          try {
            final long millis = (long) (Math.random() * 12);
            Thread.sleep(millis);
            transmit();
          }
          catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    };
  }

  private void transmit() {
    final OTOperation operation = outboundQueue.poll();

    if (operation == null) {
      return;
    }

    OTLogUtil.log("TRANSMIT",
        operation.toString(),
        localEngine.toString(),
        remoteEngine.toString(),
        operation.getRevision(),
        "\"" + localEngine.getEntityStateSpace().getEntity(operation.getEntityId()).getState().get() + "\"");

    remoteEngine.receive(localEngine.getId(), OTOperationImpl.createLocalOnlyOperation(remoteEngine, operation, getLastKnownRemoteSequence(operation.getEntityId())));

    getPeerData(operation.getEntityId()).setLastKnownTransmittedSequence(operation.getRevision());
  }

  @Override
  public void sendPurgeHint(Integer entityId, int revision) {
  }

  @Override
  public void forceResync(Integer entityId, int revision, String state) {
  }

  @Override
  public void send(final OTOperation operation) {
    outboundQueue.offer(operation);
  }

  public void start() {
    thread.start();
  }

  public Thread getThread() {
    return thread;
  }

}
