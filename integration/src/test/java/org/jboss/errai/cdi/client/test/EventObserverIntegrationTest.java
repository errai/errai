package org.jboss.errai.cdi.client.test;

import java.util.ArrayList;
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
    private static final List<String> expectedEvents = new ArrayList<String>() {{
        add("");
        add("A");
        add("B");
        add("C");
        add("AB");
        add("AC");
        add("BC");
        add("ABC");
    }};
    
    private static final List<String> expectedEventsA = new ArrayList<String>() {{
        add("A");
        add("AB");
        add("AC");
        add("ABC");
    }};

    private static final List<String> expectedEventsB = new ArrayList<String>() {{
        add("B");
        add("AB");
        add("BC");
        add("ABC");
    }};
    
    private static final List<String> expectedEventsC = new ArrayList<String>() {{
        add("C");
        add("AC");
        add("BC");
        add("ABC");
    }};
    
    private static final List<String> expectedEventsAB = new ArrayList<String>() {{
        add("AB");
        add("ABC");
    }};

    private static final List<String> expectedEventsAC = new ArrayList<String>() {{
        add("AC");
        add("ABC");
    }};
    
    private static final List<String> expectedEventsBC = new ArrayList<String>() {{
        add("BC");
        add("ABC");
    }};
    
    private static final List<String> expectedEventsABC = new ArrayList<String>() {{
        add("ABC");
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
	 * and the expected events received by the client-side observers (right column). 
	 * We basically fire all combinations of qualified events using three 
	 * qualifiers (@A @B @C) and test if each observer received its corresponding events,
	 * and only those events. The {} empty set is used to describe an event without
	 * qualifiers.  
	 * 
	 * {}     => {}
	 * A      => {},A
	 * B      => {},B
	 * C      => {},C
	 * A,B    => {},A,B,AB,(BA (redundant but still worth testing)) 
	 * A,C    => {},A,C,AC
	 * B,C    => {},B,C,BC
     * A,B,C  => {},A,B,C,AB,BC,AC,ABC,(BA (redundant but still worth testing)) 
	 */
	public void testEventObservers() {
	    assertNotNull(CDITestObserverModule.getInstance().getStartEvent());
	    CDITestObserverModule.getInstance().start();
	    
	    Timer timer = new Timer() {
            public void run() {
                Map<String, List<String>> receivedEvents = 
                    CDITestObserverModule.getInstance().getReceivedEvents();
                
                assertEquals("Wrong events observed for @{}", expectedEvents, receivedEvents.get(""));
                assertEquals("Wrong events observed for @A", expectedEventsA, receivedEvents.get("A"));
                assertEquals("Wrong events observed for @B", expectedEventsB, receivedEvents.get("B"));
                assertEquals("Wrong events observed for @C", expectedEventsC, receivedEvents.get("C"));
                assertEquals("Wrong events observed for @AB", expectedEventsAB, receivedEvents.get("AB"));
                assertEquals("Wrong events observed for @BA", expectedEventsAB, receivedEvents.get("BA"));
                assertEquals("Wrong events observed for @AC", expectedEventsAC, receivedEvents.get("AC"));
                assertEquals("Wrong events observed for @BC", expectedEventsBC, receivedEvents.get("BC"));
                assertEquals("Wrong events observed for @ABC", expectedEventsABC, receivedEvents.get("ABC"));
                
                finishTest();
            }
        };
        timer.schedule(10000);
        delayTestFinish(15000);
	}
}
