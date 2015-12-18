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

package org.jboss.errai.bus.client.api;

import org.jboss.errai.common.client.api.Assert;

/**
 * Represents the event that happens during a state transition in the
 * {@link ClientMessageBus} lifecycle. Provides access to contextual information
 * at the time of the event.
 *
 * @see BusLifecycleListener
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public final class BusLifecycleEvent {

  private final ClientMessageBus bus;

  private final TransportError reason;

  /**
   * Creates a new lifecycle event with the given details.
   *
   * @param bus
   *          The bus that this lifecycle event pertains to. Must not be null.
   * @param reason
   *          The error that caused this lifecycle transition. Null is
   *          permitted, and means the transition was not caused by a transport
   *          error.
   */
  public BusLifecycleEvent(ClientMessageBus bus, TransportError reason) {
    this.bus = Assert.notNull(bus);
    this.reason = reason;
  }

  /**
   * Returns the bus that this lifecycle event pertains to.
   *
   * @return The bus that this lifecycle event pertains to. Never null.
   */
  public ClientMessageBus getBus() {
    return bus;
  }

  /**
   * The transport error that caused this lifecycle transition.
   *
   * @return The transport error that caused this lifecycle transition. Returns
   *         null if the transition was not caused by a transport error.
   */
  public TransportError getReason() {
    return reason;
  }
}
