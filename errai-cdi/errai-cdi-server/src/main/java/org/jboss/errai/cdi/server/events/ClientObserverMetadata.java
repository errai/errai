/*
 * Copyright (C) 2014 Red Hat, Inc. and/or its affiliates.
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

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.EventMetadata;

import org.jboss.errai.enterprise.client.cdi.api.CDI;

/**
 * Represents an active client-side event observer (an observer that at least
 * one client currently listens to).
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ClientObserverMetadata {
  private final Class<?> eventType;
  private final Set<String> qualifiers;

  public ClientObserverMetadata(final Class<?> eventType, final Set<String> qualifiers) throws ClassNotFoundException {
    this.eventType = eventType;
    this.qualifiers = qualifiers;
  }

  public Class<?> getEventType() {
    return eventType;
  }

  public Set<String> getQualifiers() {
    return qualifiers;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ClientObserverMetadata other = (ClientObserverMetadata) obj;
    if (eventType == null) {
      if (other.eventType != null)
        return false;
    }
    else if (!eventType.equals(other.eventType))
      return false;
    if (qualifiers == null) {
      if (other.qualifiers != null)
        return false;
    }
    else if (!qualifiers.equals(other.qualifiers))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((eventType == null) ? 0 : eventType.hashCode());
    result = prime * result + ((qualifiers == null) ? 0 : qualifiers.hashCode());
    return result;
  }

  public boolean matches(Object event, EventMetadata emd) {
    Class<?> actualEventType = event.getClass();
    Set<String> actualQualifiers = (emd != null) ? CDI
            .getQualifiersPart(emd.getQualifiers().toArray(new Annotation[0])) : Collections.<String> emptySet();

    // The clients subscribe to every supertype and interface type separately
    // which is why checking for equals is enough here. Otherwise, we would have
    // to check for eventType.isAssignableFrom(actualEventType).
    boolean typeMatches = eventType.equals(actualEventType);
    boolean qualifiersMatch = qualifiers.isEmpty() || qualifiers.contains(Any.class.getName())
            || actualQualifiers.containsAll(this.qualifiers);

    return typeMatches && qualifiersMatch;
  }

  @Override
  public String toString() {
    return "ClientEventObserver [eventType=" + eventType + ", qualifiers=" + qualifiers + "]";
  }
}
