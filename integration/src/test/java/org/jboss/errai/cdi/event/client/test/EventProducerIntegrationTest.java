package org.jboss.errai.cdi.event.client.test;

import java.util.List;
import java.util.Map;

import org.jboss.errai.cdi.event.client.EventProducerTestModule;

import com.google.gwt.user.client.Timer;

/**
 * Tests CDI event producers.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class EventProducerIntegrationTest extends AbstractEventIntegrationTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.event.EventProducerTestModule";
  }

  @Override
  public void gwtSetUp() throws Exception {
    super.gwtSetUp();
  }

  public void testInjectedEvents() {
    assertNotNull(EventProducerTestModule.getInstance().getEvent());
    assertNotNull(EventProducerTestModule.getInstance().getEventA());
    assertNotNull(EventProducerTestModule.getInstance().getEventB());
    assertNotNull(EventProducerTestModule.getInstance().getEventC());
    assertNotNull(EventProducerTestModule.getInstance().getEventAB());
    assertNotNull(EventProducerTestModule.getInstance().getEventAC());
    assertNotNull(EventProducerTestModule.getInstance().getEventBC());
    assertNotNull(EventProducerTestModule.getInstance().getEventABC());
  }

  public void testEventProducers() {
    final Timer testInitTimer = new Timer() {
      public void run() {
        // we need to wait for the BusReadyEvent because deferred events are fired without qualifiers!
        if (EventProducerTestModule.getInstance().getBusReadyEventsReceived()) {
          EventProducerTestModule.getInstance().fireAll();
        } else {
          fail("Did not receive a BusReadyEvent!");
        }
      }
    };
    testInitTimer.schedule(5000);

    final Timer testResultTimer = new Timer() {
      public void run() {
        Map<String, List<String>> actualEvents = EventProducerTestModule.getInstance().getReceivedEventsOnServer();

        // assert that the server received all events
        EventProducerIntegrationTest.this.verifyEvents(actualEvents);
        finishTest();
      }
    };
    testResultTimer.schedule(20000);
    delayTestFinish(25000);
  }
}
