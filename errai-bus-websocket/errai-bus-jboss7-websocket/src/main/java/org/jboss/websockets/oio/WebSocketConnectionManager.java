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

package org.jboss.websockets.oio;

import org.jboss.websockets.oio.internal.Handshake;
import org.jboss.websockets.oio.internal.WebSocketHeaders;
import org.jboss.websockets.oio.internal.protocol.ietf00.Hybi00Handshake;
import org.jboss.websockets.oio.internal.protocol.ietf07.Hybi07Handshake;
import org.jboss.websockets.oio.internal.protocol.ietf08.Hybi08Handshake;
import org.jboss.websockets.oio.internal.protocol.ietf13.Hybi13Handshake;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class WebSocketConnectionManager {
  private static final List<Handshake> websocketHandshakes;

  static {
    final List<Handshake> handshakeList = new ArrayList<Handshake>();
    handshakeList.add(new Hybi13Handshake());
    handshakeList.add(new Hybi07Handshake());
    handshakeList.add(new Hybi08Handshake());
    handshakeList.add(new Hybi00Handshake());

    websocketHandshakes = Collections.unmodifiableList(handshakeList);
  }

  public static OioWebSocket establish(String protocolName, HttpRequestBridge request, HttpResponseBridge response, ClosingStrategy closingStrategy) throws IOException {
    for (Handshake handshake : websocketHandshakes) {
      if (handshake.matches(request)) {
        /**
         * We found a matching handshake, so let's tell the web server we'd like to begin the process of
         * upgrading this connection to a WebSocket.
         */
        response.startUpgrade();

        //log.debug("Found a compatible handshake: (Version:"
        //        + handshake.getVersion() + "; Handler: " + handshake.getClass().getName() + ")");

        /* Sets the standard upgrade headers that are common to all HTTP 101 upgrades, as well as the
        * SEC_WEBSOCKETS_PROTOCOL header (if the protocol is specified) common to all WebSocket implementations.
        */
        response.setHeader("Upgrade", "WebSocket");
        response.setHeader("Connection", "Upgrade");

        if (protocolName != null && request.getHeader(WebSocketHeaders.SEC_WEBSOCKET_PROTOCOL.name()) != null)
          WebSocketHeaders.SEC_WEBSOCKET_PROTOCOL.set(response, protocolName);
        else {
          WebSocketHeaders.SEC_WEBSOCKET_PROTOCOL.set(response, null);
        }

        /**
         * Generate the server handshake response -- setting the necessary headers and also capturing
         * any data bound for the body of the response.
         */
        final byte[] handShakeData = handshake.generateResponse(request, response);

        // write the handshake data
        response.getOutputStream().write(handShakeData);

        /**
         * Obtain an WebSocket instance from the handshaker.
         */
        final OioWebSocket webSocket
                = handshake.getWebSocket(request, response, closingStrategy);

        //log.debug("Using WebSocket implementation: " + webSocket.getClass().getName());

        response.sendUpgrade();
        return webSocket;
      }
    }
    return null;
  }
}
