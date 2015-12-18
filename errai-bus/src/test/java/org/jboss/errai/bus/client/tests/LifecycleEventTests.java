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

package org.jboss.errai.bus.client.tests;

import com.google.gwt.user.client.Timer;
import org.jboss.errai.bus.client.api.BusErrorCallback;
import org.jboss.errai.bus.client.api.BusLifecycleAdapter;
import org.jboss.errai.bus.client.api.BusLifecycleEvent;
import org.jboss.errai.bus.client.api.BusLifecycleListener;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.base.TransportIOException;
import org.jboss.errai.bus.client.api.TransportError;
import org.jboss.errai.bus.client.framework.Wormhole;
import org.jboss.errai.bus.client.tests.support.RecordingBusLifecycleListener;
import org.jboss.errai.bus.client.tests.support.RecordingBusLifecycleListener.EventType;
import org.jboss.errai.bus.client.tests.support.RecordingBusLifecycleListener.RecordedEvent;

import java.util.ArrayList;
import java.util.List;

public class LifecycleEventTests extends AbstractErraiTest {

  private final RecordingBusLifecycleListener listener = new RecordingBusLifecycleListener();

  /**
   * Listeners that will be removed from the bus in gwtTearDown(). Tests can add their own listeners.
   * This is important because the bus gets
   */
  private final List<BusLifecycleListener> listenersToRemove = new ArrayList<BusLifecycleListener>();

  /**
   * If set non-null by a test, this endpoint URL will be restored during teardown.
   */
  private Wormhole.Fixer endpointFixer = null;

  @Override
  public String getModuleName() {
    return "org.jboss.errai.bus.ErraiBusTests";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    bus.addLifecycleListener(listener);
  }

  @Override
  protected void gwtTearDown() throws Exception {
    for (BusLifecycleListener listener : listenersToRemove) {
      bus.removeLifecycleListener(listener);
    }
    if (endpointFixer != null) {
      endpointFixer.fix();
    }
    super.gwtTearDown();
  }

  public void testNormalFullLifecycle() throws Exception {
    final List<EventType> expectedEventTypes = new ArrayList<EventType>();

    assertEquals(expectedEventTypes, listener.getEventTypes());

    runAfterInit(new Runnable() {
      @Override
      public void run() {
        expectedEventTypes.add(EventType.ASSOCIATING);
        expectedEventTypes.add(EventType.ONLINE);
        assertEquals(expectedEventTypes, listener.getEventTypes());

        bus.stop(true);

        expectedEventTypes.add(EventType.OFFLINE);
        expectedEventTypes.add(EventType.DISASSOCIATING);
        assertEquals(expectedEventTypes, listener.getEventTypes());

        finishTest();
      }
    });
  }

  public void testRecoverFromExpiredSession() throws Exception {
    final List<EventType> expectedEventTypes = new ArrayList<EventType>();

    assertEquals(expectedEventTypes, listener.getEventTypes());

    runAfterInit(new Runnable() {
      @Override
      public void run() {
        expectedEventTypes.add(EventType.ASSOCIATING);
        expectedEventTypes.add(EventType.ONLINE);
        assertEquals(expectedEventTypes, listener.getEventTypes());

        // simulate session expiration
        MessageBuilder.createMessage()
            .toSubject("ExpiryService")
            .signalling().noErrorHandling().sendNowWith(bus);

        expectedEventTypes.add(EventType.OFFLINE);

        // currently, session renewal involves a full stop of the bus. this isn't particularly critical
        // for end-user applications to see, so if this test fails later by producing [OFFLINE, ONLINE]
        // we could consider that a win.
        expectedEventTypes.add(EventType.DISASSOCIATING);
        expectedEventTypes.add(EventType.ASSOCIATING);

        expectedEventTypes.add(EventType.ONLINE);
        pollUntilListenerSees(expectedEventTypes);
      }
    });
  }

  public void testRecoverFromNetworkError() throws Exception {
    final List<EventType> expectedEventTypes = new ArrayList<EventType>();

    assertEquals(expectedEventTypes, listener.getEventTypes());

    runAfterInit(new Runnable() {
      @Override
      public void run() {
        expectedEventTypes.add(EventType.ASSOCIATING);
        expectedEventTypes.add(EventType.ONLINE);
        assertEquals(expectedEventTypes, listener.getEventTypes());

        // simulate 404 on bus endpoint URL
        endpointFixer = Wormhole.changeBusEndpointUrl(bus, "invalid.url");

        expectedEventTypes.add(EventType.OFFLINE);
        expectedEventTypes.add(EventType.ONLINE);
        pollUntilListenerSees(expectedEventTypes);

        // wait for failure, then set back to the correct value so bus can recover
        new Timer() {
          @Override
          public void run() {
            endpointFixer.fix();
          }
        }.schedule(5000);
      }
    });
  }

