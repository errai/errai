package org.jboss.errai.cdi.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.cdi.client.api.Event;
import org.jboss.errai.cdi.client.events.BusReadyEvent;
import org.jboss.errai.cdi.client.qualifier.A;
import org.jboss.errai.cdi.client.qualifier.B;
import org.jboss.errai.cdi.client.qualifier.C;
import org.jboss.errai.ioc.client.api.EntryPoint;

@EntryPoint
public class CDITestObserverModule {
    private static int busReadyEventsReceived = 0;
    private static CDITestObserverModule instance;
    private Map<String, List<String>> receivedEvents = new HashMap<String, List<String>>();
    
    @Inject
    private Event<CDITestStartEvent> startEvent;
    
	@PostConstruct
	public void doPostConstruct() {
		instance = this;
	}

	public static CDITestObserverModule getInstance() {
        return instance;
    }
	
	public static int getBusReadyEventsReceived() {
	    return busReadyEventsReceived;
	}
	
	public Map<String, List<String>> getReceivedEvents() {
	    return receivedEvents;
	}
	
	public Event<CDITestStartEvent> getStartEvent() {
	    return startEvent;
	}
	
	public void onBusReady(@Observes BusReadyEvent event) {
		busReadyEventsReceived++;
	}
	
	public void start() {
	    startEvent.fire(new CDITestStartEvent());
	}
	
	public void onEvent(@Observes String event) {
	    addEvent("", event);
	}
	
	public void onEventA(@Observes @A String event) {
	    addEvent("A", event);
    }

    public void onEventB(@Observes @B String event) {
        addEvent("B", event);
    }

    public void onEventC(@Observes @C String event) {
        addEvent("C", event);
    }
    
    public void onEventAB(@Observes @A @B String event) {
        addEvent("AB", event);
    }
    
    public void onEventBA(@Observes @B @A String event) {
        addEvent("BA", event);
    }
    
    public void onEventAC(@Observes @A @C String event) {
        addEvent("AC", event);
    }
    
    public void onEventBC(@Observes @B @C String event) {
        addEvent("BC", event);
    }
    
    public void onEventABC(@Observes @A @B @C String event) {
        addEvent("ABC", event);
    }
    
    private void addEvent(String receiver, String event) {
        List<String> events = receivedEvents.get(receiver);
        if(events==null) events = new ArrayList<String>();
        events.add(event);
        receivedEvents.put(receiver, events);
    }
}