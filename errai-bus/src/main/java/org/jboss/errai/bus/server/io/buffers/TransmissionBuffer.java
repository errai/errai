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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Mike Brock
 */
public class TransmissionBuffer implements Buffer {
  public static int DEFAULT_BUFFER_SIZE = (1024 * 1024) * 20;
  public static int DEFAULT_SEGMENT_SIZE = 1024 * 8;

  final int bufferSize;

  final ByteBuffer buffer;
  final short[] segmentMap;

  final int segmentSize;
  final int segments;

  private final AtomicLong sequenceNumber = new AtomicLong();

  private final ReadWriteLock segmentTableLock = new ReentrantReadWriteLock();

  public TransmissionBuffer() {
    bufferSize = DEFAULT_BUFFER_SIZE;
    segments = DEFAULT_BUFFER_SIZE / DEFAULT_SEGMENT_SIZE;

    segmentSize = DEFAULT_SEGMENT_SIZE;
    buffer = ByteBuffer.allocateDirect(bufferSize);
    segmentMap = new short[segments];
  }

  public TransmissionBuffer(int segmentSize, int segments) {
    // must pad segment size for size headers -- or the last segment may be odd-sized (that would not be good)
    this.segmentSize = segmentSize + 4;
    this.bufferSize = this.segmentSize * segments;
    this.segments = segments;


    buffer = ByteBuffer.allocateDirect(bufferSize);

    segmentMap = new short[segments];
  }

  private int allocSegmentTable(int writeSize, short color) {
    final int allocSize = ((writeSize + 4) / segmentSize) + 1;
    int seq = (int) sequenceNumber.getAndAdd(allocSize) % segments;
    /**
     * Allocate the segments to the this color
     */
    for (int i = 0; i < allocSize; i++) {
      segmentMap[(seq + i) % segments] = color;
    }

    return seq * segmentSize;
  }

  @Override
  public void write(final int writeSize, final InputStream inputStream, final BufferColor bufferColor) throws IOException {
    // try to obtain a lock. if it's taken, we can proceed anyways.
    // this is to prevent partial reads of the segmentMap by readers.
    final boolean haveLock = segmentTableLock.writeLock().tryLock();
    try {
      if (writeSize > bufferSize) {
        throw new RuntimeException("write size larger than buffer can fit");
      }

      int writeCursor = allocSegmentTable(writeSize, bufferColor.getColor());

      // encode content length.
      writeChunkSize(writeCursor, writeSize);

      writeCursor += 4;

      int end = writeCursor + writeSize;
      for (; writeCursor < end && writeCursor < bufferSize; writeCursor++) {
        buffer.put(writeCursor, (byte) inputStream.read());
      }

      if (writeCursor < end) {
        for (int i = 0; i < end - bufferSize; i++) {
          buffer.put(i, (byte) inputStream.read());
        }
      }
    }
    finally {
      if (haveLock) segmentTableLock.writeLock().unlock();
    }
    /**
     * Wake up any waiting readers.
     */
    bufferColor.wake();
  }


  @Override
  public void write(final int writeSize, final InputStream inputStream,
                    final BufferColor bufferColor, final BufferCallback callback) throws IOException {
    // try to obtain a lock. if it's taken, we can proceed anyways.
    // this is to prevent partial reads of the segmentMap by readers.
    final boolean haveLock = segmentTableLock.writeLock().tryLock();
    try {
      if (writeSize > bufferSize) {
        throw new RuntimeException("write size larger than buffer can fit");
      }

      int writeCursor = allocSegmentTable(writeSize, bufferColor.getColor());

      // encode content length.
      writeChunkSize(writeCursor, writeSize);

      writeCursor += 4;

      int end = writeCursor + writeSize;
      for (; writeCursor < end && writeCursor < bufferSize; writeCursor++) {
        buffer.put(writeCursor, (byte) inputStream.read());
      }

      if (writeCursor < end) {
        for (int i = 0; i < end - bufferSize; i++) {
          buffer.put(i, (byte) inputStream.read());
        }
      }

    }
    finally {
      if (haveLock) segmentTableLock.writeLock().unlock();
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

  private void get(int readSegment, final OutputStream outputStream, final BufferColor bufferColor
          , final BufferCallback callback) throws IOException {
    int readCursor = readSegment * segmentSize;

    if (readCursor == bufferSize) {
      readCursor = 0;
    }

    int readSize = readChunkSize(readCursor);

    readCursor += 4;

    final int endRead = readCursor + readSize;
    final int contiguousSegments = (readSize + 1) / segmentSize + 1;

    for (; readCursor < endRead && readCursor < bufferSize; readCursor++) {
      outputStream.write(callback.each(buffer.get(readCursor), outputStream));
    }

    if (readCursor < endRead) {
      int remaining = endRead - bufferSize;
      for (int i = 0; i < remaining; i++) {
        outputStream.write(callback.each(buffer.get(readCursor), outputStream));
      }
    }

    bufferColor.incrementSequence(contiguousSegments);
  }

  @Override
  public void read(final OutputStream outputStream, final BufferColor bufferColor) throws IOException {
    bufferColor.lock.lock();

    try {
      int readSegment;
      while ((readSegment = getNextSegment(bufferColor, bufferColor.getSequence())) != -1) {
        get(readSegment, outputStream, bufferColor);
      }
    }
    finally {
      bufferColor.lock.unlock();
    }
  }

  @Override
  public void read(final OutputStream outputStream, final BufferColor bufferColor, final BufferCallback callback) throws IOException {
    bufferColor.lock.lock();

    try {
      boolean succ = false;
      int readSegment;
      while ((readSegment = getNextSegment(bufferColor, bufferColor.getSequence())) != -1) {
        if (!succ) {
          callback.before(outputStream);
          succ = true;
        }
        get(readSegment, outputStream, bufferColor, callback);
      }
      if (succ) {
        callback.after(outputStream);
      }
    }
    finally {
      bufferColor.lock.unlock();
    }
  }


  @Override
  public void readWait(final OutputStream outputStream, final BufferColor bufferColor) throws InterruptedException, IOException {
    bufferColor.lock.lockInterruptibly();

    try {
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
          bufferColor.dataWaiting.await();
          awoken = true;
        }
        catch (InterruptedException e) {
          bufferColor.dataWaiting.signal();
          throw e;
        }
      }
    }
    finally {
      bufferColor.lock.unlock();
    }
  }

