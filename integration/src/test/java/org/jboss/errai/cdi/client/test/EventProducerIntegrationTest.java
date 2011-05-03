package org.jboss.errai.cdi.client.test;

import java.util.ArrayList;
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
     * and the expected events received by the server-side observers (right column). 
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
	public void testEventProducers() {	   
	    final Timer testInitTimer = new Timer() {
            public void run() {
                // we need to wait for the BusReadyEvent because deferred events are fired without qualifiers!
                if(CDITestProducerModule.getInstance().getBusReadyEventsReceived()) {
                    CDITestProducerModule.getInstance().fireAll();
                } else {
                    fail("Did not receive BusReadyEvent!");
                }
            }
	    };
	    testInitTimer.schedule(5000);
	    
	    final Timer testResultTimer = new Timer() {
            public void run() {
                Map<String, List<String>> receivedEvents = 
                    CDITestProducerModule.getInstance().getReceivedEventsOnServer();
                
                assertEquals("Wrong events observed on Server for @{}", expectedEvents, receivedEvents.get(""));
                assertEquals("Wrong events observed on Server for @A", expectedEventsA, receivedEvents.get("A"));
                assertEquals("Wrong events observed on Server for @B", expectedEventsB, receivedEvents.get("B"));
                assertEquals("Wrong events observed on Server for @C", expectedEventsC, receivedEvents.get("C"));
                assertEquals("Wrong events observed on Server for @AB", expectedEventsAB, receivedEvents.get("AB"));
                assertEquals("Wrong events observed on Server for @BA", expectedEventsAB, receivedEvents.get("BA"));
                assertEquals("Wrong events observed on Server for @AC", expectedEventsAC, receivedEvents.get("AC"));
                assertEquals("Wrong events observed on Server for @BC", expectedEventsBC, receivedEvents.get("BC"));
                assertEquals("Wrong events observed on Server for @ABC", expectedEventsABC, receivedEvents.get("ABC"));
                
                finishTest();
            }
        };
        testResultTimer.schedule(15000);
        delayTestFinish(20000);
	}
}
