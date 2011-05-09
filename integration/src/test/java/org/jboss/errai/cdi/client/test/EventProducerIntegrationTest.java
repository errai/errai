package org.jboss.errai.cdi.client.test;

import java.util.List;
import java.util.Map;

import org.jboss.errai.cdi.client.CDITestProducerModule;

import com.google.gwt.user.client.Timer;

/**
 * Tests CDI event producers.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class EventProducerIntegrationTest extends AbstractEventIntegrationTest {

    @Override
    public String getModuleName() {
        return "org.jboss.errai.cdi.CDITestProducerModule";
    }

    @Override
    public void gwtSetUp() throws Exception {
        super.gwtSetUp();

    }

    public void testInjectedEvents() {
        assertNotNull(CDITestProducerModule.getInstance().getEvent());
        assertNotNull(CDITestProducerModule.getInstance().getEventA());
        assertNotNull(CDITestProducerModule.getInstance().getEventB());
        assertNotNull(CDITestProducerModule.getInstance().getEventC());
        assertNotNull(CDITestProducerModule.getInstance().getEventAB());
        assertNotNull(CDITestProducerModule.getInstance().getEventAC());
        assertNotNull(CDITestProducerModule.getInstance().getEventBC());
        assertNotNull(CDITestProducerModule.getInstance().getEventABC());
    }

    public void testEventProducers() {
        final Timer testInitTimer = new Timer() {
            public void run() {
                // we need to wait for the BusReadyEvent because deferred events are fired without qualifiers!
                if (CDITestProducerModule.getInstance().getBusReadyEventsReceived()) {
                    CDITestProducerModule.getInstance().fireAll();
                } else {
                    fail("Did not receive a BusReadyEvent!");
                }
            }
        };
        testInitTimer.schedule(5000);

        final Timer testResultTimer = new Timer() {
            public void run() {
                Map<String, List<String>> actualEvents = CDITestProducerModule.getInstance()
                        .getReceivedEventsOnServer();

                // assert that the server received all events
                EventProducerIntegrationTest.this.verifyEvents(actualEvents);
                finishTest();
            }
        };
        testResultTimer.schedule(20000);
        delayTestFinish(25000);
    }
}
