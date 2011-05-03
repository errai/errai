package org.jboss.errai.cdi.client.test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.errai.cdi.client.CDITestObserverModule;

import com.google.gwt.user.client.Timer;

/**
 * Tests CDI event observers.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@SuppressWarnings("serial")
public class EventObserverIntegrationTest extends AbstractErraiCDITest {

    private static final Map<String, List<String>> expectedEvents = new HashMap<String, List<String>>() {{
            put("", Arrays.asList(new String[] { "", "A", "B", "C", "AB", "AC", "BC", "ABC" }));
            put("A", Arrays.asList(new String[] { "A", "AB", "AC", "ABC" }));
            put("B", Arrays.asList(new String[] { "B", "AB", "BC", "ABC" }));
            put("C", Arrays.asList(new String[] { "C", "AC", "BC", "ABC" }));
            put("AB", Arrays.asList(new String[] { "AB", "ABC" }));
            put("AC", Arrays.asList(new String[] { "AC", "ABC" }));
            put("BC", Arrays.asList(new String[] { "BC", "ABC" }));
            put("ABC", Arrays.asList(new String[] { "ABC" }));
    }};

    @Override
    public String getModuleName() {
        return "org.jboss.errai.cdi.CDITestObserverModule";
    }

    public void testBusReadyEventObserver() {
        Timer timer = new Timer() {
            public void run() {
                assertEquals("Wrong number of BusReadyEvents received:", 1, 
                        CDITestObserverModule.getInstance().getBusReadyEventsReceived());
                finishTest();
            }
        };
        timer.schedule(10000);
        delayTestFinish(15000);
    }

    /**
     * The following table describes the events being fired on the server (left column) 
     * and the client-side observers expected to receive the events (right column). 
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
    public void testEventObservers() {
        assertNotNull(CDITestObserverModule.getInstance().getStartEvent());
        CDITestObserverModule.getInstance().start();

        Timer timer = new Timer() {
            public void run() {
                Map<String, List<String>> actualEvents = CDITestObserverModule.getInstance().getReceivedEvents();

                assertEquals("Wrong events observed for @{}", expectedEvents.get(""), actualEvents.get(""));
                assertEquals("Wrong events observed for @A", expectedEvents.get("A"), actualEvents.get("A"));
                assertEquals("Wrong events observed for @B", expectedEvents.get("B"), actualEvents.get("B"));
                assertEquals("Wrong events observed for @C", expectedEvents.get("C"), actualEvents.get("C"));
                assertEquals("Wrong events observed for @AB", expectedEvents.get("AB"), actualEvents.get("AB"));
                assertEquals("Wrong events observed for @BA", expectedEvents.get("AB"), actualEvents.get("BA"));
                assertEquals("Wrong events observed for @AC", expectedEvents.get("AC"), actualEvents.get("AC"));
                assertEquals("Wrong events observed for @BC", expectedEvents.get("BC"), actualEvents.get("BC"));
                assertEquals("Wrong events observed for @ABC", expectedEvents.get("ABC"), actualEvents.get("ABC"));

                finishTest();
            }
        };
        timer.schedule(10000);
        delayTestFinish(15000);
    }
}
