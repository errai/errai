package org.jboss.errai.cdi.client.test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.errai.cdi.client.CDITestProducerModule;

import com.google.gwt.user.client.Timer;

/**
 * Tests CDI event observers.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@SuppressWarnings("serial")
public class EventProducerIntegrationTest extends AbstractErraiCDITest {

    private static final Map<String, List<String>> expectedEvents = new HashMap<String, List<String>>() {{
        put("", Arrays.asList(new String[]{"","A","B","C","AB","AC","BC","ABC"}));
        put("A", Arrays.asList(new String[]{"A","AB","AC","ABC"}));
        put("B", Arrays.asList(new String[]{"B","AB","BC","ABC"}));
        put("C", Arrays.asList(new String[]{"C","AC","BC","ABC"}));
        put("AB", Arrays.asList(new String[]{"AB","ABC"}));
        put("AC", Arrays.asList(new String[]{"AC","ABC"}));
        put("BC", Arrays.asList(new String[]{"BC","ABC"}));
        put("ABC", Arrays.asList(new String[]{"ABC"}));
    }};

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

    /**
     * The following table describes the events being fired on the client (left column) 
     * and the server-side observers expected to receive the events (right column). 
     * We basically fire all combinations of qualified events using three 
     * qualifiers ( @A @B @C ) and test if each observer received its corresponding events,
     * and only those events. The {} empty set is used to describe an event without
     * qualifiers.  
     * 
     * {}     => {}
     * A      => {},A
     * B      => {},B
     * C      => {},C
     * A,B    => {},A,B,AB,(BA (redundant, but still worth testing to make sure that the sequence doesn't matter)) 
     * A,C    => {},A,C,AC
     * B,C    => {},B,C,BC
     * A,B,C  => {},A,B,C,AB,BA,BC,AC,ABC 
     */
    public void testEventProducers() {
        final Timer testInitTimer = new Timer() {
            public void run() {
                // we need to wait for the BusReadyEvent because deferred events are fired without qualifiers!
                if (CDITestProducerModule.getInstance().getBusReadyEventsReceived()) {
                    CDITestProducerModule.getInstance().fireAll();
                } else {
                    fail("Did not receive BusReadyEvent!");
                }
            }
        };
        testInitTimer.schedule(5000);

        final Timer testResultTimer = new Timer() {
            public void run() {
                Map<String, List<String>> actualEvents = CDITestProducerModule.getInstance()
                        .getReceivedEventsOnServer();

                assertEquals("Server got wrong events for @{}", expectedEvents.get(""), actualEvents.get(""));
                assertEquals("Server got wrong events for @A", expectedEvents.get("A"), actualEvents.get("A"));
                assertEquals("Server got wrong events for @B", expectedEvents.get("B"), actualEvents.get("B"));
                assertEquals("Server got wrong events for @C", expectedEvents.get("C"), actualEvents.get("C"));
                assertEquals("Server got wrong events for @AB", expectedEvents.get("AB"), actualEvents.get("AB"));
                assertEquals("Server got wrong events for @BA", expectedEvents.get("AB"), actualEvents.get("BA"));
                assertEquals("Server got wrong events for @AC", expectedEvents.get("AC"), actualEvents.get("AC"));
                assertEquals("Server got wrong events for @BC", expectedEvents.get("BC"), actualEvents.get("BC"));
                assertEquals("Server got wrong events for @ABC", expectedEvents.get("ABC"), actualEvents.get("ABC"));

                finishTest();
            }
        };
        testResultTimer.schedule(15000);
        delayTestFinish(20000);
    }
}
