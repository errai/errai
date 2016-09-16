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

import java.lang.annotation.Annotation;
import java.util.Arrays;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.BusLifecycleAdapter;
import org.jboss.errai.bus.client.api.BusLifecycleEvent;
import org.jboss.errai.bus.client.api.ClientMessageBus;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.cdi.event.client.DependentEventObserverTestModule;
import org.jboss.errai.cdi.event.client.EventObserverTestModule;
import org.jboss.errai.cdi.event.client.EventProducerTestModule;
import org.jboss.errai.cdi.event.client.NotifierModule;
import org.jboss.errai.cdi.event.client.OnDemandEventObserver;
import org.jboss.errai.cdi.event.client.PrimitiveEventTestModule;
import org.jboss.errai.cdi.event.client.shared.Create;
import org.jboss.errai.cdi.event.client.shared.Delete;
import org.jboss.errai.cdi.event.client.shared.NotifierStartEvent;
import org.jboss.errai.cdi.event.client.shared.Update;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.ioc.client.IOCUtil;
import org.jboss.errai.ioc.client.container.IOC;

import com.google.gwt.user.client.Timer;

/**
 * Tests CDI event observers.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class EventObserverIntegrationTest extends AbstractEventIntegrationTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.event.EventObserverTestModule";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    EventProducerTestModule.clearReceivedEventsOnServer();
    super.gwtSetUp();
  }

  public void testBusReadyEventObserver() {
    delayTestFinish(60000);
    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        final EventObserverTestModule module = IOC.getBeanManager().lookupBean(EventObserverTestModule.class).getInstance();

        assertEquals("Wrong number of BusReadyEvents received:", 1, module.getBusReadyEventsReceived());
        finishTest();
      }
    });
  }

  public void testEventObservers() {
    final Runnable verifier = new Runnable() {
      @Override
      public void run() {
        final EventObserverTestModule module = IOC.getBeanManager().lookupBean(EventObserverTestModule.class).getInstance();

        // assert that client received all events
        EventObserverIntegrationTest.this.verifyQualifiedEvents(module.getReceivedQualifiedEvents(), true);
        EventObserverIntegrationTest.this.verifySuperTypeEvents(module.getReceivedSuperTypeEvents());

        finishTest();
      }
    };

    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        final EventObserverTestModule module = IOC.getBeanManager().lookupBean(EventObserverTestModule.class).getInstance();

        assertNotNull(module.getStartEvent());
        module.setResultVerifier(verifier);
        module.start();
      }
    });

    // only used for the case the {@see FinishEvent} was not received
    verifyInBackupTimer(verifier, 120000);
    delayTestFinish(240000);
  }

  public void testOnDemandEventObservers() {
    assertEquals("An instance of the observer already exists! This test is now pointless!",
            0, OnDemandEventObserver.instanceCount);

    final Runnable secondVerifier = new Runnable() {
      @Override
      public void run() {
        final OnDemandEventObserver observer = IOC.getBeanManager().lookupBean(OnDemandEventObserver.class).getInstance();

        assertEquals(1, observer.getEventLog().size());

        finishTest();
      }
    };

    final Runnable firstVerifier = new Runnable() {
      @Override
      public void run() {
        // now creating this observer for the first time ever
        final OnDemandEventObserver observer = IOC.getBeanManager().lookupBean(OnDemandEventObserver.class).getInstance();

        assertEquals(0, observer.getEventLog().size());

        final EventObserverTestModule module = IOC.getBeanManager().lookupBean(EventObserverTestModule.class).getInstance();
        module.setResultVerifier(secondVerifier);
        EventProducerTestModule.clearReceivedEventsOnServer();
        module.start();
      }
    };

    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        final EventObserverTestModule module = IOC.getBeanManager().lookupBean(EventObserverTestModule.class).getInstance();

        assertNotNull(module.getStartEvent());
        module.setResultVerifier(firstVerifier);
        module.start();
      }
    });

    verifyInBackupTimer(firstVerifier, 120000);
    delayTestFinish(240000);
  }

  // regression test for ERRAI-592
  public void testObserversStillWorkAfterSessionRenegotiation() {
    /* Unfortunately, this test is quite intricate. Here's the outline:
     *  - set up event observers (the server will associate our QueueSession with those observers)
     *  - kill the QueueSession
     *  - ask the bus to reconnect (gives us a new QueueSession with a different ID)
     *  - test that the pre-existing CDI observers still receive events from the server
     */

    final Runnable verifier = new Runnable() {
      @Override
      public void run() {
        System.out.println("Verifier starting");
        final EventObserverTestModule module = IOC.getBeanManager().lookupBean(EventObserverTestModule.class).getInstance();

        // assert that client received all events
        EventObserverIntegrationTest.this.verifyQualifiedEvents(module.getReceivedQualifiedEvents(), true);
        EventObserverIntegrationTest.this.verifySuperTypeEvents(module.getReceivedSuperTypeEvents());

        finishTest();
      }
    };

    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        final EventObserverTestModule module = IOC.getBeanManager().lookupBean(EventObserverTestModule.class).getInstance();

        assertNotNull(module.getStartEvent());
        module.setResultVerifier(verifier);
        // unlike the other test cases, not starting the test yet!

        final ClientMessageBus bus = (ClientMessageBus) ErraiBus.get();
        MessageBuilder.createMessage("queueSessionInvalidationService").done().sendNowWith(bus);

        new Timer() {
          @Override
          public void run() {
            System.out.println("Stopping bus...");
            bus.stop(false);
            bus.addLifecycleListener(new BusLifecycleAdapter() {
              @Override
              public void busOnline(final BusLifecycleEvent e) {
                System.out.println("Bus is back online. Starting event test module.");
                module.start();
              }
            });
          }
        }.schedule(1000);

        new Timer() {
          @Override
          public void run() {
            System.out.println("Restarting bus...");
            bus.init();
          }
        }.schedule(4000);
      }
    });

    verifyInBackupTimer(verifier, 120000);
    delayTestFinish(240000);
  }

  public void testDestroyBeanWithEventObservers() {
    final DependentEventObserverTestModule module = IOC.getBeanManager().lookupBean(DependentEventObserverTestModule.class).getInstance();
    IOC.getBeanManager().destroyBean(module);
    assertTrue("Bean wasn't destroyed", module.isDestroyed());
  }

  // Regression test for ERRAI-646
  public void testDestroyBeanWithEventObserversDoesNotUnsubscribeOtherObservers() {
    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        final DependentEventObserverTestModule module = IOC.getBeanManager().lookupBean(DependentEventObserverTestModule.class).getInstance();
        IOC.getBeanManager().destroyBean(module);
      }
    });

    new Timer() {
      @Override
      public void run() {
        testEventObservers();
      }
    }.schedule(30000);

    delayTestFinish(240000);
  }

  public void testObserverOfPrimitiveEvent() throws Exception {
    final PrimitiveEventTestModule module = IOCUtil.getInstance(PrimitiveEventTestModule.class);

    module.boolEvent.fire(false);
    module.doubleEvent.fire(1.0);
    module.floatEvent.fire(2.0f);
    module.intEvent.fire(3);
    module.longEvent.fire(4L);
    module.shortEvent.fire((short) 5);
    module.byteEvent.fire((byte) 6);
    module.charEvent.fire((char) 7);

    assertEquals(Arrays
            .asList(
                    false,
                    1.0,
                    2.0f,
                    3,
                    4L,
                    (short) 5,
                    (byte) 6,
                    (char) 7
            ), module.observed);
  }

  public void testEventWithEmptyCollectionOfSubTypeRetainsQualifiers() throws Exception {
    final long start = System.currentTimeMillis();
    final int timeLimit = 20000;
    delayTestFinish(timeLimit);
    CDI.addPostInitTask(() -> {
      final Create create = new Create() {
        @Override
        public Class<? extends Annotation> annotationType() {
          return Create.class;
        }
      };
      final Update update = new Update() {
        @Override
        public Class<? extends Annotation> annotationType() {
          return Update.class;
        }
      };
      final Delete delete = new Delete() {
        @Override
        public Class<? extends Annotation> annotationType() {
          return Delete.class;
        }
      };

      final NotifierModule module = IOCUtil.getInstance(NotifierModule.class);
      module.event.select(create).fire(new NotifierStartEvent());
      module.event.select(update).fire(new NotifierStartEvent());
      module.event.select(delete).fire(new NotifierStartEvent());

      final Runnable test = () -> {
        assertTrue(module.observed.containsKey(Create.class));
        assertEquals(1, module.observed.get(Create.class).size());
        assertTrue(module.observed.get(Create.class).get(0).getEmpty().isEmpty());

        assertTrue(module.observed.containsKey(Update.class));
        assertEquals(1, module.observed.get(Update.class).size());
        assertTrue(module.observed.get(Update.class).get(0).getEmpty().isEmpty());

        assertTrue(module.observed.containsKey(Delete.class));
        assertEquals(1, module.observed.get(Delete.class).size());
        assertTrue(module.observed.get(Delete.class).get(0).getEmpty().isEmpty());

        finishTest();
      };

      final Timer timer = new Timer() {

        @Override
        public void run() {
          try {
            test.run();
          }
          catch (final AssertionError ae) {
            if (timeLimit - (System.currentTimeMillis() - start) > 1000) {
              schedule(500);
            }
            else {
              throw ae;
            }
          }
        }
      };
      timer.schedule(500);

    });
  }

}
