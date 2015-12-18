/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.cdi.event.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.cdi.client.event.FinishEvent;
import org.jboss.errai.cdi.client.event.StartEvent;
import org.jboss.errai.cdi.client.qualifier.A;
import org.jboss.errai.cdi.client.qualifier.B;
import org.jboss.errai.cdi.client.qualifier.C;
import org.jboss.errai.enterprise.client.cdi.events.BusReadyEvent;

@Dependent
public class DependentEventObserverTestModule extends EventTestObserverSuperClass {
  private final Map<String, List<String>> receivedQualifiedEvents = new HashMap<String, List<String>>();

  private int busReadyEventsReceived = 0;
  private Runnable verifier;
  private boolean destroyed;

  @Inject
  private Event<StartEvent> startEvent;

  @PreDestroy
  private void destroy() {
    destroyed = true;
  }

  public int getBusReadyEventsReceived() {
    return busReadyEventsReceived;
  }

  public Map<String, List<String>> getReceivedQualifiedEvents() {
    return receivedQualifiedEvents;
  }

  public Event<StartEvent> getStartEvent() {
    return startEvent;
  }

  public boolean isDestroyed() {
    return destroyed;
  }

  /**
   * count the {@link org.jboss.errai.enterprise.client.cdi.events.BusReadyEvent}
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

  @SuppressWarnings("unused")
  private void onEvent(@Observes String event) {
    addQualifiedReceivedEvent("", event);
  }
  
  public void onEventA(@Observes @A String event) {
    addQualifiedReceivedEvent("A", event);
  }

  public void onEventB(@Observes @B String event) {
    addQualifiedReceivedEvent("B", event);
  }

  public void onEventC(@Observes @C String event) {
    addQualifiedReceivedEvent("C", event);
  }

  public void onEventAB(@Observes @A @B String event) {
    addQualifiedReceivedEvent("AB", event);
  }

  public void onEventBA(@Observes @B @A String event) {
    addQualifiedReceivedEvent("BA", event);
  }

  public void onEventAC(@Observes @A @C String event) {
    addQualifiedReceivedEvent("AC", event);
  }

  public void onEventBC(@Observes @B @C String event) {
    addQualifiedReceivedEvent("BC", event);
  }

  public void onEventABC(@Observes @A @B @C String event) {
    addQualifiedReceivedEvent("ABC", event);
  }

  public void onFinish(@Observes FinishEvent event) {
    if (verifier != null) {
      verifier.run();
    }
  }

  public void setResultVerifier(Runnable verifier) {
    this.verifier = verifier;
  }
  
  private void addQualifiedReceivedEvent(String receiver, String event) {
    List<String> events = receivedQualifiedEvents.get(receiver);
    if (events == null)
      events = new ArrayList<String>();

    if (events.contains(event))
      throw new RuntimeException(receiver + " received " + event + " twice!");

    events.add(event);
    receivedQualifiedEvents.put(receiver, events);
  }
}
