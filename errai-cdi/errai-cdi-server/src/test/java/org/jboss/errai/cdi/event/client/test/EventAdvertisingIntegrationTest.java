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

import java.util.ArrayList;
import java.util.List;

import junit.framework.AssertionFailedError;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.framework.ClientMessageBusImpl;
import org.jboss.errai.cdi.client.event.LocalEventA;
import org.jboss.errai.cdi.client.event.MyEventImpl;
import org.jboss.errai.cdi.event.client.shared.PortableLocalEventA;
import org.jboss.errai.common.client.api.extension.InitVotes;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.enterprise.client.cdi.CDIProtocol;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.junit.Test;

import com.google.gwt.user.client.Timer;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 * @author Max Barkely <mbarkley@redhat.com>
 */
public class EventAdvertisingIntegrationTest extends AbstractErraiCDITest {

  private final List<String> messageBeanTypeLog = new ArrayList<String>();
  private ClientMessageBusImpl backupBus;
  private Timer testTimer;

  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.event.EventObserverTestModule";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    ClientMessageBusImpl fakeBus = new ClientMessageBusImpl() {
      @Override
      public void send(Message message) {
        if (message.hasPart(CDIProtocol.BeanType) && message.getSubject().equals(CDI.SERVER_DISPATCHER_SUBJECT)) {
          messageBeanTypeLog.add(message.get(String.class, CDIProtocol.BeanType));
        }
        super.send(message);
      }
    };
    backupBus = UntestableFrameworkUtil.installAlternativeBusImpl(fakeBus);

    InitVotes.reset();
    super.gwtSetUp();
  }

  @Override
  protected void gwtTearDown() throws Exception {
    messageBeanTypeLog.clear();
    if (testTimer != null) {
      testTimer.cancel();
    }
    super.gwtTearDown();
    
    ClientMessageBusImpl fakeBus = UntestableFrameworkUtil.installAlternativeBusImpl(backupBus);
    fakeBus.stop(true);
  }

  @Test
  public void testLocalEventNotInitiallyAdvertisedToServer() {
    final long start = System.currentTimeMillis();
    testTimer = new Timer() {
      @Override
      public void run() {
        try {
          // this is the actual point of the test
          assertFalse("Local event should not have been advertised to the server",
                  messageBeanTypeLog.contains(LocalEventA.class.getName()));

          // this is an important safety check, because it would be too easy for the test to
          // fake-pass if the implementation details change.
          assertTrue("Portable event should have been advertised to the server",
                  messageBeanTypeLog.contains(MyEventImpl.class.getName()));

          finishTest();
        } catch (AssertionFailedError ex) {
          if (System.currentTimeMillis() - start > 25000) {
            cancel();
            throw ex;
          }
        }
      }
    };
    testTimer.scheduleRepeating(500);
    delayTestFinish(30000);
  }

  @Test
  public void testLocalEventNotReadvertisedToServer() {
    /*
     * Test overview:
     * - Wait for initial CDI Event Advertising to occur
     * - Invalidate the Session Queue
     * - Check for re-advertised CDI Events until timeout
     */
    
    final long start = System.currentTimeMillis();
    new Timer() {
      @Override
      public void run() {
        if (!messageBeanTypeLog.contains(LocalEventA.class.getName())
                && messageBeanTypeLog.contains(MyEventImpl.class.getName())) {
          messageBeanTypeLog.clear();
          cancel();
          MessageBuilder.createMessage("queueSessionInvalidationService").done().sendNowWith(ErraiBus.get());
          delayTestFinish(30000);
          final long secondStart = System.currentTimeMillis();
          testTimer = new Timer() {
            @Override
            public void run() {
              try {
                // this is the actual point of the test
                assertFalse("Local event should not have been advertised to the server",
                        messageBeanTypeLog.contains(LocalEventA.class.getName()));

                // this is an important safety check, because it would be too easy for the test to
                // fake-pass if the implementation details change.
                assertTrue("Portable event should have been advertised to the server",
                        messageBeanTypeLog.contains(MyEventImpl.class.getName()));

                finishTest();
              } catch (AssertionFailedError ex) {
                if (System.currentTimeMillis() - secondStart > 25000) {
                  cancel();
                  throw ex;
                }
              }
            }
          };
          testTimer.scheduleRepeating(500);
        }
        else if (System.currentTimeMillis() - start > 25000) {
          cancel();
          fail("Timed out while waiting for initial advertising of services");
        }
      }
    }.scheduleRepeating(500);
    delayTestFinish(30000);
  }

  @Test
  public void testPortableLocalEventNotInitiallyAdvertisedToServer() {
    final long start = System.currentTimeMillis();
    testTimer = new Timer() {
      @Override
      public void run() {
        try {
          // this is the actual point of the test
          assertFalse("Local event should not have been advertised to the server",
                  messageBeanTypeLog.contains(PortableLocalEventA.class.getName()));
          // this is an important safety check, because it would be too easy for the test to
          // fake-pass if the implementation details change.
          assertTrue("Portable event should have been advertised to the server",
                  messageBeanTypeLog.contains(MyEventImpl.class.getName()));
          finishTest();
        } catch (AssertionFailedError ex) {
          if (System.currentTimeMillis() - start > 55000) {
            cancel();
            throw ex;
          }
        }
      }
    };
    testTimer.scheduleRepeating(500);
    delayTestFinish(60000);
  }

  @Test
  public void testPortableLocalEventNotReadvertisedToServer() {
    final long start = System.currentTimeMillis();
    new Timer() {
      @Override
      public void run() {
        if (!messageBeanTypeLog.contains(PortableLocalEventA.class.getName())
                && messageBeanTypeLog.contains(MyEventImpl.class.getName())) {
          messageBeanTypeLog.clear();
          cancel();
          MessageBuilder.createMessage("queueSessionInvalidationService").done().sendNowWith(ErraiBus.get());
          delayTestFinish(30000);
          final long secondStart = System.currentTimeMillis();
          testTimer = new Timer() {
            @Override
            public void run() {
              try {
                // this is the actual point of the test
                assertFalse("Local event should not have been advertised to the server",
                        messageBeanTypeLog.contains(PortableLocalEventA.class.getName()));

                // this is an important safety check, because it would be too easy for the test to
                // fake-pass if the implementation details change.
                assertTrue("Portable event should have been advertised to the server",
                        messageBeanTypeLog.contains(MyEventImpl.class.getName()));

                finishTest();
              } catch (AssertionFailedError ex) {
                if (System.currentTimeMillis() - secondStart > 25000) {
                  cancel();
                  throw ex;
                }
              }
            }
          };
          testTimer.scheduleRepeating(500);
        }
        else if (System.currentTimeMillis() - start > 25000) {
          cancel();
          fail("Timed out while waiting for initial advertising of services");
        }
      }
    }.scheduleRepeating(500);
    delayTestFinish(30000);
  }

}
