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

package org.jboss.websockets.oio.internal.protocol.ietf00;

import org.jboss.websockets.oio.ClosingStrategy;
import org.jboss.websockets.oio.HttpRequestBridge;
import org.jboss.websockets.oio.HttpResponseBridge;
import org.jboss.websockets.oio.OioWebSocket;
import org.jboss.websockets.oio.internal.Handshake;
import org.jboss.websockets.oio.internal.WebSocketHeaders;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.jboss.websockets.oio.internal.WebSocketHeaders.SEC_WEBSOCKET_KEY1;
import static org.jboss.websockets.oio.internal.WebSocketHeaders.SEC_WEBSOCKET_KEY2;

/**
 * @author Mike Brock
 */
public class Hybi00Handshake extends Handshake {
  public Hybi00Handshake() {
    super("0", "MD5", null);
  }

  @Override
  public boolean matches(HttpRequestBridge request) {
    return SEC_WEBSOCKET_KEY1.isIn(request) && SEC_WEBSOCKET_KEY2.isIn(request);
  }

  @Override
  public OioWebSocket getWebSocket(final HttpRequestBridge request,
                                   final HttpResponseBridge response,
                                   final ClosingStrategy closingStrategy) throws IOException {
    return Hybi00Socket.from(request, response, closingStrategy);
  }

  @Override
  public byte[] generateResponse(final HttpRequestBridge request,
                                 final HttpResponseBridge response) throws IOException {

    if (WebSocketHeaders.ORIGIN.isIn(request)) {
      WebSocketHeaders.SEC_WEBSOCKET_ORIGIN.set(response, WebSocketHeaders.ORIGIN.get(request).trim());
    }

    final String origin = "ws://" + request.getHeader("Host") + request.getRequestURI();

    WebSocketHeaders.SEC_WEBSOCKET_LOCATION.set(response, origin);
    WebSocketHeaders.SEC_WEBSOCKET_PROTOCOL.copy(request, response);

    // Calculate the answer of the challenge.
    final String key1 = SEC_WEBSOCKET_KEY1.get(request);
    final String key2 = SEC_WEBSOCKET_KEY2.get(request);
    final byte[] key3 = new byte[8];

    final InputStream inputStream = request.getInputStream();
    inputStream.read(key3);

    final byte[] solution = solve(getHashAlgorithm(), key1, key2, key3);

    return solution;
  }

  public static byte[] solve(final String hashAlgorithm, String encodedKey1, String encodedKey2, byte[] key3) {
    return solve(hashAlgorithm, decodeKey(encodedKey1), decodeKey(encodedKey2), key3);
  }

  public static byte[] solve(final String hashAlgorithm, long key1, long key2, byte[] key3) {
    ByteBuffer buffer = ByteBuffer.allocate(16).order(ByteOrder.BIG_ENDIAN);

    buffer.putInt((int) key1);
    buffer.putInt((int) key2);
    buffer.put(key3);

    final byte[] solution = new byte[16];
    buffer.rewind();
    buffer.get(solution, 0, 16);

    try {
      final MessageDigest digest = MessageDigest.getInstance(hashAlgorithm);
      return digest.digest(solution);
    }
    catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("error generating hash", e);
    }
  }

  public static long decodeKey(final String encoded) {
    final int len = encoded.length();
    int numSpaces = 0;

    for (int i = 0; i < len; ++i) {
      if (encoded.charAt(i) == ' ') {
        ++numSpaces;
      }
    }

    final String digits = encoded.replaceAll("[^0-9]", "");
    final long product = Long.parseLong(digits);
    return product / numSpaces;
  }
}
