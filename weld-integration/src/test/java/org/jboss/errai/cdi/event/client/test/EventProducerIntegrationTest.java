package org.jboss.errai.cdi.event.client.test;

import java.util.List;
import java.util.Map;

import org.jboss.errai.cdi.event.client.EventProducerTestModule;
import org.jboss.errai.enterprise.client.cdi.api.CDI;

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
    final Runnable verifier = new Runnable() {
      public void run() {
        Map<String, List<String>> actualEvents = EventProducerTestModule.getInstance().getReceivedEventsOnServer();

        // assert that the server received all events
        EventProducerIntegrationTest.this.verifyEvents(actualEvents);
        finishTest();
      }
    };

    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        if (EventProducerTestModule.getInstance().getBusReadyEventsReceived()) {
          EventProducerTestModule.getInstance().setResultVerifier(verifier);
          EventProducerTestModule.getInstance().fireAll();
        }
        else {
          fail("Did not receive a BusReadyEvent!");
        }
      }
    });

    // only used for the case the {@see FinishEvent} was not received.
    verifyInBackupTimer(verifier, 120000);
    delayTestFinish(240000);
  }
}
