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

import org.jboss.errai.cdi.event.client.EventProducerTestModule;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.ioc.client.container.IOC;

import java.util.List;
import java.util.Map;

/**
 * Tests CDI event producers.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class EventProducerIntegrationTest extends AbstractEventIntegrationTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.event.EventProducerTestModule";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
  }

  public void testInjectedEvents() {
    delayTestFinish(60000);

      CDI.addPostInitTask(new Runnable() {
        @Override
        public void run() {
          EventProducerTestModule module = IOC.getBeanManager().lookupBean(EventProducerTestModule.class).getInstance();

          assertNotNull(module.getEvent());
          assertNotNull(module.getEventA());
          assertNotNull(module.getEventB());
          assertNotNull(module.getEventC());
          assertNotNull(module.getEventAB());
          assertNotNull(module.getEventAC());
          assertNotNull(module.getEventBC());
          assertNotNull(module.getEventABC());

          finishTest();
      }
    });
  }

  public void testEventProducers() {
    final EventProducerTestModule module = IOC.getBeanManager().lookupBean(EventProducerTestModule.class).getInstance();

    final Runnable verifier = new Runnable() {
      @Override
      public void run() {

        Map<String, List<String>> actualEvents = module.getReceivedEventsOnServer();

        // assert that the server received all events
        EventProducerIntegrationTest.this.verifyQualifiedEvents(actualEvents, false);
        finishTest();
      }
    };

    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        if (module.getBusReadyEventsReceived()) {
          module.setResultVerifier(verifier);
          module.fireAll();
        }
        else {
          fail("Did not receive a BusReadyEvent!");
        }
      }
    });

    // only used for the case the {@see FinishEvent} was not received.
    verifyInBackupTimer(verifier, 120000);
    delayTestFinish(240000);
  }
}
