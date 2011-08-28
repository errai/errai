package org.jboss.errai.cdi.event.client.test;

import java.util.List;
import java.util.Map;

import org.jboss.errai.cdi.event.client.EventObserverTestModule;

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

  public void testBusReadyEventObserver() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        assertEquals("Wrong number of BusReadyEvents received:", 1, EventObserverTestModule.getInstance()
                .getBusReadyEventsReceived());
        finishTest();
      }
    });
  }

  public void testEventObservers() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        assertNotNull(EventObserverTestModule.getInstance().getStartEvent());
        EventObserverTestModule.getInstance().start();

        EventObserverTestModule.getInstance().registerCallback(9, 
            new Runnable() {
                public void run() {
                    Map<String, List<String>> actualEvents = EventObserverTestModule.getInstance().getReceivedEvents();
  
                    // assert that client received all events
                    EventObserverIntegrationTest.this.verifyEvents(actualEvents);
                    finishTest();
                 }
            });
      }
    }, 2000);
  }
}
