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

package org.jboss.errai.bus.server;

import junit.framework.TestCase;
import org.jboss.as.websockets.protocol.ietf00.Hybi00Handshake;
import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.api.SessionEndListener;
import org.jboss.errai.bus.server.io.buffers.BufferColor;
import org.jboss.errai.bus.server.io.buffers.TransmissionBuffer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Mike Brock
 */
public class BusTests extends TestCase {
  public void testNewClientsDontReceiveBackBroadcasts() throws IOException {
    TransmissionBuffer buffer = TransmissionBuffer.create();
    QueueSession session = new QueueSession() {
      @Override
      public String getSessionId() {
        return "ID";
      }

      @Override
      public boolean isValid() {
        return false;
      }

      @Override
      public boolean endSession() {
        return false;
      }

      @Override
      public void setAttribute(String attribute, Object value) {
      }

      @Override
      public <T> T getAttribute(Class<T> type, String attribute) {
        return null;
      }

      @Override
      public Collection<String> getAttributeNames() {
        return null;
      }

      @Override
      public boolean hasAttribute(String attribute) {
        return false;
      }

      @Override
      public boolean removeAttribute(String attribute) {
        return false;
      }

      @Override
      public void addSessionEndListener(SessionEndListener listener) {
      }
    };

    BufferColor global = BufferColor.getAllBuffersColor();
    String bufData = "writeIn";

    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bufData.getBytes());
    buffer.write(byteArrayInputStream, global);

    MessageQueueImpl messageQueue = new MessageQueueImpl(buffer, session);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    messageQueue.poll(false, outputStream);

    assertEquals("[]", new String(outputStream.toByteArray()));
  }

  public void testHandshake() throws Exception {
    final List<Byte> byteList = new ArrayList<Byte>();
    byte firstByte = (byte) -1;

    System.out.println((int) (firstByte & 127));

//    byteList.add(firstByte);
//    String toEncode = "Test!";
//    byteList.add((byte) toEncode.length());
//
//    for (byte b : toEncode.getBytes()) {
//      byteList.add(b);
//    }
//    byteList.add((byte) 0x01);
//
//    String s = JBossAS7WebSocketServlet.readFrame(null, new InputStream() {
//      int cursor;
//
//      @Override
//      public int read() throws IOException {
//        return byteList.get(cursor++);
//      }
//
//      @Override
//      public int available() throws IOException {
//        return byteList.size() - cursor;
//      }
//    });
//
//    System.out.println(s);
  }

  public void testHybi00Handshake() throws Exception {
    String key1 = "P388 O503D&ul7 {K%gX( %7  15";
    String key2 = "1 N ?|k UT0or 3o  4 I97N 5-S3O 31";
    //String additionalBytes = "Tm[K T2u";
    byte[] additionalBytes = {0x47, 0x30, 0x22, 0x2D, 0x5A, 0x3F, 0x47, 0x58};

    byte[] solved =
            Hybi00Handshake.solve("MD5", Hybi00Handshake.decodeKey(key1), Hybi00Handshake.decodeKey(key2),
                    additionalBytes);

    for (byte b : solved) {
      System.out.print(Integer.toHexString(b) + " ");
    }
    System.out.println();
  }

}
