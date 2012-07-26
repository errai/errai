package org.jboss.errai.cdi.event.client;

import org.jboss.errai.cdi.client.event.FunFinishEvent;
import org.jboss.errai.cdi.client.event.FunStartEvent;
import org.jboss.errai.ioc.client.api.EntryPoint;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * Test module used by {@see EventObserverIntegrationTest}.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@EntryPoint
public class EventRoutingTestModule extends EventTestObserverSuperClass {
  private Runnable verifier;

  @Inject
  private Event<FunStartEvent> startEvent;

  /**
   * start the event producers on the server
   */
  public void start() {
    startEvent.fire(new FunStartEvent());
  }

  public void onFinish(@Observes FunFinishEvent event) {
    if (verifier != null) {
      verifier.run();
    }
  }

  public void setResultVerifier(Runnable verifier) {
    this.verifier = verifier;
  }

}