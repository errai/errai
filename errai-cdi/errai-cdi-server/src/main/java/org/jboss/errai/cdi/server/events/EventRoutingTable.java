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

package org.jboss.errai.cdi.server.events;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.api.laundry.Laundry;
import org.jboss.errai.bus.client.api.laundry.LaundryList;
import org.jboss.errai.bus.client.api.laundry.LaundryListProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <a href="http://www.youtube.com/watch?v=qBXn9PptgN8">Architectural Overview</a>
 *
 * @author Mike Brock
 */
public class EventRoutingTable {
  private static final String CDI_EVENT_ROUTES_ACTIVE = "cdi.event.routesActive";

  // type to (set<annotations> to set<session ids>)
  private final Map<String, Map<Set<String>, Set<String>>> activeRoutes
      = new ConcurrentHashMap<String, Map<Set<String>, Set<String>>>();

  private final Object routeChangeLock = new Object();
  private final Object sessionChangeLock = new Object();

  public void activateRoute(final String eventType,
                            final Set<String> annotations,
                            final QueueSession queueSession) {


    Map<Set<String>, Set<String>> route = activeRoutes.get(eventType);
    if (route == null) {
      synchronized (routeChangeLock) {
        route = activeRoutes.get(eventType);
        if (route == null) {
          activeRoutes.put(eventType, route = new ConcurrentHashMap<Set<String>, Set<String>>());
        }
      }
    }

    Set<String> sessions = route.get(annotations);

    if (sessions == null) {
      synchronized (sessionChangeLock) {
        sessions = route.get(annotations);
        if (sessions == null) {
          route.put(annotations, sessions =
              Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>()));
        }
      }
    }

    sessions.add(queueSession.getSessionId());

    updateLaundry(queueSession);
  }

  public void deactivateRoute(final String eventType,
                              final Set<String> annotations,
                              final QueueSession queueSession) {

    final Map<Set<String>, Set<String>> route = activeRoutes.get(eventType);
    if (route == null) {
      return;
    }

    final Set<String> sessions = route.get(annotations);
    if (sessions == null) {
      for (final Map.Entry<Set<String>, Set<String>> entry : activeRoutes.get(eventType).entrySet()) {
        if (annotations.containsAll(entry.getKey())) {
          entry.getValue().remove(queueSession.getSessionId());
        }
      }
    }
    else {
      sessions.remove(queueSession.getSessionId());
    }
  }

  public boolean isRouteActive(final String eventType,
                               final Set<String> annotations,
                               final QueueSession queueSession) {
    final Map<Set<String>, Set<String>> route = activeRoutes.get(eventType);
    if (route == null) {
      return false;
    }
    final Set<String> sessions = route.get(annotations);
    boolean active = sessions != null && sessions.contains(queueSession.getSessionId());
    return active;
  }

  public Collection<String> getQueueIdsForRoute(final String eventType,
                                                final Set<String> annotations) {

    final Map<Set<String>, Set<String>> route = activeRoutes.get(eventType);
    if (route == null) {
      return Collections.emptySet();
    }

    final Set<String> sessions = route.get(annotations);
    if (sessions == null) {
      final Set<String> ids = new HashSet<String>();
      for (final Map.Entry<Set<String>, Set<String>> entry : activeRoutes.get(eventType).entrySet()) {
        if (annotations.containsAll(entry.getKey())) {
          ids.addAll(entry.getValue());
        }
      }

      synchronized (sessionChangeLock) {
        final Map<Set<String>, Set<String>> routeMap = new ConcurrentHashMap<Set<String>, Set<String>>();
        routeMap.put(annotations, ids);
        activeRoutes.put(eventType, routeMap);
      }

      return ids;
    }

    return sessions;
  }

  private void removeAllForId(final String id) {
    synchronized (sessionChangeLock) {
      for (final Map<Set<String>, Set<String>> routeMaps : activeRoutes.values()) {
        for (final Map.Entry<Set<String>, Set<String>> entry : routeMaps.entrySet()) {
          entry.getValue().remove(id);
        }
      }
    }
  }

  private void updateLaundry(final QueueSession queueSession) {
    Boolean routesActive = queueSession.getAttribute(Boolean.class, CDI_EVENT_ROUTES_ACTIVE);
    if (routesActive == null) {
      synchronized (queueSession) {
        routesActive = queueSession.getAttribute(Boolean.class, CDI_EVENT_ROUTES_ACTIVE);
        if (routesActive == null) {
          final LaundryList laundryList = LaundryListProviderFactory.get().getLaundryList(queueSession);
          laundryList.add(new Laundry() {
            @Override
            public void clean() throws Exception {
              removeAllForId(queueSession.getSessionId());
            }
          });
        }
      }
    }
  }
}
