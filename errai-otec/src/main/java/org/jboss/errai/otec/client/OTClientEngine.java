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

package org.jboss.errai.otec.client;

import org.jboss.errai.otec.client.operation.OTOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Mike Brock
 */
public class OTClientEngine extends AbstractOTEngine {
  
  private static final Logger logger = LoggerFactory.getLogger(OTClientEngine.class);

  protected OTClientEngine(final PeerState peerState, final String name) {
    super(name, peerState);
  }

  @SuppressWarnings("UnusedDeclaration")
  public static OTEngine createEngineWithSinglePeer() {
    return createEngineWithSinglePeer(null);
  }

  public static OTEngine createEngineWithSinglePeer(final String name) {
    final AbstractOTEngine otClientEngine = new OTClientEngine(new SinglePeerState(), name);
    otClientEngine.start();
    return otClientEngine;
  }

  @Override
  public boolean receive(final String peerId, final OTOperation remoteOp) {
    try {
      logger.debug("RECEIVE fromPeer=" + peerId + "; op=" + remoteOp  + "; rev=" + remoteOp.getRevision()
          + "; state=\"" + getEntityStateSpace().getEntity(remoteOp.getEntityId()).getState().get() + "\"");
      return applyFromRemote(remoteOp) != null;
    }
    catch (OTException e) {
      logger.warn("warning. unrecoverable path divergence. will resync.");
      return false;
    }
    catch (BadSync badSync) {
      return false;
    }
  }

  @Override
  public void notifyOperation(final OTOperation operation) {
    super.notifyOperation(operation);
  }

  @Override
  public void start() {
    setMode(OTEngineMode.Online);
  }

  @Override
  public void stop(boolean wait) {
    setMode(OTEngineMode.Offline);
  }

}
