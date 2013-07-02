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

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.otec.client.OTEngine;
import org.jboss.errai.otec.client.OTPeer;
import org.jboss.errai.otec.harness.ManyTimesTestRunner;
import org.junit.runner.RunWith;

/**
 * @author Mike Brock
 */
@RunWith(ManyTimesTestRunner.class)
public class ThreeEngineAsyncFuzzTest extends ThreeEngineOtecTest {
  protected List<AsynchronousMockPeerlImpl> peersStarted = new ArrayList<AsynchronousMockPeerlImpl>();

  @Override
  protected void suspendEngines() {
  }

  @Override
  protected OTPeer createPeerFor(OTEngine local, OTEngine remote) {
    if (local.getName().equals("Server")) {
      return new SynchronousMockPeerlImpl(local, remote);
    }
    final AsynchronousMockPeerlImpl asynchronousMockPeerl = new AsynchronousMockPeerlImpl(local, remote);
    peersStarted.add(asynchronousMockPeerl);
    return asynchronousMockPeerl;
  }

  @Override
  protected void startEnginesAndWait() {
    for (AsynchronousMockPeerlImpl peer : peersStarted) {
      peer.start();
    }

    try {
      for (AsynchronousMockPeerlImpl peer : peersStarted)
        peer.getThread().join();
    }
    catch (Throwable t) {
      t.printStackTrace();
    }

    stopServerEngineAndWait();
  }
}
