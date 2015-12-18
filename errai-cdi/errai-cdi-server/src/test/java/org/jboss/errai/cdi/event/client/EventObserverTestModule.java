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
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.jboss.errai.cdi.client.event.FinishEvent;
import org.jboss.errai.cdi.client.event.StartEvent;
import org.jboss.errai.cdi.client.qualifier.A;
import org.jboss.errai.cdi.client.qualifier.B;
import org.jboss.errai.cdi.client.qualifier.C;
import org.jboss.errai.enterprise.client.cdi.events.BusReadyEvent;
import org.jboss.errai.ioc.client.api.EntryPoint;

/**
 * Test module used by {@see EventObserverIntegrationTest}.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@EntryPoint
public class EventObserverTestModule extends EventTestObserverSuperClass {
  private final Map<String, List<String>> receivedQualifiedEvents = new HashMap<String, List<String>>();

  private int busReadyEventsReceived = 0;
  private int startEventsReceived = 0;
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
  
  public int getStartEventsReceived() {
    return startEventsReceived;
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
   * count the {@link BusReadyEvent}
   */
  public void onBusReady(@Observes BusReadyEvent event) {
    busReadyEventsReceived++;
  }

  /**
   * start the event producers on the server
   */
  public void start() {
    System.out.println("Firing StartEvent");
    startEventsReceived = 0;
    startEvent.fire(new StartEvent());
  }

//  private void onObject(@Observes Object foo) {
//    
//  }
  
  private void onStart(@Observes StartEvent event) {
    startEventsReceived++;
    if (startEventsReceived > 1) {
      throw new RuntimeException("Received too many start events (did the server mirror it back?)");
    }
  }
  
  private void onEvent(@Observes String event) {
    System.out.println("Observed unqualified");
    addQualifiedReceivedEvent("", event);
  }

  // This serves as a regression test for ERRAI-680. It should be possible to have 2 private
  // observer methods with the same name.
  private void onEvent(@Observes Integer event) {
  }
  
  @SuppressWarnings("unused")
  private void onEventAny(@Observes @Any String event) {
    System.out.println("Observed @Any");
    addQualifiedReceivedEvent("Any", event);
  }

  public void onEventA(@Observes @A String event) {
    System.out.println("Observed @A");
    addQualifiedReceivedEvent("A", event);
  }

  public void onEventB(@Observes @B String event) {
    System.out.println("Observed @B");
    addQualifiedReceivedEvent("B", event);
  }

  public void onEventC(@Observes @C String event) {
    System.out.println("Observed @C");
    addQualifiedReceivedEvent("C", event);
  }

  public void onEventAB(@Observes @A @B String event) {
    System.out.println("Observed @A @B");
    addQualifiedReceivedEvent("AB", event);
  }

  public void onEventBA(@Observes @B @A String event) {
    System.out.println("Observed @B @A");
    addQualifiedReceivedEvent("BA", event);
  }

  public void onEventAC(@Observes @A @C String event) {
    System.out.println("Observed @A @C");
    addQualifiedReceivedEvent("AC", event);
  }

  public void onEventBC(@Observes @B @C String event) {
    System.out.println("Observed @B @C");
    addQualifiedReceivedEvent("BC", event);
  }

  public void onEventABC(@Observes @A @B @C String event) {
    System.out.println("Observed @A @B @C");
    addQualifiedReceivedEvent("ABC", event);
  }

  public void onFinish(@Observes FinishEvent event) {
    System.out.println("Observed FinishEvent");

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
    events.add(event);
    receivedQualifiedEvents.put(receiver, events);
  }
}
