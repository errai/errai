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
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Mike Brock
 */
public class TransmissionBuffer implements Buffer {
  public static int DEFAULT_BUFFER_SIZE = (1024 * 1024) * 5;
  public static int DEFAULT_SEGMENT_SIZE = 1024 * 8;

  final int bufferSize;

  final ByteBuffer buffer;
  final byte[] segmentMap;

  final int segmentSize;
  final int segments;

  private final AtomicLong sequenceNumber = new AtomicLong();

  public TransmissionBuffer() {
    bufferSize = DEFAULT_BUFFER_SIZE;
    segments = DEFAULT_BUFFER_SIZE / DEFAULT_SEGMENT_SIZE;

    segmentSize = DEFAULT_SEGMENT_SIZE;
    buffer = ByteBuffer.allocateDirect(bufferSize);
    segmentMap = new byte[segments];
  }

  public TransmissionBuffer(int segmentSize, int segments) {
    // must pad segment size for size headers -- or the last segment may be odd-sized (that would not be good)
    this.segmentSize = segmentSize + 4;
    this.bufferSize = this.segmentSize * segments;
    this.segments = segments;


    buffer = ByteBuffer.allocateDirect(bufferSize);

    segmentMap = new byte[segments];
  }

  @Override
  public void write(final int writeSize, final InputStream inputStream, final BufferColor bufferColor) throws IOException {
    final int alloc = ((writeSize + 4) / segmentSize) + 1;
    final int seq = getNextSequence(alloc);
    final byte color = (byte) bufferColor.getColor();

    if (writeSize > bufferSize) {
      throw new RuntimeException("write size larger than buffer can fit");
    }

    /**
     * Allocate the segments to the this color
     */
    for (int i = 0; i < alloc; i++) {
      segmentMap[(seq + i) % segments] = color;
    }

    int writeCursor = seq * segmentSize;

    // encode content length.
    writeChunkSize(writeCursor, writeSize);

    writeCursor += 4;

    int end = writeCursor + writeSize;
    for (; writeCursor < end && writeCursor < bufferSize; writeCursor++) {
      buffer.put(writeCursor, (byte) inputStream.read());
      //buffer[writeCursor] = (byte) inputStream.read();
    }

    if (writeCursor < end) {
      for (int i = 0; i < end - bufferSize; i++) {
        buffer.put(i, (byte) inputStream.read());
      }
    }

    /**
     * Wake up any waiting readers.
     */
    bufferColor.wake();
  }

  private void get(int readSegment, final OutputStream outputStream, final BufferColor bufferColor) throws IOException {
    int readCursor = readSegment * segmentSize;

    if (readCursor == bufferSize) {
      readCursor = 0;
    }

    int readSize = readChunkSize(readCursor);

    readCursor += 4;

    final int endRead = readCursor + readSize;
    final int contiguousSegments = (readSize + 1) / segmentSize + 1;

    for (; readCursor < endRead && readCursor < bufferSize; readCursor++) {
      //   outputStream.write(buffer[readCursor]);
      outputStream.write(buffer.get(readCursor));
    }

    if (readCursor < endRead) {
      int remaining = endRead - bufferSize;
      for (int i = 0; i < remaining; i++) {
        outputStream.write(buffer.get(readCursor));
      }
    }


    bufferColor.incrementSequence(contiguousSegments);
  }

  @Override
  public void read(final OutputStream outputStream, final BufferColor bufferColor) throws IOException {
    final ReentrantLock lock = bufferColor.getLock();
    lock.lock();

    try {

      int readSegment;
      while ((readSegment = getNextSegment(bufferColor, bufferColor.getSequence())) != -1) {
        get(readSegment, outputStream, bufferColor);
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
      final Condition dataWaiting = bufferColor.getDataWaiting();
      boolean awoken = false;

      for (; ; ) {
        int seg;
        if ((seg = getNextSegment(bufferColor, bufferColor.getSequence())) != -1) {
          get(seg, outputStream, bufferColor);
          awoken = true;
          continue;
        }

        if (awoken) {
          return;
        }

        try {
          dataWaiting.await();
          awoken = true;
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
      final Condition dataWaiting = bufferColor.getDataWaiting();
      boolean awoken = false;

      for (; ; ) {
        int seg;
        if ((seg = getNextSegment(bufferColor, bufferColor.getSequence())) != -1) {
          get(seg, outputStream, bufferColor);
          awoken = true;
          continue;
        }

        if (awoken || nanos <= 0) {
          return;
        }

        try {
          nanos = dataWaiting.awaitNanos(nanos);
          awoken = true;
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

  /**
   * Calculate the next segment.
   *
   * @param bufferColor
   * @param index
   * @return
   */
  private int getNextSegment(final BufferColor bufferColor, final int index) {
    final int color = bufferColor.getColor();
    final int seq = sequenceNumber.intValue();

    int delta = 0;
    try {
      for (int i = index; i < seq; i++) {
        byte seg = segmentMap[i % segments];
        if (seg == color) {
          return i % segments;
        }
        ++delta;
      }
    }
    finally {
      if (delta > 0)
        bufferColor.incrementSequence(delta);
    }

    return -1;
  }

  private int getNextSequence(final int neededSegments) {
    return (int) sequenceNumber.getAndAdd(neededSegments) % segments;
  }

  private void writeChunkSize(int position, int size) {
//    buffer[position++] = (byte) ((size >> 24) & 0xFF);
//    buffer[position++] = (byte) ((size >> 16) & 0xFF);
//    buffer[position++] = (byte) ((size >> 8) & 0xFF);
//    buffer[position] = (byte) (size & 0xFF);

    buffer.putInt(position, size);
  }

  private int readChunkSize(int position) {
//    return (((((int) buffer[position + 3]) & 0xFF) << 32) +
//            ((((int) buffer[position + 2]) & 0xFF) << 40) +
//            ((((int) buffer[position + 1]) & 0xFF) << 48) +
//            ((((int) buffer[position]) & 0xFF) << 56));

    return buffer.getInt(position);
  }

  public void dumpSegments() {

    System.out.println();
    System.out.println("SEGMENT DUMP");

    for (int i = 0; i < segmentMap.length && i < sequenceNumber.intValue(); i++) {
      StringBuilder build = new StringBuilder();
      int pos = i * segmentSize;
      int length = readChunkSize(pos);
      build.append("Segment " + i + " <color:" + (int) segmentMap[i] + ";length:" + length + ";location:" + pos + ">");
      pos += 4;

      byte[] buf = new byte[length];
      for (int x = 0; x < length; x++) {
        buf[x] = buffer.get(pos + x);
      }

      build.append("::").append(new String(buf));
      length += 4;

      if (length > segmentSize) {
        int segSpan = (length / segmentSize) + 1;

        i += segSpan;
      }

      System.out.println(build.toString());
    }

  }
}
