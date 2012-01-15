package org.jboss.errai.cdi.event.client.test;

import java.util.List;
import java.util.Map;

import org.jboss.errai.cdi.event.client.EventObserverTestModule;
import org.jboss.errai.enterprise.client.cdi.api.CDI;

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

  public void testBusReadyEventObserver() {
    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        assertEquals("Wrong number of BusReadyEvents received:", 1,
            EventObserverTestModule.getInstance().getBusReadyEventsReceived());

        finishTest();
      }
    });
  }

  public void testEventObservers() {
    EventObserverTestModule.getInstance().setResultVerifier(new Runnable() {
      public void run() {
        Map<String, List<String>> actualEvents = EventObserverTestModule.getInstance().getReceivedEvents();

        // assert that client received all events
        EventObserverIntegrationTest.this.verifyEvents(actualEvents);
        finishTest();
      }
    });

    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        assertNotNull(EventObserverTestModule.getInstance().getStartEvent());
        EventObserverTestModule.getInstance().start();
      }
    });

    delayTestFinish(240000);
  }
}
