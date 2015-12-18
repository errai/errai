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

import org.jboss.as.websockets.Frame;
import org.jboss.as.websockets.FrameType;
import org.jboss.as.websockets.frame.BinaryFrame;
import org.jboss.as.websockets.frame.CloseFrame;
import org.jboss.as.websockets.frame.PingFrame;
import org.jboss.as.websockets.frame.PongFrame;
import org.jboss.as.websockets.frame.TextFrame;
import org.jboss.websockets.oio.ClosingStrategy;
import org.jboss.websockets.oio.HttpRequestBridge;
import org.jboss.websockets.oio.HttpResponseBridge;
import org.jboss.websockets.oio.OioWebSocket;
import org.jboss.websockets.oio.internal.AbstractWebSocket;
import org.jboss.websockets.oio.internal.util.Hash;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Implementation of the Hybi-07 Websocket Framing Protocol.
 *
 * @author Mike Brock
 */
public class Hybi07Socket extends AbstractWebSocket {

  public Hybi07Socket(final InputStream inputStream,
                      final OutputStream outputStream,
                      final ClosingStrategy closingStrategy) {
    super(inputStream, outputStream, closingStrategy);

  }

  public static OioWebSocket from(final HttpRequestBridge request,
                                  final HttpResponseBridge response,
                                  final ClosingStrategy closingStrategy) throws IOException {

    return new Hybi07Socket(
            request.getInputStream(),
            response.getOutputStream(),
            closingStrategy);
  }

  private static final byte FRAME_OPCODE = 127;
  private static final byte FRAME_MASKED = Byte.MIN_VALUE;
  private static final byte FRAME_LENGTH = 127;

  private FrameType getNextFrameType() throws IOException {
    switch ((inputStream.read() & FRAME_OPCODE)) {
      case 0x00:
        return FrameType.Continuation;
      case 0x01:
        return FrameType.Text;
      case 0x02:
        return FrameType.Binary;
      case 0x08:
        return FrameType.ConnectionClose;
      case 0x09:
        return FrameType.Ping;
      case 0x0A:
        return FrameType.Pong;
      default:
        return FrameType.Unknown;
    }
  }

  private int getPayloadSize(int b) throws IOException {
    int payloadLength = (b & FRAME_LENGTH);
    if (payloadLength == 126) {
      payloadLength = ((inputStream.read() & 0xFF) << 8) +
              (inputStream.read() & 0xFF);
    }
    else if (payloadLength == 127) {
      // ignore the first 4-bytes. We can't deal with 64-bit ints right now anyways.
      inputStream.read();
      inputStream.read();
      inputStream.read();
      inputStream.read();
      payloadLength = ((inputStream.read() & 0xFF) << 24) +
              ((inputStream.read() & 0xFF) << 16) +
              ((inputStream.read() & 0xFF) << 8) +
              ((inputStream.read() & 0xFF));
    }

    return payloadLength;
  }


