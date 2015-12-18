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

package org.jboss.errai.jpa.test.entity;

public class CallbackLogEntry {

  private final Object eventReceiver;
  private final Class<?> eventType;

  public CallbackLogEntry(Object eventReceiver, Class<?> eventType) {
    this.eventReceiver = eventReceiver;
    this.eventType = eventType;
  }

  public Object getEventReceiver() {
    return eventReceiver;
  }

  public Class<?> getEventType() {
    return eventType;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
            + ((eventReceiver == null) ? 0 : eventReceiver.hashCode());
    result = prime * result + ((eventType == null) ? 0 : eventType.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    CallbackLogEntry other = (CallbackLogEntry) obj;
    if (eventReceiver == null) {
      if (other.eventReceiver != null)
        return false;
    }
    else if (!eventReceiver.equals(other.eventReceiver))
      return false;
    if (eventType == null) {
      if (other.eventType != null)
        return false;
    }
    else if (!eventType.equals(other.eventType))
      return false;
    return true;
  }

  @Override
  public String toString() {
    String receiverClassName = eventReceiver.getClass().getName();
    int receiverIdentity = System.identityHashCode(eventReceiver instanceof StandaloneLifecycleListener ?
            ((StandaloneLifecycleListener) eventReceiver).getEventSubject() : eventReceiver);
    String eventClassName = eventType.getName();

    return receiverClassName.substring(receiverClassName.lastIndexOf('.') + 1)
            + "@" + receiverIdentity
            + ":" + eventClassName.substring(eventClassName.lastIndexOf('.') + 1);
  }
}
