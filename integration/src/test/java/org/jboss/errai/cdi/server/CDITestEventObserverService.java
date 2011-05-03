package org.jboss.errai.cdi.server;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.cdi.client.ReceivedEvent;
import org.jboss.errai.cdi.client.qualifier.A;
import org.jboss.errai.cdi.client.qualifier.B;
import org.jboss.errai.cdi.client.qualifier.C;

@ApplicationScoped
public class CDITestEventObserverService {
    private static CDITestEventObserverService instance;
   
    @Inject
    // we use this event to report received event back to the client for easier testability
    private Event<ReceivedEvent> receivedEventEvent;
    
    @PostConstruct
    public void doPostConstruct() {
        instance = this;
    }

    public static CDITestEventObserverService getInstance() {
        return instance;
    }
    
    public void onEvent(@Observes String event) {
        receivedEventEvent.fire(new ReceivedEvent("", event));
    }
    
    public void onEventA(@Observes @A String event) {
        receivedEventEvent.fire(new ReceivedEvent("A", event));
    }

    public void onEventB(@Observes @B String event) {
        receivedEventEvent.fire(new ReceivedEvent("B", event));
    }

    public void onEventC(@Observes @C String event) {
        receivedEventEvent.fire(new ReceivedEvent("C", event));
    }
    
    public void onEventAB(@Observes @A @B String event) {
        receivedEventEvent.fire(new ReceivedEvent("AB", event));
    }
    
    public void onEventBA(@Observes @B @A String event) {
        receivedEventEvent.fire(new ReceivedEvent("BA", event));
    }
    
    public void onEventAC(@Observes @A @C String event) {
        receivedEventEvent.fire(new ReceivedEvent("AC", event));
    }
    
    public void onEventBC(@Observes @B @C String event) {
        receivedEventEvent.fire(new ReceivedEvent("BC", event));
    }
    
    public void onEventABC(@Observes @A @B @C String event) {
        receivedEventEvent.fire(new ReceivedEvent("ABC", event));
    }
}