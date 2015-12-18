/*
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

package org.jboss.errai.cdi.event.client.test;

import java.util.List;

import org.jboss.errai.cdi.client.event.DataBoundEvent;
import org.jboss.errai.cdi.client.event.LocalEventA;
import org.jboss.errai.cdi.event.client.DisconnectedEventTestModule;
import org.jboss.errai.common.client.api.extension.InitVotes;
import org.jboss.errai.databinding.client.BindableProxy;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;

/**
 * @author Mike Brock
 */
public class DisconnectedEventIntegrationTest extends AbstractErraiCDITest {
  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.event.LocalEventTestModule";
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

  public void testLocalEvent() {
    delayTestFinish(60000);

    InitVotes.registerOneTimeInitCallback(new Runnable() {
      @Override
      public void run() {
        final DisconnectedEventTestModule testModule
            = IOC.getBeanManager().lookupBean(DisconnectedEventTestModule.class).getInstance();

        final String testText = "NOQUAL";
        final String qualText = "QUAL";
        final String extraQualText = "EXTRAQUAL";

        testModule.fireEvent(testText);
        testModule.fireQualified(qualText);
        testModule.fireQualifiedWithExtraQualifiers(extraQualText);

        final List<LocalEventA> capturedEvents = testModule.getCapturedEvents();

        assertEquals("wrong number of events", 9, capturedEvents.size());

        assertEquals(testText + ":None", capturedEvents.get(0).getMessage());
        assertEquals(testText + ":Any", capturedEvents.get(1).getMessage());

        assertEquals(qualText + ":None", capturedEvents.get(2).getMessage());
        assertEquals(qualText + ":Any", capturedEvents.get(3).getMessage());
        assertEquals(qualText + ":A", capturedEvents.get(4).getMessage());

        assertEquals(extraQualText + ":None", capturedEvents.get(5).getMessage());
        assertEquals(extraQualText + ":Any", capturedEvents.get(6).getMessage());
        assertEquals(extraQualText + ":A", capturedEvents.get(7).getMessage());
        assertEquals(extraQualText + ":AB", capturedEvents.get(8).getMessage());

        finishTest();
      }
    });
  }
  
  public void testLocalDataBoundEvent() {
    delayTestFinish(60000);

    InitVotes.registerOneTimeInitCallback(new Runnable() {
      @Override
      public void run() {
        final DisconnectedEventTestModule module
            = IOC.getBeanManager().lookupBean(DisconnectedEventTestModule.class).getInstance();

        DataBoundEvent dbe = DataBinder.forModel(new DataBoundEvent()).getModel();
        dbe.setValue("testValue");
        module.fireDataBoundEvent(dbe);

        DataBoundEvent capturedEvent = module.getCapturedDataBoundEvent();
        assertNotNull("databound event was not observed", capturedEvent);
        assertFalse("databound event was not unwrapped", capturedEvent instanceof BindableProxy);
        assertEquals("databound event was not marshalled correctly", "testValue", capturedEvent.getValue());
        finishTest();
      }
    });
  }
}

