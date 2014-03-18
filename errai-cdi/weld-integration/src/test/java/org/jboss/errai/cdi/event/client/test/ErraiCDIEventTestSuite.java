package org.jboss.errai.cdi.event.client.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    ClientLocalEventIntegrationTest.class,
    DisconnectedEventIntegrationTest.class,
    EventAdvertisingIntegrationTest.class,
    EventObserverIntegrationTest.class,
    EventProducerIntegrationTest.class,
    EventRoutingIntegrationTest.class,
    ObserverManipulationTest.class,
    ServerLocalEventIntegrationTest.class
})
public class ErraiCDIEventTestSuite {

}