  @SuppressWarnings("ResultOfMethodCallIgnored")
  private String _readTextFrame() throws IOException {
    int b = inputStream.read();
    final boolean frameMasked = (b & FRAME_MASKED) != 0;
    int payloadLength = getPayloadSize(b);

    final byte[] frameMaskingKey = new byte[4];

    if (frameMasked) {
      inputStream.read(frameMaskingKey);
    }

    final StringBuilder payloadBuffer = new StringBuilder(payloadLength);

    int read = 0;
    if (frameMasked) {
      do {
        payloadBuffer.append(((char) ((inputStream.read() ^ frameMaskingKey[read % 4]) & 127)));
      }
      while (++read < payloadLength);
    }
    else {
      // support unmasked frames for testing.

      do {
        payloadBuffer.append((char) inputStream.read());
      }
      while (++read < payloadLength);
    }

    return payloadBuffer.toString();
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  public byte[] _readBinaryFrame() throws IOException {
    int b = inputStream.read();
    final boolean frameMasked = (b & FRAME_MASKED) != 0;
    int payloadLength = getPayloadSize(b);

    final byte[] frameMaskingKey = new byte[4];

    if (frameMasked) {
      inputStream.read(frameMaskingKey);
    }

    final byte[] buf = new byte[payloadLength];

    int read = 0;
    if (frameMasked) {
      do {
        buf[read] = (byte) ((inputStream.read() ^ frameMaskingKey[read % 4]) & 127);
      }
      while (++read < payloadLength);
    }
    else {
      // support unmasked frames for testing.

      do {
        buf[read] = (byte) inputStream.read();
      }
      while (++read < payloadLength);
    }

    return buf;
  }


  private void _writeTextFrame(final String txt) throws IOException {
    byte[] strBytes = txt.getBytes("UTF-8");

    outputStream.write(-127);
    if (strBytes.length > Short.MAX_VALUE) {
      outputStream.write(-1); // unsigned 7-bit int of value 127 -- leading bit indicates masking.

      // pad the first 4 bytes of 64-bit context length. If this frame is larger than 2GB, you're in trouble. =)
      outputStream.write(0);
      outputStream.write(0);
      outputStream.write(0);
      outputStream.write(0);
      outputStream.write((strBytes.length & 0xFF) << 24);
      outputStream.write((strBytes.length & 0xFF) << 16);
      outputStream.write((strBytes.length & 0xFF) << 8);
      outputStream.write((strBytes.length & 0xFF));
    }
    else if (strBytes.length > 125) {
      outputStream.write(-2); // unsigned 7-bit int of value 126 -- leading bit indicates masking.
      outputStream.write(((strBytes.length >> 8) & 0xFF));
      outputStream.write(((strBytes.length) & 0xFF));
    }
    else {
      outputStream.write(-128 | (strBytes.length & 127));
    }

    /**
     * From IETF Websockets Protocol Specification:
     *
     *   The masking key is a 32-bit value chosen at random by the client.
     The masking key MUST be derived from a strong source of entropy, and
     the masking key for a given frame MUST NOT make it simple for a
     server to predict the masking key for a subsequent frame.  RFC 4086
     [RFC4086] discusses what entails a suitable source of entropy for
     security-sensitive applications.
     */
    final byte[] mask = new byte[4];
    Hash.getRandomBytes(mask);
    outputStream.write(mask);

    int len = strBytes.length;
    for (int j = 0; j < len; j++) {
      outputStream.write((strBytes[j] ^ mask[j % 4]));
    }

    outputStream.flush();
  }

  private void _writeBinaryFrame(final byte[] data) throws IOException {
    final int len = data.length;

    outputStream.write(-126);
    if (data.length > Short.MAX_VALUE) {
      outputStream.write(127);

      // pad the first 4 bytes of 64-bit context length. If this frame is larger than 2GB, you're in trouble. =)
      outputStream.write(0);
      outputStream.write(0);
      outputStream.write(0);
      outputStream.write(0);
      outputStream.write((len & 0xFF) << 24);
      outputStream.write((len & 0xFF) << 16);
      outputStream.write((len & 0xFF) << 8);
      outputStream.write((len & 0xFF));
    }
    else if (data.length > 125) {
      outputStream.write(126);
      outputStream.write(((len >> 8) & 0xFF));
      outputStream.write(((len) & 0xFF));
    }
    else {
      outputStream.write((len & 127));
    }

    final byte[] mask = new byte[4];
    Hash.getRandomBytes(mask);
    outputStream.write(mask);

    for (int j = 0; j < len; j++) {
      outputStream.write((data[j] ^ mask[j % 4]));
    }

    outputStream.flush();
  }

  private void _sendConnectionClose() throws IOException {
    outputStream.write(-120);
    outputStream.write(125);
    outputStream.flush();
  }

  private void _sendPing() throws IOException {
    outputStream.write(-119);
    outputStream.write(125);
    outputStream.flush();
  }

  private void _sendPong() throws IOException {
    outputStream.write(-118);
    outputStream.write(125);
    outputStream.flush();
  }

  public Frame readFrame() throws IOException {
    switch (getNextFrameType()) {
      case Text:
        return TextFrame.from(_readTextFrame());
      case Binary:
        return BinaryFrame.from(_readBinaryFrame());
      case Ping:
        return PingFrame.get();
      case Pong:
        return PongFrame.get();
      case ConnectionClose:
        closeSocket();
        return CloseFrame.get();
    }
    throw new IOException("unknown frame type");
  }


  public void writeFrame(Frame frame) throws IOException {
    switch (frame.getType()) {
      case Text:
        _writeTextFrame(((TextFrame) frame).getText());
        break;
      case Binary:
        _writeBinaryFrame(((BinaryFrame) frame).getByteArray());
        break;
      case ConnectionClose:
        _sendConnectionClose();
        break;
      case Ping:
        _sendPing();
        break;
      case Pong:
        _sendPong();
        break;
      default:
        throw new IOException("unable to handle frame type: " + frame.getType());
    }
  }
}
