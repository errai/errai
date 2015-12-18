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

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.client.api.Subscription;
import org.jboss.errai.cdi.client.event.FunEvent;
import org.jboss.errai.cdi.client.qualifier.A;
import org.jboss.errai.cdi.client.qualifier.B;
import org.jboss.errai.cdi.event.client.EventRoutingTestModule;
import org.jboss.errai.enterprise.client.cdi.AbstractCDIEventCallback;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.enterprise.client.cdi.CDIProtocol;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.ioc.client.container.IOC;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Brock
 */
public class EventRoutingIntegrationTest extends AbstractErraiCDITest {
  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.event.EventRoutingTestModule";
  }

  public void testEventRouting() {
    delayTestFinish(60000);

    final List<FunEvent> wireEvents = new ArrayList<FunEvent>();
    final List<FunEvent> actualEvents = new ArrayList<FunEvent>();
    final EventRoutingTestModule module = IOC.getBeanManager().lookupBean(EventRoutingTestModule.class).getInstance();

    final Runnable verifier = new Runnable() {
      @Override
      public void run() {
        for (final FunEvent funEvent : wireEvents) {

          // none of the events should have text that contain the character 'A'.
          if (funEvent.getText().contains("A")) {
            fail("should not have received qualifier A on the wire");
          }

          assertEquals("same number of events should be received on wire and actual",
              wireEvents.size(), actualEvents.size());
        }

        finishTest();
      }
    };

    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        module.setResultVerifier(verifier);

        final Subscription subscribeA = CDI.subscribe(FunEvent.class.getName(), new AbstractCDIEventCallback<FunEvent>() {
          {
            qualifierSet.add(A.class.getName());
          }

          @Override
          protected void fireEvent(FunEvent event) {
            actualEvents.add(event);
          }
        });

        final Subscription subscribeB = CDI.subscribe(FunEvent.class.getName(), new AbstractCDIEventCallback<FunEvent>() {
          {
            qualifierSet.add(B.class.getName());
          }

          @Override
          protected void fireEvent(FunEvent event) {
            actualEvents.add(event);
          }
        });

        final String eventSubject = CDI.getSubjectNameByType(FunEvent.class.getName());
        ErraiBus.get().subscribe(eventSubject, CDI.ROUTING_CALLBACK);
        ErraiBus.get().subscribe(eventSubject, new MessageCallback() {
          @Override
          public void callback(final Message message) {
            final Object beanRef = message.get(Object.class, CDIProtocol.BeanReference);
            if (beanRef instanceof FunEvent) {
              wireEvents.add((FunEvent) beanRef);
            }
          }
        });

        // intentionally remove the subscription with the "A" qualifier, to ensure the server-side routing
        // logic remove it from the route correctly.
        subscribeA.remove();

        // send the initial event to begin the test
        module.start();
      }
    });


  }
}
