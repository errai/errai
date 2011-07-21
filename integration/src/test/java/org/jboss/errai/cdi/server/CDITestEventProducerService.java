package org.jboss.errai.cdi.server;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.cdi.client.api.Conversational;
import org.jboss.errai.cdi.client.qualifier.A;
import org.jboss.errai.cdi.client.qualifier.B;
import org.jboss.errai.cdi.client.qualifier.C;
import org.jboss.errai.cdi.event.client.StartEvent;

@ApplicationScoped
public class CDITestEventProducerService {

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

  @Conversational
  public void start(@Observes StartEvent event) {
    fireAll();
  }

  public void fireAll() {
    fire();
    fireA();
    fireB();
    fireC();
    fireAB();
    fireAC();
    fireBC();
    fireABC();
  }

  public void fire() {
    event.fire("");
  }

  public void fireA() {
    eventA.fire("A");
  }

  public void fireB() {
    eventB.fire("B");
  }

  public void fireC() {
    eventC.fire("C");
  }

  public void fireAB() {
    eventAB.fire("AB");
  }

  public void fireBC() {
    eventBC.fire("BC");
  }

  public void fireAC() {
    eventAC.fire("AC");
  }

  public void fireABC() {
    eventABC.fire("ABC");
  }
}
