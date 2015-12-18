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

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.cdi.client.event.FinishEvent;
import org.jboss.errai.cdi.client.event.ReceivedEvent;
import org.jboss.errai.cdi.client.qualifier.A;
import org.jboss.errai.cdi.client.qualifier.B;
import org.jboss.errai.cdi.client.qualifier.C;
import org.jboss.errai.enterprise.client.cdi.events.BusReadyEvent;
import org.jboss.errai.ioc.client.api.EntryPoint;

/**
 * Test module used by {@see EventProducerIntegrationTest}.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@EntryPoint
public class EventProducerTestModule {
  private boolean busReadyEventReceived = false;
  private Runnable verifier;

  private Map<String, List<String>> receivedEventsOnServer = new HashMap<String, List<String>>();

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
  private Event<FinishEvent> finishEvent;

  public boolean getBusReadyEventsReceived() {
    return busReadyEventReceived;
  }

  /**
   * count the {@link BusReadyEvent}
   */
  public void onBusReady(@Observes BusReadyEvent event) {
    busReadyEventReceived = true;
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
    fireFinishEvent();
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

  public void fireFinishEvent() {
    finishEvent.fire(new FinishEvent());
  }

  public Event<String> getEvent() {
    return event;
  }

  public Event<String> getEventA() {
    return eventA;
  }

  public Event<String> getEventB() {
    return eventB;
  }

  public Event<String> getEventC() {
    return eventC;
  }

  public Event<String> getEventAB() {
    return eventAB;
  }

  public Event<String> getEventBC() {
    return eventBC;
  }

  public Event<String> getEventAC() {
    return eventAC;
  }

  public Event<String> getEventABC() {
    return eventABC;
  }

  public void collectResults(@Observes ReceivedEvent event) {
    if (event.getEvent().equals("FINISH")) {
      if (verifier != null) {
        verifier.run();
      }
    }

    List<String> events = receivedEventsOnServer.get(event.getReceiver());
    if (events == null)
      events = new ArrayList<String>();

    if (events.contains(event))
      throw new RuntimeException(event.getReceiver() + " received " + event.getEvent() + " twice!");

    events.add(event.getEvent());
    receivedEventsOnServer.put(event.getReceiver(), events);
  }

  public Map<String, List<String>> getReceivedEventsOnServer() {
    return receivedEventsOnServer;
  }

  public void setResultVerifier(Runnable verifier) {
    this.verifier = verifier;
  }
}
