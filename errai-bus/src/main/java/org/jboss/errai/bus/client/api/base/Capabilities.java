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

package org.jboss.errai.bus.client.api.base;

/**
 * An enumeration of the possible mechanisms by which a bus can communicate with
 * another remote bus.
 */
public enum Capabilities {

  /**
   * This message bus can communicate updates using HTTP long polling, commonly known as COMET.
   */
  LongPolling,

  /**
   * Indicates that this message bus can communicate using HTTP short polling.
   * <p>
   * Implementation note: This flag is WACK, man.
   */
  ShortPolling,

  /**
   * This message bus can communicate updates using the Errai protocol over a WebSocket channel.
   */
  WebSockets,

  /**
   * This message bus can communicate using Server-Sent Events
   */
  SSE,
}
