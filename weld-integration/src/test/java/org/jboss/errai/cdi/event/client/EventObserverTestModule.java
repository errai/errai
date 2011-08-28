package org.jboss.errai.cdi.event.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.enterprise.client.cdi.events.BusReadyEvent;
import org.jboss.errai.cdi.client.qualifier.A;
import org.jboss.errai.cdi.client.qualifier.B;
import org.jboss.errai.cdi.client.qualifier.C;
import org.jboss.errai.ioc.client.api.EntryPoint;

/**
 * Test module used by {@see EventObserverIntegrationTest}.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@EntryPoint
public class EventObserverTestModule {
  private int busReadyEventsReceived = 0;
  private static EventObserverTestModule instance;
  private Map<String, List<String>> receivedEvents = new HashMap<String, List<String>>();

  private Runnable callback;
  private int callbackOnCount;

  @Inject
  private Event<StartEvent> startEvent;

  @PostConstruct
  public void doPostConstruct() {
    instance = this;
  }

  public static EventObserverTestModule getInstance() {
    return instance;
  }

  public int getBusReadyEventsReceived() {
    return busReadyEventsReceived;
  }

  public Map<String, List<String>> getReceivedEvents() {
    return receivedEvents;
  }

  public Event<StartEvent> getStartEvent() {
    return startEvent;
  }

  /**
   * count the {@link BusReadyEvent}
   */
  public void onBusReady(@Observes BusReadyEvent event) {
    busReadyEventsReceived++;
  }

  /**
   * start the event producers on the server
   */
  public void start() {
    startEvent.fire(new StartEvent());
  }

  // all the observer methods

  public void onEvent(@Observes String event) {
    addReceivedEvent("", event);
  }

  public void onEventA(@Observes @A String event) {
    addReceivedEvent("A", event);
  }

  public void onEventB(@Observes @B String event) {
    addReceivedEvent("B", event);
  }

  public void onEventC(@Observes @C String event) {
    addReceivedEvent("C", event);
  }

  public void onEventAB(@Observes @A @B String event) {
    addReceivedEvent("AB", event);
  }

  public void onEventBA(@Observes @B @A String event) {
    addReceivedEvent("BA", event);
  }

  public void onEventAC(@Observes @A @C String event) {
    addReceivedEvent("AC", event);
  }

  public void onEventBC(@Observes @B @C String event) {
    addReceivedEvent("BC", event);
  }

  public void onEventABC(@Observes @A @B @C String event) {
    addReceivedEvent("ABC", event);
  }

  private void addReceivedEvent(String receiver, String event) {
    List<String> events = receivedEvents.get(receiver);
    if (events == null)
      events = new ArrayList<String>();
    events.add(event);
    receivedEvents.put(receiver, events);

    if (receivedEvents.size() == callbackOnCount) {
      callback.run();
    }
  }

  public void registerCallback(int onCount, Runnable runnable) {
    callbackOnCount = onCount;
    callback = runnable;
  }
}