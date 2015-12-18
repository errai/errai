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

package org.jboss.websockets.oio.internal.protocol.ietf07;


import org.jboss.websockets.oio.ClosingStrategy;
import org.jboss.websockets.oio.HttpRequestBridge;
import org.jboss.websockets.oio.HttpResponseBridge;
import org.jboss.websockets.oio.OioWebSocket;
import org.jboss.websockets.oio.internal.Handshake;
import org.jboss.websockets.oio.internal.WebSocketHeaders;
import org.jboss.websockets.oio.internal.util.Base64;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.jboss.websockets.oio.internal.WebSocketHeaders.ORIGIN;
import static org.jboss.websockets.oio.internal.WebSocketHeaders.SEC_WEBSOCKET_KEY;
import static org.jboss.websockets.oio.internal.WebSocketHeaders.SEC_WEBSOCKET_LOCATION;
import static org.jboss.websockets.oio.internal.WebSocketHeaders.SEC_WEBSOCKET_ORIGIN;
import static org.jboss.websockets.oio.internal.WebSocketHeaders.SEC_WEBSOCKET_PROTOCOL;
import static org.jboss.websockets.oio.internal.WebSocketHeaders.SEC_WEBSOCKET_VERSION;

/**
 * The handshaking protocol implementation for Hybi-07.
 *
 * @author Mike Brock
 */
public class Hybi07Handshake extends Handshake {
  protected Hybi07Handshake(final String version) {
    super(version, "SHA1", "258EAFA5-E914-47DA-95CA-C5AB0DC85B11");
  }

  public Hybi07Handshake() {
    this("7");
  }

  @Override
  public OioWebSocket getWebSocket(final HttpRequestBridge request,
                                   final HttpResponseBridge response,
                                   final ClosingStrategy closingStrategy) throws IOException {
    return Hybi07Socket.from(request, response, closingStrategy);
  }

  @Override
  public boolean matches(final HttpRequestBridge request) {
    return (SEC_WEBSOCKET_KEY.isIn(request) && SEC_WEBSOCKET_VERSION.matches(request, getVersion()));
  }

  @Override
  public byte[] generateResponse(final HttpRequestBridge request,
                                 final HttpResponseBridge response) throws IOException {

    if (ORIGIN.isIn(request)) {
      SEC_WEBSOCKET_ORIGIN.set(response, ORIGIN.get(request));
    }

    SEC_WEBSOCKET_PROTOCOL.copy(request, response);

    SEC_WEBSOCKET_LOCATION.set(response, getWebSocketLocation(request));

    final String key = SEC_WEBSOCKET_KEY.get(request);
    final String solution = solve(key);

    WebSocketHeaders.SEC_WEBSOCKET_ACCEPT.set(response, solution);

    return new byte[0];
  }

  public String solve(final String nonceBase64) {
    try {
      final String concat = nonceBase64.trim().concat(getMagicNumber());
      final MessageDigest digest = MessageDigest.getInstance(getHashAlgorithm());
      digest.update(concat.getBytes("UTF-8"));
      final String result = Base64.encodeBase64String(digest.digest()).trim();
//
//      System.out.println("Browser Key: '" + nonceBase64 + "'");
//      System.out.println("Concat     : '" + concat + "'");
//      System.out.println("Result     : '" + result + "'");

      return result;
    }
    catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("error generating hash", e);
    }
    catch (UnsupportedEncodingException e) {
      throw new RuntimeException("could not get UTF-8 bytes");
    }
  }
}
