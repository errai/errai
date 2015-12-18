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

package org.jboss.errai.bus.server.io;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.server.api.MessageQueue;
import org.jboss.errai.bus.server.io.buffers.Buffer;
import org.jboss.errai.bus.server.io.buffers.BufferColor;
import org.jboss.errai.bus.server.io.buffers.BufferOverflowException;
import org.jboss.errai.bus.server.util.MarkedByteWriteAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * This implementation of {@link MessageDeliveryHandler} facilitates the buffering of all inbound message
 * traffic to a singular ring-buffer.
 *
 * @author Mike Brock
 */
public class BufferDeliveryHandler implements MessageDeliveryHandler, Buffered, Cleanable {
  private static Logger log = LoggerFactory.getLogger(BufferDeliveryHandler.class);
  private static final BufferDeliveryHandler singleton = new BufferDeliveryHandler();

  public static BufferDeliveryHandler getInstance() {
    return singleton;
  }

  private BufferDeliveryHandler() {
  }

  @Override
  public boolean deliver(final MessageQueue queue, final Message message) throws IOException {
    try {
      final Buffer buffer = queue.getBuffer();
      final BufferColor bufferColor = queue.getBufferColor();

      BufferHelper.encodeAndWrite(buffer, bufferColor, message);
    }
    finally {
      queue.incrementMessageCount();
      queue.fireActivationCallback();
    }

    return true;
  }

  @Override
  public void noop(final MessageQueue queue) throws IOException {
    BufferHelper.encodeAndWriteNoop(queue.getBuffer(), queue.getBufferColor());
  }

  @Override
  public void clean(final MessageQueue queue) {
    //   discardPageData(queue);
  }


  @Override
  public boolean copyFromBuffer(final MessageQueue queue,
                                final ByteWriteAdapter toAdapter) throws IOException {


    final MarkedByteWriteAdapter markedOutputStream = new MarkedByteWriteAdapter(toAdapter);

    try {
      queue.getBuffer().read(markedOutputStream, queue.getBufferColor(), new MultiMessageFilter());
      
      if (markedOutputStream.dataWasWritten() && markedOutputStream.getBytesWritten() > 2) {
        queue.resetMessageCount();
        return true;
      }
    }
    catch (BufferOverflowException e) {
      queue.getBufferColor().getSequence().set(queue.getBuffer().getHeadSequence());
      log.warn("buffer data was evicted for session " + queue.getSession().getSessionId()
          + " due to overflow condition. (consider increasing buffer size with errai.bus.buffer_size "
          + "in ErraiService.properties)");
    }

    return false;
  }

  @Override
  public boolean copyFromBuffer(TimeUnit timeUnit, int timeout, MessageQueue queue, ByteWriteAdapter toAdapter)
      throws IOException {
    final MarkedByteWriteAdapter markedOutputStream = new MarkedByteWriteAdapter(toAdapter);

    try {
      queue.getBuffer().readWait(timeUnit, timeout, markedOutputStream, queue.getBufferColor(),
          new MultiMessageFilter());
      
      if (markedOutputStream.dataWasWritten() && markedOutputStream.getBytesWritten() > 2) {
        queue.resetMessageCount();
        return true;
      }
    }
    catch (BufferOverflowException e) {
      queue.getBufferColor().getSequence().set(queue.getBuffer().getHeadSequence());
      log.warn("buffer data was evicted for session " + queue.getSession().getSessionId()
          + " due to overflow condition. (consider increasing buffer size with errai.bus.buffer_size "
          + "in ErraiService.properties)");
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }

    return false;
  }
}
