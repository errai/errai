/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

package org.jboss.errai.bus.server.io;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.server.io.buffers.Buffer;
import org.jboss.errai.bus.server.io.buffers.BufferCallback;
import org.jboss.errai.bus.server.io.buffers.BufferColor;
import org.jboss.errai.marshalling.server.protocol.ErraiProtocolServer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.jboss.errai.marshalling.server.protocol.ErraiProtocolServer.encodePayloadToByteArrayInputStream;

/**
 * @author Mike Brock
 */
public final class BufferHelper {
  private BufferHelper() {
  }

  public static class MultiMessageHandlerCallback implements BufferCallback {
    int brackCount;
    int seg;

    @Override
    public void before(final OutputStream outstream) throws IOException {
      outstream.write('[');
    }

    @Override
    public int each(int i, final OutputStream outstream) throws IOException {
      if (i == '{' && ++brackCount == 1 && seg != 0) {
        outstream.write(',');
      }
      else if (i == '}' && brackCount != 0 && --brackCount == 0) {
        seg++;
      }
//
//      switch (i) {
//        case '{':
//          if (++brackCount == 1 && seg != 0) {
//            outstream.write(',');
//          }
//          break;
//        case '}':
//          if (brackCount == 0) break;
//          if (--brackCount == 0) {
//            seg++;
//          }
//          break;
//      }
      return i;
    }

    @Override
    public void after(final OutputStream outstream) throws IOException {
      if (brackCount == 1) {
        outstream.write('}');
      }
      outstream.write(']');
    }
  }

  public static void encodeAndWrite(final Buffer buffer, final BufferColor bufferColor, final Message message)
          throws IOException {

    buffer.write(encodePayloadToByteArrayInputStream(message.getParts()), bufferColor);
  }

  private static final byte[] NOOP_ARRAY = new byte[0];

  public static void encodeAndWriteNoop(final Buffer buffer, final BufferColor bufferColor)
          throws IOException {

    buffer.write(NOOP_ARRAY.length, new ByteArrayInputStream(NOOP_ARRAY), bufferColor);
  }
}
