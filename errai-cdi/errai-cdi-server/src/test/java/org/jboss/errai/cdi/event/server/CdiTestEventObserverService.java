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

package org.jboss.errai.cdi.event.server;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.cdi.client.event.FinishEvent;
import org.jboss.errai.cdi.client.event.ReceivedEvent;
import org.jboss.errai.cdi.client.qualifier.A;
import org.jboss.errai.cdi.client.qualifier.B;
import org.jboss.errai.cdi.client.qualifier.C;

@ApplicationScoped
public class CdiTestEventObserverService {
  private static CdiTestEventObserverService instance;

  @Inject
  // we use this event to report received events back to the client for easier testability
  private Event<ReceivedEvent> receivedEvent;
  
  @PostConstruct
  public void doPostConstruct() {
    instance = this;
  }

  public static CdiTestEventObserverService getInstance() {
    return instance;
  }

  public void onEvent(@Observes String event) {
    receivedEvent.fire(new ReceivedEvent("", event));
  }

  public void onEventA(@Observes @A String event) {
    receivedEvent.fire(new ReceivedEvent("A", event));
  }

  public void onEventB(@Observes @B String event) {
    receivedEvent.fire(new ReceivedEvent("B", event));
  }

  public void onEventC(@Observes @C String event) {
    receivedEvent.fire(new ReceivedEvent("C", event));
  }

  public void onEventAB(@Observes @A @B String event) {
    receivedEvent.fire(new ReceivedEvent("AB", event));
  }

  public void onEventBA(@Observes @B @A String event) {
    receivedEvent.fire(new ReceivedEvent("BA", event));
  }

  public void onEventAC(@Observes @A @C String event) {
    receivedEvent.fire(new ReceivedEvent("AC", event));
  }

  public void onEventBC(@Observes @B @C String event) {
    receivedEvent.fire(new ReceivedEvent("BC", event));
  }

  public void onEventABC(@Observes @A @B @C String event) {
    receivedEvent.fire(new ReceivedEvent("ABC", event));
  }
  
  public void onFinish(@Observes FinishEvent event) {
    receivedEvent.fire(new ReceivedEvent("FINISH", "FINISH"));
  }
}
