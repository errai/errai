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

import junit.framework.Assert;

import org.jboss.errai.otec.client.OTEngine;
import org.jboss.errai.otec.client.OTPeer;
import org.jboss.errai.otec.client.State;
import org.jboss.errai.otec.client.TransactionLog;
import org.jboss.errai.otec.client.mutation.Mutation;
import org.jboss.errai.otec.client.operation.OTOperation;
import org.jboss.errai.otec.client.util.OTLogUtil;
import org.jboss.errai.otec.harness.OTTestingLogger;
import org.junit.Before;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public abstract class AbstractOtecTest {
  private static final String PLAYBACK_FORMAT = "%-30s %-40s\n";
  
  protected abstract OTPeer createPeerFor(OTEngine local, OTEngine remote);

  protected void assertSystemAssertionsEnabled() {
    // test that JVM assertions are enabled.
    boolean assertionsEnabled = false;
    assert assertionsEnabled = true;

    /**
     * The OT logging framework is now guarded behind JVM assertion calls to allow it to be efficiently disabled.
     * This will allow us to bake in the logging system without worrying about impacting production performance
     * of constructing complex logging calls.
     *
     * If JVM are not enabled (-ea) then logging calls will not be executed, and these tests may not succeed if
     * they're testing logging behaviour.
     */
    Assert.assertTrue("System assertions MUST be enabled (-ea) to run these tests", assertionsEnabled);
  }
  
  @SuppressWarnings("unchecked")
  protected String replayLogAndReturnResult(final String name,
                                          final State state,
                                          final int revision,
                                          final TransactionLog log) {

    renderPlaybackHeader(name, revision);
    renderInitialStatePlayback(state);

    for (final OTOperation operation : log.getCanonLog()) {
      for (final Mutation mutation : operation.getMutations()) {
        mutation.apply(state);
        renderMutationPlayback(mutation, state);
      }
    }

    System.out.println("RESULTING HASH: " + state.getHash());
    System.out.println("\n");

    return (String) state.get();
  }
  

  private static void renderPlaybackHeader(final String stateName, int currentRevision) {
    System.out.println("===================================================");
    System.out.println("NODE: " + stateName + "; CURRENT REVISION: " + currentRevision);
    System.out.println();
    System.out.printf(PLAYBACK_FORMAT, "MUTATION", "STATE");
    System.out.println("---------------------------------------------------");
  }

  private static void renderInitialStatePlayback(final State state) {
    System.out.printf(PLAYBACK_FORMAT, "SYNC", "\"" + String.valueOf(state.get()) + "\"");
  }

  private static void renderMutationPlayback(final Mutation mutation, final State state) {
    System.out.printf(PLAYBACK_FORMAT, mutation, "\"" + String.valueOf(state.get()) + "\"");
  }

  public void peer(final OTEngine engine1, final OTEngine engine2) {
    engine1.registerPeer(createPeerFor(engine1, engine2));
    engine2.registerPeer(createPeerFor(engine2, engine1));
  }

  @Before
  public void setUp() throws Exception {
    OTLogUtil.setLogAdapter(new OTTestingLogger());
    assertSystemAssertionsEnabled();
  }
}
