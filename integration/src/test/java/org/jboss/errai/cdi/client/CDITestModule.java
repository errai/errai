package org.jboss.errai.cdi.client;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.cdi.client.api.Event;
import org.jboss.errai.cdi.client.events.BusReadyEvent;
import org.jboss.errai.ioc.client.api.EntryPoint;

@EntryPoint
public class CDITestModule {
    private static int busReadyEventsReceived = 0;
    private static CDITestModule instance;
    
	@Inject
	private Event<String> stringEvent;

	@PostConstruct
	public void doPostConstruct() {
		instance = this;
	}

	public static CDITestModule getInstance() {
        return instance;
    }
	
	public static int getBusReadyEventsReceived() {
	    return busReadyEventsReceived;
	}
	
	public Event<String> getStringEvent() {
	    return stringEvent;
	}
	
	public void onBusReady(@Observes BusReadyEvent event) {
		System.out.println("Bus ready");
		busReadyEventsReceived++;
		stringEvent.fire("Hello World");
	}
}