  @Override
  public void readWait(final TimeUnit unit, final long time,
                       final OutputStream outputStream, final BufferColor bufferColor) throws IOException, InterruptedException {
    bufferColor.lock.lockInterruptibly();

    try {
      // long seqExtent = sequenceNumber.get();
      long nanos = unit.toNanos(time);

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
          nanos = bufferColor.dataWaiting.awaitNanos(nanos);
          awoken = true;
        }
        catch (InterruptedException e) {
          bufferColor.dataWaiting.signal();
          throw e;
        }
      }
    }
    finally {
      bufferColor.lock.unlock();
    }
  }

  @Override
  public void readWaitNoFollow(TimeUnit unit, long time, OutputStream outputStream,
                               BufferColor bufferColor, BufferCallback callback) throws IOException, InterruptedException {
    bufferColor.lock.lockInterruptibly();

    try {
      long seqExtent = sequenceNumber.get();
      long nanos = unit.toNanos(time);

      boolean succ = false;
      boolean awoken = false;

      int seg;
      for (; ; ) {
        if ((seg = getNextSegment(bufferColor, bufferColor.getSequence(), seqExtent)) != -1) {
          if (!succ) {
            callback.before(outputStream);
            succ = true;
          }

          get(seg, outputStream, bufferColor, callback);

          awoken = true;
          continue;
        }

        if (awoken || nanos <= 0) {
          callback.after(outputStream);
          return;
        }

        try {
          nanos = bufferColor.dataWaiting.awaitNanos(nanos);
          callback.before(outputStream);
          seqExtent = sequenceNumber.get();
          succ = awoken = true;
        }
        catch (InterruptedException e) {
          bufferColor.dataWaiting.signal();
          throw e;
        }
      }
    }
    finally {
      bufferColor.lock.unlock();
    }
  }

  /**
   * Calculate the next segment.
   *
   * @param bufferColor
   * @param index
   * @return
   */
  private int getNextSegment(final BufferColor bufferColor, final long index) {
    int delta = 0;
    try {
      segmentTableLock.readLock().lock();
      final long seq = sequenceNumber.get();

      final int color = bufferColor.getColor();

      for (long i = index; i < seq; i++) {
        short seg = segmentMap[(int) i % segments];
        if (seg == color) {
          return (int) i % segments;
        }
        ++delta;
      }
    }
    finally {
      if (delta > 0)
        bufferColor.incrementSequence(delta);

      segmentTableLock.readLock().unlock();
    }

    return -1;
  }

  private int getNextSegment(final BufferColor bufferColor, final long index, final long max) {
    int delta = 0;
    try {
      segmentTableLock.readLock().lock();
      final long seq = sequenceNumber.get();

      final int color = bufferColor.getColor();

      for (long i = index; i < seq && i < max; i++) {
        short seg = segmentMap[(int) i % segments];
        if (seg == color) {
          return (int) i % segments;
        }
        ++delta;
      }
    }
    finally {
      if (delta > 0)
        bufferColor.incrementSequence(delta);

      segmentTableLock.readLock().unlock();
    }

    return -1;
  }

//  private int getNextSequence(final int neededSegments) {
//    return (int) sequenceNumber.getAndAdd(neededSegments) % segments;
//  }

  private void writeChunkSize(int position, int size) {
    buffer.putInt(position, size);
  }

  private int readChunkSize(int position) {

    return buffer.getInt(position);
  }

  public void dumpSegments() {

    System.out.println();
    System.out.println("SEGMENT DUMP");

    for (int i = 0; i < segmentMap.length && i < sequenceNumber.get(); i++) {
      StringBuilder build = new StringBuilder();
      int pos = i * segmentSize;
      int length = readChunkSize(pos);
      build.append("Segment " + i + " <color:" + (int) segmentMap[i] + ";length:" + length + ";location:" + pos + ">");
      pos += 4;

      byte[] buf = new byte[length];
      for (int x = 0; x < length; x++) {
        buf[x] = buffer.get(pos + x);
      }

      build.append("::'").append(new String(buf)).append("'");
      length += 4;

      if (length > segmentSize) {
        int segSpan = (length / segmentSize) + 1;

        i += segSpan;
      }

      System.out.println(build.toString());
    }
  }

  public List<String> dumpSegmentsAsList() {
    List<String> list = new ArrayList<String>();

    for (int i = 0; i < segmentMap.length && i < sequenceNumber.get(); i++) {
      int pos = i * segmentSize;
      int length = readChunkSize(pos);

      pos += 4;

      byte[] buf = new byte[length];
      for (int x = 0; x < length; x++) {
        buf[x] = buffer.get(pos + x);
      }

      list.add(new String(buf));

      length += 4;

      if (length > segmentSize) {
        int segSpan = (length / segmentSize) + 1;

        i += segSpan;
      }
    }
    return list;

  }
}
