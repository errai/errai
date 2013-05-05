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

import org.jboss.errai.otec.client.OTEngine;
import org.jboss.errai.otec.client.OTQueuedOperation;
import org.jboss.errai.otec.client.PeerState;
import org.jboss.errai.otec.client.operation.OTOperation;
import org.jboss.errai.otec.server.MultiplePeerState;
import org.jboss.errai.otec.server.OTServerEngine;

/**
 * @author Mike Brock
 */
public class SynchronousServerEngine extends OTServerEngine {
  protected SynchronousServerEngine(final PeerState peerState, final String name) {
    super(name, peerState);
  }

  public static OTEngine createEngineWithMultiplePeers(final String name) {
    final SynchronousServerEngine otServerEngine = new SynchronousServerEngine(new MultiplePeerState(), name);
    return otServerEngine;
  }

  @Override
  public boolean receive(String peerId, OTOperation remoteOp) {
    handleOperation(new OTQueuedOperation(remoteOp.getRevision(), remoteOp, peerId, remoteOp.getEntityId()));
    return true;
  }

  @Override
  public void start() {
  }

  @Override
  public void stop(boolean wait) {
  }
}
