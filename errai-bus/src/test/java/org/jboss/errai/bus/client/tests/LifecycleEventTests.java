package org.jboss.errai.bus.client.tests;

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.Wormhole;
import org.jboss.errai.bus.client.tests.support.RecordingBusLifecycleListener;
import org.jboss.errai.bus.client.tests.support.RecordingBusLifecycleListener.EventType;

import com.google.gwt.user.client.Timer;

public class LifecycleEventTests extends AbstractErraiTest {

  private final RecordingBusLifecycleListener listener = new RecordingBusLifecycleListener();

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
        final String oldUrl = Wormhole.changeBusEndpointUrl(bus, "invalid.url");

        expectedEventTypes.add(EventType.OFFLINE);
        expectedEventTypes.add(EventType.ONLINE);
        pollUntilListenerSees(expectedEventTypes);

        // wait for failure, then set back to the correct value so bus can recover
        new Timer() {
          @Override
          public void run() {
            Wormhole.changeBusEndpointUrl(bus, oldUrl);
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
        Wormhole.changeBusEndpointUrl(bus, "invalid.url");

        expectedEventTypes.add(EventType.OFFLINE);
        expectedEventTypes.add(EventType.DISASSOCIATING);
        pollUntilListenerSees(expectedEventTypes);
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
