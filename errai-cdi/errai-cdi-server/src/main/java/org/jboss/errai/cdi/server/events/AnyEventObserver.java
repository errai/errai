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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.EventMetadata;

import org.jboss.errai.config.marshalling.MarshallingConfiguration;

/**
 * Managed bean that observes all server-side events and dispatches them to the
 * connected clients using the {@link EventDispatcher}.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@ApplicationScoped
public class AnyEventObserver {

  private static EventDispatcher eventDispatcher;

  public static void init(EventDispatcher dispatcher) {
    eventDispatcher = dispatcher;
  }

  @SuppressWarnings("unused")
  private void onEvent(@Observes Object event, EventMetadata emd) {
    // Check if initialized
    if (eventDispatcher == null)
      return;

    // Check if the event is a portable Errai CDI event and should be forwarded
    // to all listening clients
    if (MarshallingConfiguration.isPortableType(event.getClass())) {
      eventDispatcher.sendEventToClients(event, emd);
    }

  }
}
