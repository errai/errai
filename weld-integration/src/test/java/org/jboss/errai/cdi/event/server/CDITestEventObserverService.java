package org.jboss.errai.cdi.event.server;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.cdi.client.qualifier.A;
import org.jboss.errai.cdi.client.qualifier.B;
import org.jboss.errai.cdi.client.qualifier.C;
import org.jboss.errai.cdi.event.client.ReceivedEvent;

@ApplicationScoped
public class CDITestEventObserverService {
  private static CDITestEventObserverService instance;

  @Inject
  // we use this event to report received events back to the client for easier testability
  private Event<ReceivedEvent> receivedEvent;

  @PostConstruct
  public void doPostConstruct() {
    instance = this;
  }

  public static CDITestEventObserverService getInstance() {
    return instance;
  }

  public void onEvent(@Observes String event) {
    System.out.println("server:onEvent()");
    receivedEvent.fire(new ReceivedEvent("", event));
  }

  public void onEventA(@Observes @A String event) {
    System.out.println("server:onEventA():A");
    receivedEvent.fire(new ReceivedEvent("A", event));
  }

  public void onEventB(@Observes @B String event) {
    System.out.println("server:onEventB():B");
    receivedEvent.fire(new ReceivedEvent("B", event));
  }

  public void onEventC(@Observes @C String event) {
    System.out.println("server:onEventC():C");
    receivedEvent.fire(new ReceivedEvent("C", event));
  }

  public void onEventAB(@Observes @A @B String event) {
    System.out.println("server:onEventAB():AB");
    receivedEvent.fire(new ReceivedEvent("AB", event));
  }

  public void onEventBA(@Observes @B @A String event) {
    System.out.println("server:onEventBA():BA");
    receivedEvent.fire(new ReceivedEvent("BA", event));
  }

  public void onEventAC(@Observes @A @C String event) {
    System.out.println("server:onEventAC():AC");
    receivedEvent.fire(new ReceivedEvent("AC", event));
  }

  public void onEventBC(@Observes @B @C String event) {
    System.out.println("server:onEventBC():BC");
    receivedEvent.fire(new ReceivedEvent("BC", event));
  }

  public void onEventABC(@Observes @A @B @C String event) {
    System.out.println("server:onEventABC():ABC");
    receivedEvent.fire(new ReceivedEvent("ABC", event));
  }
}