/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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
import java.util.List;

import javax.enterprise.event.Observes;

import org.jboss.errai.cdi.client.event.MyAbstractEvent;
import org.jboss.errai.cdi.client.event.MyAbstractEventInterface;
import org.jboss.errai.cdi.client.event.MyEventImpl;
import org.jboss.errai.cdi.client.event.MyEventInterface;

/**
 * This class serves a dual-purpose for now: to test whether @Observes works in abstract super classes 
 * and to check if super types of event types can be observed.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public abstract class EventTestObserverSuperClass {
  private List<String> receivedSuperTypeEvents = new ArrayList<String>();

  public List<String> getReceivedSuperTypeEvents() {
    return receivedSuperTypeEvents;
  }

  public void observeInterface(@Observes Cloneable e) {
    // should never fire
    receivedSuperTypeEvents.add(Comparable.class.getName());
  }

  public void observeInterface(@Observes MyEventInterface e) {
    receivedSuperTypeEvents.add(MyEventInterface.class.getName());
  }
  
  public void observeSuperTypeInterface(@Observes MyAbstractEventInterface e) {
    receivedSuperTypeEvents.add(MyAbstractEventInterface.class.getName());
  }

  public void observeSuperType(@Observes MyAbstractEvent e) {
    receivedSuperTypeEvents.add(MyAbstractEvent.class.getName());
  }

  public void observeImplementation(@Observes MyEventImpl e) {
    receivedSuperTypeEvents.add(MyEventImpl.class.getName());
  }
}
