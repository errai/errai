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

import org.jboss.errai.otec.mutation.Mutation;
import org.jboss.errai.otec.operation.OTOperation;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public abstract class AbstractOtecTest {
  private static final String PLAYBACK_FORMAT = "%-30s %-40s\n";
  
  protected abstract OTPeer createPeerFor(OTEngine local, OTEngine remote);
  
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
}
