package org.jboss.errai.cdi.event.server;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.cdi.client.event.FinishEvent;
import org.jboss.errai.cdi.client.event.MyEventImpl;
import org.jboss.errai.cdi.client.event.MyEventInterface;
import org.jboss.errai.cdi.client.event.StartEvent;
import org.jboss.errai.cdi.client.event.UnobservedEvent;
import org.jboss.errai.cdi.client.qualifier.A;
import org.jboss.errai.cdi.client.qualifier.B;
import org.jboss.errai.cdi.client.qualifier.C;

@ApplicationScoped
public class CdiTestEventProducerService {

  @Inject
  private Event<String> event;

  @Inject @A
  private Event<String> eventA;

  @Inject @B
  private Event<String> eventB;

  @Inject @C
  private Event<String> eventC;

  @Inject @A @B
  private Event<String> eventAB;

  @Inject @B @C
  private Event<String> eventBC;

  @Inject @A @C
  private Event<String> eventAC;

  @Inject @A @B @C
  private Event<String> eventABC;

  @Inject
  private Event<MyEventInterface> myEvent;

  @Inject
  private Event<UnobservedEvent> unobservedEvent;

  @Inject
  private Event<FinishEvent> finishEvent;

  public void start(@Observes StartEvent event) {
    System.out.println("Server Observed StartEvent");
    fireAll();
  }

  public void fireAll() {
    event.fire("");
    eventA.fire("A");
    eventB.fire("B");
    eventC.fire("C");
    eventAB.fire("AB");
    eventAC.fire("AC");
    eventBC.fire("BC");
    eventABC.fire("ABC");
    myEvent.fire(new MyEventImpl());
    unobservedEvent.fire(new UnobservedEvent(String.valueOf(System.currentTimeMillis())));
    finishEvent.fire(new FinishEvent());
  }
}