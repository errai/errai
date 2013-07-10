package org.jboss.errai.cdi.event.client.test;

import org.jboss.errai.cdi.event.client.DependentEventObserverTestModule;
import org.jboss.errai.cdi.event.client.EventObserverTestModule;
import org.jboss.errai.cdi.event.client.OnDemandEventObserver;
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
    return "org.jboss.errai.cdi.event.EventObserverTestModule";
  }

  public void testBusReadyEventObserver() {
    delayTestFinish(60000);
    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        EventObserverTestModule module = IOC.getBeanManager().lookupBean(EventObserverTestModule.class).getInstance();

        assertEquals("Wrong number of BusReadyEvents received:", 1, module.getBusReadyEventsReceived());
        finishTest();
      }
    });
  }

  public void testEventObservers() {
    final Runnable verifier = new Runnable() {
      @Override
      public void run() {
        EventObserverTestModule module = IOC.getBeanManager().lookupBean(EventObserverTestModule.class).getInstance();

        // assert that client received all events
        EventObserverIntegrationTest.this.verifyQualifiedEvents(module.getReceivedQualifiedEvents(), true);
        EventObserverIntegrationTest.this.verifySuperTypeEvents(module.getReceivedSuperTypeEvents());

        finishTest();
      }
    };

    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        EventObserverTestModule module = IOC.getBeanManager().lookupBean(EventObserverTestModule.class).getInstance();

        assertNotNull(module.getStartEvent());
        module.setResultVerifier(verifier);
        module.start();
      }
    });

    // only used for the case the {@see FinishEvent} was not received
    verifyInBackupTimer(verifier, 120000);
    delayTestFinish(240000);
  }

  public void testOnDemandEventObservers() {
    assertEquals("An instance of the observer already exists! This test is now pointless!",
            0, OnDemandEventObserver.instanceCount);

    final Runnable secondVerifier = new Runnable() {
      @Override
      public void run() {
        OnDemandEventObserver observer = IOC.getBeanManager().lookupBean(OnDemandEventObserver.class).getInstance();

        assertEquals(1, observer.getEventLog().size());

        finishTest();
      }
    };

    final Runnable firstVerifier = new Runnable() {
      @Override
      public void run() {
        // now creating this observer for the first time ever
        OnDemandEventObserver observer = IOC.getBeanManager().lookupBean(OnDemandEventObserver.class).getInstance();

        assertEquals(0, observer.getEventLog().size());

        EventObserverTestModule module = IOC.getBeanManager().lookupBean(EventObserverTestModule.class).getInstance();
        module.setResultVerifier(secondVerifier);
        module.start();
      }
    };

    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        EventObserverTestModule module = IOC.getBeanManager().lookupBean(EventObserverTestModule.class).getInstance();

        assertNotNull(module.getStartEvent());
        module.setResultVerifier(firstVerifier);
        module.start();
      }
    });

    // only used for the case the {@see FinishEvent} was not received
    verifyInBackupTimer(firstVerifier, 120000);
    delayTestFinish(240000);
  }

  public void testDestroyBeanWithEventObservers() {
    DependentEventObserverTestModule module = IOC.getBeanManager().lookupBean(DependentEventObserverTestModule.class).getInstance();
    IOC.getBeanManager().destroyBean(module);
    assertTrue("Bean wasn't destroyed", module.isDestroyed());
  }

}