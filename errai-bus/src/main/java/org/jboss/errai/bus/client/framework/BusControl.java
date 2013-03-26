/*
 * Copyright 2013 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.bus.client.framework;

/**
 * An interface which exposes the ability to disconnect or reconnect the client message bus. Usually in response
 * to error handling.
 *
 * @deprecated Use {@link ClientMessageBus#stop(boolean)} and
 *             {@link ClientMessageBus#init()} to stop and start the bus.
 * @author Mike Brock
 */
@Deprecated
public interface BusControl {
  /**
   * Immediately disconnects the bus from the server.
   */
  public void disconnect();

  /**
   * Reconnects the bus to the server. If the bus is already connected, calling this disconnects the bus and
   * negotiates a new federation. If the bus is not connected, it attempts to initiate a new connection.
   */
  public void reconnect();
}
