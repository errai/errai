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

package org.jboss.errai.cdi.server.events;

import org.jboss.errai.bus.client.api.messaging.MessageBus;

import java.util.Set;

/**
 * @author Mike Brock
 */
public class ConversationalEventWrapper {
  private Object eventObject;
  private Class<?> eventType;
  private Set<String> qualifierStrings;
  private MessageBus bus;

  public ConversationalEventWrapper(Object eventObject, Class<?> eventType, Set<String> qualifierStrings, MessageBus bus) {
    this.eventObject = eventObject;
    this.eventType = eventType;
    this.qualifierStrings = qualifierStrings;
    this.bus = bus;
  }

  public Object getEventObject() {
    return eventObject;
  }

  public Class<?> getEventType() {
    return eventType;
  }

  public Set<String> getQualifierStrings() {
    return qualifierStrings;
  }

  public MessageBus getBus() {
    return bus;
  }
}
