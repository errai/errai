package org.jboss.errai.bus.client.tests;

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.bus.client.api.BusLifecycleAdapter;
import org.jboss.errai.bus.client.api.BusLifecycleEvent;
import org.jboss.errai.bus.client.api.BusLifecycleListener;
import org.jboss.errai.bus.client.api.ErrorCallback;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.ClientMessageBusImpl;
import org.jboss.errai.bus.client.framework.Wormhole;
import org.jboss.errai.bus.client.tests.support.RecordingBusLifecycleListener;
import org.jboss.errai.bus.client.tests.support.RecordingBusLifecycleListener.EventType;

import com.google.gwt.user.client.Timer;

public class LifecycleEventTests extends AbstractErraiTest {

  private final RecordingBusLifecycleListener listener = new RecordingBusLifecycleListener();

  /**
   * If set non-null by a test, this endpoint URL will be restored during teardown.
   */
  private String originalBusEndpointUrl = null;

  @Override
  public String getModuleName() {
    return "org.jboss.errai.bus.ErraiBusTests";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    bus.addLifecycleListener(listener);
  };

  @Override
  protected void gwtTearDown() throws Exception {
    super.gwtTearDown();
    bus.removeLifecycleListener(listener);
    if (originalBusEndpointUrl != null) {
      Wormhole.changeBusEndpointUrl(bus, originalBusEndpointUrl);
    }
  }

  public void testNormalFullLifecycle() throws Exception {
    final List<EventType> expectedEventTypes = new ArrayList<EventType>();

    // we expect the bus already fired an ASSOCIATING event way before we had a
    // chance to observe it (i.e. in its constructor). So we expect the listener's
    // log to be empty at this point.
    assertEquals(expectedEventTypes, listener.getEventTypes());

    runAfterInit(new Runnable() {
      @Override
      public void run() {
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

    // we expect the bus already fired an ASSOCIATING event way before we had a
    // chance to observe it (i.e. in its constructor). So we expect the listener's
    // log to be empty at this point.
    assertEquals(expectedEventTypes, listener.getEventTypes());

    runAfterInit(new Runnable() {
      @Override
      public void run() {
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

    // we expect the bus already fired an ASSOCIATING event way before we had a
    // chance to observe it (i.e. in its constructor). So we expect the listener's
    // log to be empty at this point.
    assertEquals(expectedEventTypes, listener.getEventTypes());

    runAfterInit(new Runnable() {
      @Override
      public void run() {
        expectedEventTypes.add(EventType.ONLINE);
        assertEquals(expectedEventTypes, listener.getEventTypes());

        // simulate 404 on bus endpoint URL
        originalBusEndpointUrl = Wormhole.changeBusEndpointUrl(bus, "invalid.url");

        expectedEventTypes.add(EventType.OFFLINE);
        expectedEventTypes.add(EventType.ONLINE);
        pollUntilListenerSees(expectedEventTypes);

        // wait for failure, then set back to the correct value so bus can recover
        new Timer() {
          @Override
          public void run() {
            Wormhole.changeBusEndpointUrl(bus, originalBusEndpointUrl);
          }
        }.schedule(500);
      }
    });
  }

  public void testPersistentNetworkError() throws Exception {
    final List<EventType> expectedEventTypes = new ArrayList<EventType>();

    // we expect the bus already fired an ASSOCIATING event way before we had a
    // chance to observe it (i.e. in its constructor). So we expect the listener's
    // log to be empty at this point.
    assertEquals(expectedEventTypes, listener.getEventTypes());

    runAfterInit(new Runnable() {
      @Override
      public void run() {
        expectedEventTypes.add(EventType.ONLINE);
        assertEquals(expectedEventTypes, listener.getEventTypes());

        // simulate 404 on bus endpoint URL
        originalBusEndpointUrl = Wormhole.changeBusEndpointUrl(bus, "invalid.url");

        expectedEventTypes.add(EventType.OFFLINE);
        expectedEventTypes.add(EventType.DISASSOCIATING);
        pollUntilListenerSees(expectedEventTypes);
      }
    });
  }

  /**
   * Tests the local message delivery behaviour described in
   * {@link BusLifecycleListener#busDisassociating(BusLifecycleEvent)}: when you
   * call bus.setInitialized(true), local messages are delivered offline.
   */
  public void testLocalDeliveryAfterStoppedBus() throws Exception {
    final List<EventType> expectedEventTypes = new ArrayList<EventType>();

    // we expect the bus already fired an ASSOCIATING event way before we had a
    // chance to observe it (i.e. in its constructor). So we expect the listener's
    // log to be empty at this point.
    assertEquals(expectedEventTypes, listener.getEventTypes());

    runAfterInit(new Runnable() {
      @Override
      public void run() {
        expectedEventTypes.add(EventType.ONLINE);
        assertEquals(expectedEventTypes, listener.getEventTypes());

        // as of Errai 2.2, subscriptions must be created when bus is online
        bus.subscribeLocal("myLocalTestSubject", new MessageCallback() {
          @Override
          public void callback(Message message) {
            finishTest();
          }
        });

        // XXX in Errai 2.2, to enable local offline delivery, we have to setInitialized(true) just after the disassociating event.
        // We plan to remove setInitialized() completely in 3.0.
        bus.addLifecycleListener(new BusLifecycleAdapter() {
          @Override
          public void busDisassociating(BusLifecycleEvent e) {
            ((ClientMessageBusImpl) bus).setInitialized(true);
          }
        });

        // stop the bus and send disconnect signal to server
        bus.stop(true);

        expectedEventTypes.add(EventType.OFFLINE);
        expectedEventTypes.add(EventType.DISASSOCIATING);
        assertEquals(expectedEventTypes, listener.getEventTypes());

        MessageBuilder.createMessage("myLocalTestSubject").withValue("cows often say moo").errorsHandledBy(new ErrorCallback() {

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
   */
  public void testLocalDeliveryAfterBusRestarted() throws Exception {
    final List<EventType> expectedEventTypes = new ArrayList<EventType>();

    // we expect the bus already fired an ASSOCIATING event way before we had a
    // chance to observe it (i.e. in its constructor). So we expect the listener's
    // log to be empty at this point.
    assertEquals(expectedEventTypes, listener.getEventTypes());

    runAfterInit(new Runnable() {

      private boolean receivedLocalMessage;

      @Override
      public void run() {
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

        MessageBuilder.createMessage("myLocalTestSubject").withValue("cows often say moo").errorsHandledBy(new ErrorCallback() {

          @Override
          public boolean error(Message message, Throwable throwable) {
            throwable.printStackTrace();
            fail("Got an error sending local message");
            return false;
          }
        }).sendNowWith(bus);

        assertFalse(receivedLocalMessage);

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

  private void pollUntilListenerSees(final List<EventType> expected) {


    final Timer t = new Timer() {

      @Override
      public void run() {
        List<EventType> actual = listener.getEventTypes();
        if (expected.equals(actual)) {
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
