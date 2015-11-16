package org.jboss.errai.cdi.event.server;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.cdi.client.event.FunEvent;
import org.jboss.errai.cdi.client.event.FunFinishEvent;
import org.jboss.errai.cdi.client.event.FunStartEvent;
import org.jboss.errai.cdi.client.qualifier.A;
import org.jboss.errai.cdi.client.qualifier.B;

@ApplicationScoped
public class CdiEventRoutingTestService {

  @Inject
  private Event<FunEvent> event;

  @Inject @A
  private Event<FunEvent> eventA;

  @Inject @B
  private Event<FunEvent> eventB;

  @Inject
  private Event<FunFinishEvent> finishEvent;

  public void start(@Observes FunStartEvent event) {
    fireAll();
  }

  public void fireAll() {
    event.fire(new FunEvent(""));
    eventA.fire(new FunEvent("A"));
    eventB.fire(new FunEvent("B"));
    finishEvent.fire(new FunFinishEvent());
  }
}