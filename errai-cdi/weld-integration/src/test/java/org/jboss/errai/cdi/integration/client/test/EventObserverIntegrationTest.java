package org.jboss.errai.cdi.integration.client.test;

import java.util.List;
import java.util.Map;

import org.jboss.errai.cdi.integration.client.EventObserverTestModule;
import org.jboss.errai.common.client.api.extension.InitVotes;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.ioc.client.container.IOC;

/**
 * Tests CDI event observers.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class EventObserverIntegrationTest extends AbstractEventIntegrationTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.integration.EventObserverTestModule";
  }

  public void testBusReadyEventObserver() {
    InitVotes.registerOneTimeInitCallback(new Runnable() {
      @Override
      public void run() {
        EventObserverTestModule module =
                IOC.getBeanManager().lookupBean(EventObserverTestModule.class)
                        .getInstance();

        assertEquals("Wrong number of BusReadyEvents received:", 1,
                module.getBusReadyEventsReceived());

        finishTest();
      }
    });
  }

  public void testEventObservers() {
    final Runnable verifier = new Runnable() {
      public void run() {
        EventObserverTestModule module
                = IOC.getBeanManager().lookupBean(EventObserverTestModule.class)
                .getInstance();

        Map<String, List<String>> actualEvents = module.getReceivedEvents();

        // assert that client received all events
        EventObserverIntegrationTest.this.verifyEvents(actualEvents);
        finishTest();
      }
    };

    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        EventObserverTestModule module
                = IOC.getBeanManager().lookupBean(EventObserverTestModule.class)
                .getInstance();

        assertNotNull(module.getStartEvent());
        module.setResultVerifier(verifier);
        module.start();
      }
    });

    // only used for the case the {@see FinishEvent} was not received
    verifyInBackupTimer(verifier, 120000);
    delayTestFinish(240000);
  }
}
