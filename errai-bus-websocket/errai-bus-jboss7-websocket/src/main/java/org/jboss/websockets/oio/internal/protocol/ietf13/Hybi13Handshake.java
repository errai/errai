/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.websockets.oio.internal.protocol.ietf13;

import org.jboss.websockets.oio.ClosingStrategy;
import org.jboss.websockets.oio.HttpRequestBridge;
import org.jboss.websockets.oio.HttpResponseBridge;
import org.jboss.websockets.oio.OioWebSocket;
import org.jboss.websockets.oio.internal.WebSocketHeaders;
import org.jboss.websockets.oio.internal.protocol.ietf07.Hybi07Handshake;

import java.io.IOException;

import static org.jboss.websockets.oio.internal.WebSocketHeaders.ORIGIN;
import static org.jboss.websockets.oio.internal.WebSocketHeaders.SEC_WEBSOCKET_KEY;
import static org.jboss.websockets.oio.internal.WebSocketHeaders.SEC_WEBSOCKET_LOCATION;
import static org.jboss.websockets.oio.internal.WebSocketHeaders.SEC_WEBSOCKET_PROTOCOL;

/**
 * The handshaking protocol implementation for Hybi-13.
 *
 * @author Mike Brock
 */
public class Hybi13Handshake extends Hybi07Handshake {
  public Hybi13Handshake() {
    super("13");
  }

  @Override
  public OioWebSocket getWebSocket(final HttpRequestBridge request,
                                   final HttpResponseBridge response,
                                   final ClosingStrategy closingStrategy) throws IOException {
    return Hybi13Socket.from(request, response, closingStrategy);
  }

  @Override
  public byte[] generateResponse(final HttpRequestBridge request,
                                 final HttpResponseBridge response) throws IOException {

    ORIGIN.copy(request, response);

    SEC_WEBSOCKET_PROTOCOL.copy(request, response);

    SEC_WEBSOCKET_LOCATION.set(response, getWebSocketLocation(request));

    final String key = SEC_WEBSOCKET_KEY.get(request);
    final String solution = solve(key);

    WebSocketHeaders.SEC_WEBSOCKET_ACCEPT.set(response, solution);

    return new byte[0];
  }
}
