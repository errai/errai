/*
 * Copyright 2012 JBoss, by Red Hat, Inc
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

import static java.lang.System.nanoTime;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.server.api.MessageQueue;
import org.jboss.errai.bus.server.io.buffers.Buffer;
import org.jboss.errai.bus.server.io.buffers.BufferColor;
import org.jboss.errai.bus.server.util.MarkedByteWriteAdapter;
import org.jboss.errai.bus.server.util.ServerBusTools;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This implementation of {@link MessageDeliveryHandler} facilitates the buffering of all inbound message
 * traffic to a singular ring-buffer.
 *
 * @author Mike Brock
 */
public class BufferDeliveryHandler implements MessageDeliveryHandler, Buffered, Cleanable {
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

//      if (queue.isPaged()) {
//        try {
//          synchronized (queue.getPageLock()) {
//            if (queue.isPaged()) {
//              PageUtil.writeToPageFile(queue, ServerBusTools.encodeMessageToByteArrayInputStream(message), true);
//              return true;
//            }
//          }
//        }
//        finally {
//          final ReentrantLock lock = bufferColor.getLock();
//          lock.lock();
//          try {
//            bufferColor.wake();
//          }
//          finally {
//            lock.unlock();
//          }
//        }
//      }

      BufferHelper.encodeAndWrite(buffer, bufferColor, message);

//      if (queue.incrementMessageCount() > 10
//          && !lastTransmissionWithin(queue, TimeUnit.SECONDS.toNanos(10))) {
//        // disconnect this client
//
//
//        PageUtil.pageWaitingToDisk(queue);
//      }
    }
    finally {
      queue.fireActivationCallback();
    }

    return true;
  }

  @Override
  public void noop(final MessageQueue queue) throws IOException {
    BufferHelper.encodeAndWriteNoop(queue.getBuffer(), queue.getBufferColor());
  }

//  @Override
//  public boolean pageOut(final MessageQueue queue) {
//    return PageUtil.pageWaitingToDisk(queue);
//  }
//
//  @Override
//  public void discardPageData(final MessageQueue queue) {
//    PageUtil.discardPageData(queue);
//  }

  @Override
  public void clean(final MessageQueue queue) {
 //   discardPageData(queue);
  }

  @Override
  public boolean copyFromBuffer(final boolean waitForData,
                                final MessageQueue queue,
                                final ByteWriteAdapter toAdapter) throws IOException {

//    if (queue.isPaged()) {
//      synchronized (queue.getPageLock()) {
//        if (queue.isPaged()) {
//          PageUtil.readInPageFile(queue, toAdapter, new MultiMessageFilter());
//          return false;
//        }
//      }
//    }

    final MarkedByteWriteAdapter markedOutputStream = new MarkedByteWriteAdapter(toAdapter);

    try {
      if (waitForData) {
        queue.getBuffer().readWait(TimeUnit.SECONDS, 20, markedOutputStream, queue.getBufferColor(),
            new MultiMessageFilter());
      }
      else {
        queue.getBuffer().read(markedOutputStream, queue.getBufferColor(), new MultiMessageFilter());
      }

      markedOutputStream.flush();

      if (markedOutputStream.dataWasWritten()) {
        queue.resetMessageCount();
        return true;
      }
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }

    return false;
  }


  private static boolean lastTransmissionWithin(final MessageQueue queue, final long nanos) {
    return (nanoTime() - queue.getLastTransmissionTime()) < nanos;
  }
}
