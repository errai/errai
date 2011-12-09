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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
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
  public  void write(final InputStream inputStream, final Segment segment) {
    final ReentrantLock lock = segment.getLock();
    lock.lock();
    final Condition dataWaiting = segment.getDataWaiting();

    try {
      int free = segment.getFree();
      int lineSize = segment.getWriteLineSize();
      int cursor = segment.getWriteCursor();

      try {
        int read;
        while ((read = inputStream.read()) != -1) {
          if (lineSize-- == 0) {
            cursor = segment.getStart();
          }

          if (--free == 0) {
            throw new BufferOverflowException();
          }

          buffer[cursor++] = (byte) read;
        }
      }
      catch (IOException e) {
        throw new RuntimeException("error reading", e);
      }

      segment.setWriteCursor(cursor);
    }
    finally {
      dataWaiting.signal();
      lock.unlock();
    }
  }

  @Override
  public void read(final OutputStream outputStream, final Segment segment) {
    final ReentrantLock lock = segment.getLock();
    lock.lock();

    try {
      int toRead = segment.getToRead();
      int lineSize = segment.getReadLineSize();
      int cursor = segment.getReadCursor();

      try {
        while (toRead-- != 0) {
          if (lineSize-- == 0) {
            cursor = segment.getStart();
          }

          outputStream.write(buffer[cursor++]);
        }
      }
      catch (IOException e) {
        throw new RuntimeException("error writing", e);
      }

      segment.setReadCursor(cursor);
    }
    finally {
      lock.unlock();
    }
  }

  @Override
  public void readWait(OutputStream outputStream, Segment segment) throws InterruptedException {
    final ReentrantLock lock = segment.getLock();
    lock.lockInterruptibly();

    try {
      while (true) {
        if (segment.getToRead() != 0) {
          read(outputStream, segment);
        }

        Condition dataWaiting = segment.getDataWaiting();

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
  public  void readWait(TimeUnit unit, long time, final OutputStream outputStream, final Segment segment) throws InterruptedException {
    long nanos = unit.toNanos(time);
    final ReentrantLock lock = segment.getLock();
    lock.lockInterruptibly();

    try {
      final Condition dataWaiting = segment.getDataWaiting();

      for (; ; ) {
        if (segment.getToRead() > 0) {
          read(outputStream, segment);
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

  @Override
  public Segment allocateSegment() {
    final int seg = findEmptySegment();
    segmentMap[seg] = 1;

    final int segStart = segmentSize * seg;

    return new Segment(segStart, segStart + segmentSize);
  }

  @Override
  public void deallocateSegment(Segment segment) {
    int segmentNumber = segment.getStart() / segmentSize;
    segmentMap[segmentNumber] = 0;
  }

  private int findEmptySegment() {
    for (int i = 0; i < segments; i++) {
      if (segmentMap[i] == 0) {
        return i;
      }
    }
    throw new NoSegmentAvailableException("all " + segments + " in use");
  }
}
