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

package org.jboss.errai.bus.server.io.buffers;

import javax.swing.text.Segment;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Mike Brock
 */
public class TransmissionBuffer implements Buffer {
  public static int DEFAULT_BUFFER_SIZE = (1024 * 1024);
  public static int DEFAULT_MAX_SEGMENTS = 1000;

  final int bufferSize;

  final byte[] buffer;
  final byte[] segmentMap;

  final int segmentSize;
  final int segments;

  private final AtomicInteger sequenceNumber = new AtomicInteger();

  public TransmissionBuffer() {
    bufferSize = DEFAULT_BUFFER_SIZE;
    segments = DEFAULT_MAX_SEGMENTS;

    segmentSize = bufferSize / segments;
    buffer = new byte[bufferSize];
    segmentMap = new byte[segments];
  }

  public TransmissionBuffer(int segmentSize, int segments) {
    this.bufferSize = segmentSize * segments;
    this.segments = segments;
    this.segmentSize = segmentSize;

    buffer = new byte[bufferSize];
    segmentMap = new byte[segments];
  }

  @Override
  public void write(final int writeSize, final InputStream inputStream, final BufferColor bufferColor) throws IOException {
    final int alloc = ((writeSize + 1) / segmentSize) + 1;
    final int seq = getNextSequence(alloc);
    final byte color = (byte) bufferColor.getColor();

    /**
     * Allocate the segments to the this color
     */
    for (int i = 0; i < alloc; i++) {
      segmentMap[(seq + i) % segments] = color;
    }

    int writeCursor = seq * segmentSize;

    for (int i = 0; i < writeSize; i++) {
      buffer[writeCursor++] = (byte) inputStream.read();
    }
    //append EOF
    buffer[writeCursor] = -1;

    /**
     * Wake up any waiting readers.
     */
    bufferColor.wake();
  }

  @Override
  public void read(final OutputStream outputStream, final BufferColor bufferColor) throws IOException {
    final ReentrantLock lock = bufferColor.getLock();
    lock.lock();

    try {
      final int color = bufferColor.getColor();

      int readSegment;
      for (;(readSegment = getNextSegment(color, bufferColor.getSequence())) != -1; bufferColor.incrementSequence()) {
        int readCursor = readSegment * segmentSize;
        final int endSegment = readCursor + segmentSize;

        for (; readCursor < endSegment; readCursor++) {
          byte read = buffer[readCursor];
          if (read == -1) {
            break;
          }
          outputStream.write(read);
        }
      }

    }
    finally {
      lock.unlock();
    }
  }

  @Override
  public void readWait(final OutputStream outputStream, final BufferColor bufferColor) throws InterruptedException, IOException {
    final ReentrantLock lock = bufferColor.getLock();
    lock.lockInterruptibly();

    try {
      final int color = bufferColor.getColor();

      for (; ; ) {
        if (getNextSegment(color, bufferColor.getSequence()) != -1) {
          read(outputStream, bufferColor);
          return;
        }

        Condition dataWaiting = bufferColor.getDataWaiting();

        try {
          dataWaiting.await();
        }
        catch (InterruptedException e) {
          dataWaiting.signal();
          throw e;
        }
      }
    }
    finally {
      lock.unlock();
    }
  }

  @Override
  public void readWait(final TimeUnit unit, final long time,
                       final OutputStream outputStream, final BufferColor bufferColor) throws IOException, InterruptedException {
    long nanos = unit.toNanos(time);
    final ReentrantLock lock = bufferColor.getLock();
    lock.lockInterruptibly();

    try {
      final int color = bufferColor.getColor();
      final Condition dataWaiting = bufferColor.getDataWaiting();

      for (; ; ) {
        if (getNextSegment(color, bufferColor.getSequence()) != -1) {
          read(outputStream, bufferColor);
          return;
        }
        else if (nanos <= 0) {
          return;
        }

        try {
          nanos = dataWaiting.awaitNanos(nanos);
        }
        catch (InterruptedException e) {
          dataWaiting.signal();
          throw e;
        }
      }
    }
    finally {
      lock.unlock();
    }
  }

  private int getNextSegment(final int color, final int index) {
    for (int i = index; i < sequenceNumber.intValue(); i++) {
      byte seg = segmentMap[i % segments];
      if (seg == color) return i % segments;
      else if (seg == 0 || i >= sequenceNumber.intValue()) break;
    }
    return -1;
  }

  private int getNextSequence(final int neededSegments) {
    return (int) sequenceNumber.getAndAdd(neededSegments) % segments;
  }

}
