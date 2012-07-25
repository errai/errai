package org.jboss.errai.cdi.event.client;

import org.jboss.errai.cdi.client.event.FunEvent;
import org.jboss.errai.cdi.client.event.FunFinishEvent;
import org.jboss.errai.cdi.client.event.FunStartEvent;
import org.jboss.errai.ioc.client.api.EntryPoint;

import javax.annotation.PreDestroy;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test module used by {@see EventObserverIntegrationTest}.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@EntryPoint
public class EventRoutingTestModule extends EventTestObserverSuperClass {
  private Map<String, List<FunEvent>> receivedQualifiedEvents = new HashMap<String, List<FunEvent>>();

  private int busReadyEventsReceived = 0;
  private Runnable verifier;
  private boolean destroyed;

  @Inject
  private Event<FunStartEvent> startEvent;

  @PreDestroy
  private void destroy() {
    destroyed = true;
  }

  public Map<String, List<FunEvent>> getReceivedQualifiedEvents() {
    return receivedQualifiedEvents;
  }

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
  
  public void addQualifiedReceivedEvent(String receiver, FunEvent event) {
    List<FunEvent> events = receivedQualifiedEvents.get(receiver);
    if (events == null)
      events = new ArrayList<FunEvent>();

    if (events.contains(event))
      throw new RuntimeException(receiver + " received " + event + " twice!");

    events.add(event);
    receivedQualifiedEvents.put(receiver, events);
  }
}