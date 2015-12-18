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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.jboss.errai.cdi.client.event.DataBoundEvent;
import org.jboss.errai.cdi.client.event.LocalEventA;
import org.jboss.errai.cdi.client.qualifier.A;
import org.jboss.errai.cdi.client.qualifier.B;

/**
 * @author Mike Brock
 */
@ApplicationScoped
public class DisconnectedEventTestModule {
  @Inject private Event<LocalEventA> localEventAEvent;
  @Inject private Event<DataBoundEvent> dataBoundEvent;
  @Inject @A private Event<LocalEventA> localEventAEventQualifiers;

  private final List<LocalEventA> capturedEvents = new ArrayList<LocalEventA>();
  private DataBoundEvent capturedDataBoundEvent;

  private void observesLocalEventA(@Observes final LocalEventA localEventA) {
    capturedEvents.add(new LocalEventA(localEventA.getMessage() + ":None"));
  }

  private void observesAnyLocalEvent(@Observes @Any final LocalEventA localEventA) {
    capturedEvents.add(new LocalEventA(localEventA.getMessage() + ":Any"));
  }

  private void observesLocalEventWithQualifiers(@Observes @A final LocalEventA localEventA) {
    capturedEvents.add(new LocalEventA(localEventA.getMessage() + ":A"));
  }

  private void observesLocalEventWithQualifiersB(@Observes @A @B final LocalEventA localEventA) {
    capturedEvents.add(new LocalEventA(localEventA.getMessage() + ":AB"));
  }
  
  private void observesDataBoundEvent(@Observes final DataBoundEvent dataBoundEvent) {
    capturedDataBoundEvent = dataBoundEvent;
  }

  public List<LocalEventA> getCapturedEvents() {
    return capturedEvents;
  }
  
  public DataBoundEvent getCapturedDataBoundEvent() {
    return capturedDataBoundEvent;
  }

  public void fireEvent(final String eventText) {
    localEventAEvent.fire(new LocalEventA(eventText));
  }

  public void fireQualified(final String eventText) {
     localEventAEventQualifiers.fire(new LocalEventA(eventText));
  }

  public void fireQualifiedWithExtraQualifiers(final String eventText) {
    localEventAEventQualifiers.select(new B() {
      @Override
      public Class<? extends Annotation> annotationType() {
        return B.class;
      }
    }).fire(new LocalEventA(eventText));
  }
  
  public void fireDataBoundEvent(DataBoundEvent event) {
    dataBoundEvent.fire(event);
 }
}
