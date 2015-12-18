/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.client.tests;

import java.util.Set;

import org.jboss.errai.bus.client.framework.ClientMessageBusImpl;

/**
 * Tests for the correct behaviour in case remote communication was disabled in the client bus.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class DisabledRemoteCommunicationTest extends AbstractErraiTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.bus.ErraiBusTests";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    // Disable remote communication
    setRemoteCommunicationEnabled(false);
    super.gwtSetUp();
  }

  @Override
  protected void gwtTearDown() throws Exception {
    // renable when test is done so this doesn't interfere with other tests
    setRemoteCommunicationEnabled(true);
    super.gwtTearDown();
  }

  public void testDisableRemoteCommunication() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        Set<String> remoteSubscriptions = ((ClientMessageBusImpl) bus).getRemoteServices();

        assertNotNull(remoteSubscriptions);
        assertTrue("Expected to find no remote subscriptions", remoteSubscriptions.isEmpty());

        // re-enable
        finishTest();
      }
    });
  }

  public native void setRemoteCommunicationEnabled(boolean enabled) /*-{
    $wnd.erraiBusRemoteCommunicationEnabled = enabled;
  }-*/;
}
