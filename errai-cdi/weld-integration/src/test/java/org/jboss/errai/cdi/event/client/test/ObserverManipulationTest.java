package org.jboss.errai.cdi.event.client.test;

import org.jboss.errai.enterprise.client.cdi.AbstractCDIEventCallback;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.enterprise.client.cdi.api.CDI;

/**
 * A suite of tests that change the CDI observer lists while an event is being delivered.
 * Initially, this was a regression test for ERRAI-632.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
public class ObserverManipulationTest extends AbstractErraiCDITest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.event.EventObserverTestModule";
  }

  private int eventCount;

  private class EventCounter extends AbstractCDIEventCallback<String> {
    @Override
    protected void fireEvent(String event) {
      eventCount++;
      System.out.println("EventCounter got event. Count = " + eventCount);
    }
  }

  public void testAddObserverDuringEventDelivery() throws Exception {
    CDI.subscribe(String.class.getName(), new EventCounter());
    CDI.subscribe(String.class.getName(), new EventCounter());
    CDI.subscribe(String.class.getName(), new AbstractCDIEventCallback<String>() {
      @Override
      protected void fireEvent(String event) {
        System.out.println("About to add new observer during event delivery");
        CDI.subscribe(String.class.getName(), new EventCounter());
      }
    });
    CDI.subscribe(String.class.getName(), new EventCounter());
    CDI.subscribe(String.class.getName(), new EventCounter());
    CDI.subscribe(String.class.getName(), new EventCounter());

    // before the ERRAI-632 fix, the next line was throwing ConcurrentModificationException
    CDI.fireEvent("Holey Moley!");

    assertEquals(5, eventCount);
  }
}