  //todo: now must test that bus keeps trying
  public void ignoreTestPersistentNetworkError() throws Exception {
    final List<EventType> expectedEventTypes = new ArrayList<EventType>();

    assertEquals(expectedEventTypes, listener.getEventTypes());

    runAfterInit(new Runnable() {
      @Override
      public void run() {
        expectedEventTypes.add(EventType.ASSOCIATING);
        expectedEventTypes.add(EventType.ONLINE);
        assertEquals(expectedEventTypes, listener.getEventTypes());

        // simulate 404 on bus endpoint URL
        endpointFixer = Wormhole.changeBusEndpointUrl(bus, "invalid.url");

        expectedEventTypes.add(EventType.OFFLINE);
        expectedEventTypes.add(EventType.DISASSOCIATING);
        pollUntilListenerSees(expectedEventTypes);
      }
    });
  }

  public void ignoreTestAppDirectedRecoveryFromPersistentNetworkError() throws Exception {

    System.out.println("Begin testAppDirectedRecoveryFromPersistentNetworkError()");
    final BusLifecycleListener reattacher = new BusLifecycleAdapter() {
      @Override
      public void busDisassociating(BusLifecycleEvent e) {
        // simulate server back online after extended outage
        // (or changing endpoint to fail over to an online server)

        endpointFixer.fix();
      //  Wormhole.changeBusEndpointUrl(bus, originalBusEndpointUrl);

        // explicit bus restart (in a timer so it doesn't make other listeners
        // see events out of order due to recursive event delivery)
        new Timer() {
          @Override
          public void run() {
            bus.init();
          }
        }.schedule(1);
      }
    };
    bus.addLifecycleListener(reattacher);
    listenersToRemove.add(reattacher);

    final List<EventType> expectedEventTypes = new ArrayList<EventType>();

    assertEquals(expectedEventTypes, listener.getEventTypes());

    runAfterInit(new Runnable() {
      @Override
      public void run() {
        expectedEventTypes.add(EventType.ASSOCIATING);
        expectedEventTypes.add(EventType.ONLINE);
        assertEquals(expectedEventTypes, listener.getEventTypes());

        // simulate 404 on bus endpoint URL
        endpointFixer = Wormhole.changeBusEndpointUrl(bus, "invalid.url");

        expectedEventTypes.add(EventType.OFFLINE);
        expectedEventTypes.add(EventType.DISASSOCIATING);
        // our lifecycle listener kicks in here
        expectedEventTypes.add(EventType.ASSOCIATING);
        expectedEventTypes.add(EventType.ONLINE);
        pollUntilListenerSees(expectedEventTypes);

        System.out.println("End testAppDirectedRecoveryFromPersistentNetworkError()");

      }
    });
  }

  /**
   * Tests the local message delivery behaviour described in
   * {@link BusLifecycleListener#busDisassociating(BusLifecycleEvent)}: when you
   * call bus.setInitialized(true), local messages are delivered offline.
   */
  public void testLocalDeliveryAfterStoppedBus() throws Exception {
    System.out.println("Begin testLocalDeliveryAfterStoppedBus()");
    final List<EventType> expectedEventTypes = new ArrayList<EventType>();

    assertEquals(expectedEventTypes, listener.getEventTypes());

    runAfterInit(new Runnable() {
      @Override
      public void run() {
        expectedEventTypes.add(EventType.ASSOCIATING);
        expectedEventTypes.add(EventType.ONLINE);
        assertEquals(expectedEventTypes, listener.getEventTypes());

        // as of Errai 2.2, subscriptions must be created when bus is online
        bus.subscribeLocal("myLocalTestSubject", new MessageCallback() {
          @Override
          public void callback(Message message) {
            finishTest();
          }
        });

        // stop the bus and send disconnect signal to server
        bus.stop(true);

        expectedEventTypes.add(EventType.OFFLINE);
        expectedEventTypes.add(EventType.DISASSOCIATING);
        assertEquals(expectedEventTypes, listener.getEventTypes());

        MessageBuilder.createMessage("myLocalTestSubject").withValue("cows often say moo")
            .errorsHandledBy(new BusErrorCallback() {

              @Override
              public boolean error(Message message, Throwable throwable) {
                throwable.printStackTrace();
                fail("Got an error sending local message");
                return false;
              }
            }).sendNowWith(bus);
      }
    });
  }

