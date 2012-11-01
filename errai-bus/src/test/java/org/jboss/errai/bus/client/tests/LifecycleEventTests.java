package org.jboss.errai.bus.client.tests;

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.bus.client.tests.support.RecordingBusLifecycleListener;
import org.jboss.errai.bus.client.tests.support.RecordingBusLifecycleListener.EventType;

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
}
