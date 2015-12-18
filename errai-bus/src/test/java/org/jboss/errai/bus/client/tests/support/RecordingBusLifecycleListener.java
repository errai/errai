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

package org.jboss.errai.bus.client.tests.support;

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.bus.client.api.BusLifecycleEvent;
import org.jboss.errai.bus.client.api.BusLifecycleListener;

/**
 * General purpose testing utility that records all of the bus lifecycle events it receives.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
public class RecordingBusLifecycleListener implements BusLifecycleListener {

  public enum EventType { ASSOCIATING, DISASSOCIATING, ONLINE, OFFLINE }

  public static class RecordedEvent {
    private final EventType type;
    private final BusLifecycleEvent event;

    public RecordedEvent(EventType type, BusLifecycleEvent event) {
      super();
      this.type = type;
      this.event = event;
    }

    public BusLifecycleEvent getEvent() {
      return event;
    }

    public EventType getType() {
      return type;
    }
  }

  private final List<RecordedEvent> events = new ArrayList<RecordedEvent>();

  @Override
  public void busAssociating(BusLifecycleEvent e) {
    events.add(new RecordedEvent(EventType.ASSOCIATING, e));
  }

  @Override
  public void busDisassociating(BusLifecycleEvent e) {
    events.add(new RecordedEvent(EventType.DISASSOCIATING, e));
  }

  @Override
  public void busOnline(BusLifecycleEvent e) {
    events.add(new RecordedEvent(EventType.ONLINE, e));
  }

  @Override
  public void busOffline(BusLifecycleEvent e) {
    events.add(new RecordedEvent(EventType.OFFLINE, e));
  }

  public List<RecordedEvent> getEvents() {
    return events;
  }

  /**
   * Returns the types of events fired in the order they were received in.
   */
  public List<EventType> getEventTypes() {
    List<EventType> types = new ArrayList<EventType>();
    for (RecordedEvent re : events) {
      types.add(re.getType());
    }
    return types;
  }
}