  /**
   * Tests the local message delivery behaviour described in
   * {@link BusLifecycleListener#busDisassociating(BusLifecycleEvent)}: when you
   * do not call bus.setInitialized(true), local message delivery is deferred
   * until the bus reconnects.
   * <p/>
   * <p/>
   * NOTE: The contract is no longer true. If a local message can be delivered, it will be, regardless of bus state.
   */
  public void ignoreTestLocalDeliveryAfterBusRestarted() throws Exception {
    final List<EventType> expectedEventTypes = new ArrayList<EventType>();

    // we expect the bus already fired an ASSOCIATING event way before we had a
    // chance to observe it (i.e. in its constructor). So we expect the listener's
    // log to be empty at this point.
    assertEquals(expectedEventTypes, listener.getEventTypes());

    runAfterInit(new Runnable() {

      private boolean receivedLocalMessage;

      @Override
      public void run() {
        expectedEventTypes.add(EventType.ASSOCIATING);
        expectedEventTypes.add(EventType.ONLINE);
        assertEquals(expectedEventTypes, listener.getEventTypes());

        // as of Errai 2.2, subscriptions must be created when bus is online
        bus.subscribeLocal("myLocalTestSubject", new MessageCallback() {
          @Override
          public void callback(Message message) {
            receivedLocalMessage = true;
          }
        });

        // stop the bus and send disconnect signal to server
        bus.stop(true);

        expectedEventTypes.add(EventType.OFFLINE);
        expectedEventTypes.add(EventType.DISASSOCIATING);
        assertEquals(expectedEventTypes, listener.getEventTypes());

        MessageBuilder.createMessage("myLocalTestSubject").withValue("cows often say moo")
            .errorsHandledBy(new BusErrorCallback() {

              @Override
              public boolean error(Message message, Throwable throwable) {
                throwable.printStackTrace();
                fail("Got an error sending local message");
                return false;
              }
            }).sendNowWith(bus);

        assertFalse("Message delivery should be deferred in this state", receivedLocalMessage);

        // reconnect in 500ms
        new Timer() {
          @Override
          public void run() {
            assertFalse(receivedLocalMessage);
            bus.init();
          }
        }.schedule(500);

        // ensure the local message is received
        final Timer t = new Timer() {

          @Override
          public void run() {
            if (receivedLocalMessage) {
              finishTest();
            }
            else {
              System.out.println("Still waiting for local message");

              // poll again later
              schedule(1000);
            }
          }
        };
        t.schedule(750);

      }
    });
  }

  public void testOfflineEventHasErrorInformation() throws Exception {
    final List<EventType> expectedEventTypes = new ArrayList<EventType>();

    // we expect the bus already fired an ASSOCIATING event way before we had a
    // chance to observe it (i.e. in its constructor). So we expect the listener's
    // log to be empty at this point.
    assertEquals(expectedEventTypes, listener.getEventTypes());

    final RecordingTransportErrorHandler errorHandler = new RecordingTransportErrorHandler();
    bus.addTransportErrorHandler(errorHandler);

    runAfterInit(new Runnable() {
      @Override
      public void run() {
        // simulate 404 on bus endpoint URL
        endpointFixer = Wormhole.changeBusEndpointUrl(bus, "invalid.url");

        expectedEventTypes.add(EventType.ASSOCIATING);
        expectedEventTypes.add(EventType.ONLINE);
        expectedEventTypes.add(EventType.OFFLINE);
        pollUntilListenerSees(expectedEventTypes, new Runnable() {

          @Override
          public void run() {
            RecordedEvent recordedEvent = listener.getEvents().get(2);
            assertEquals("Picked wrong event from recorder", EventType.OFFLINE, recordedEvent.getType());
            BusLifecycleEvent actualEvent = recordedEvent.getEvent();
            TransportError error = actualEvent.getReason();
            assertNotNull("No error information", error);
            assertEquals("Wrong status code", 404, error.getStatusCode());


            // this is no longer a reliable test, since the error detection is more sophisticated
            // and can encounter a 404 error WITHOUT encountering a GWT RequestBuilder exception.
            // This is because we detect problems on both send and receive. Not just receive anymore.

         //   assertNotNull("Throwable was not provided", error.getException());
         //   assertEquals("Wrong exception type", TransportIOException.class, error.getException().getClass());

            assertNotNull("Request object was not provided", error.getRequest());
            assertTrue("Bus should be planning to retry failed connection attempt",
                error.getRetryInfo().getDelayUntilNextRetry() >= 0);
            assertEquals(0, error.getRetryInfo().getRetryCount());

            List<TransportError> transportErrors = errorHandler.getTransportErrors();
            assertTrue("No errors were recorded", !transportErrors.isEmpty());

            // It's possible for the send and receive channels to both encounter errors at the same time
            // in an unpredictable manner, making this assertion unreliable.
            // assertTrue("Got too many errors: " + transportErrors, transportErrors.size() <= 2);

            assertSame("Lifecycle listener and error handler should see exact same TransportError object",
                transportErrors.get(0), error);
          }
        });
      }
    });

  }

  public void ignoreTestDisassociatingEventHasErrorInformation() throws Exception {
    final List<EventType> expectedEventTypes = new ArrayList<EventType>();

    // we expect the bus already fired an ASSOCIATING event way before we had a
    // chance to observe it (i.e. in its constructor). So we expect the listener's
    // log to be empty at this point.
    assertEquals(expectedEventTypes, listener.getEventTypes());

    final RecordingTransportErrorHandler errorHandler = new RecordingTransportErrorHandler();
    bus.addTransportErrorHandler(errorHandler);

    runAfterInit(new Runnable() {
      @Override
      public void run() {
        // simulate 404 on bus endpoint URL
        endpointFixer = Wormhole.changeBusEndpointUrl(bus, "invalid.url");

        expectedEventTypes.add(EventType.ONLINE);
        expectedEventTypes.add(EventType.OFFLINE);
        expectedEventTypes.add(EventType.DISASSOCIATING);
        pollUntilListenerSees(expectedEventTypes, new Runnable() {

          @Override
          public void run() {
            RecordedEvent recordedEvent = listener.getEvents().get(2);
            assertEquals("Picked wrong event from recorder", EventType.DISASSOCIATING, recordedEvent.getType());
            BusLifecycleEvent actualEvent = recordedEvent.getEvent();
            TransportError error = actualEvent.getReason();
            assertNotNull("No error information", error);
            assertEquals("Wrong status code", 404, error.getStatusCode());
            assertNotNull("Throwable was not provided", error.getException());
            assertEquals("Wrong exception type", TransportIOException.class, error.getException().getClass());
            assertNotNull("Request object was not provided", error.getRequest());
            assertEquals(-1L, error.getRetryInfo().getDelayUntilNextRetry());
            assertTrue("Expected at least a few retries before bus gave up", error.getRetryInfo().getRetryCount() > 2);

            List<TransportError> transportErrors = errorHandler.getTransportErrors();
            assertSame("Lifecycle listener and error handler should see exact same TransportError object",
                transportErrors.get(transportErrors.size() - 1), error);

            int expectedRetryCount = 0;
            for (TransportError oldError : transportErrors.subList(0, transportErrors.size() - 1)) {
              assertTrue(oldError.getRetryInfo().getDelayUntilNextRetry() > 0);
              assertEquals(expectedRetryCount, oldError.getRetryInfo().getRetryCount());
              expectedRetryCount++;
            }
          }
        });
      }
    });
  }

  private void pollUntilListenerSees(final List<EventType> expected) {
    pollUntilListenerSees(expected, null);
  }

  private void pollUntilListenerSees(final List<EventType> expected, final Runnable doAfter) {
    final Timer t = new Timer() {

      @Override
      public void run() {
        List<EventType> actual = listener.getEventTypes();
        if (expected.equals(actual)) {
          if (doAfter != null) {
            doAfter.run();
          }
          finishTest();
        }
        else {
          System.out.println("Lists do not match yet:");
          System.out.println("  expected: " + expected);
          System.out.println("  actual:   " + actual);

          // poll again later
          schedule(1000);
        }
      }
    };
    t.schedule(1000);
  }
}
