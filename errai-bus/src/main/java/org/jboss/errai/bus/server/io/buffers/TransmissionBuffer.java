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

  private final AtomicLong writeSequenceNumber = new AtomicLong();
  private final AtomicLong headSequence = new AtomicLong();

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

    int seq = (int) writeSequenceNumber.getAndAdd(allocSize) % segments;
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
    //  final boolean haveLock = bufferColor.segmentTableLog.writeLock().tryLock();
    //   try {
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
//    }
//    finally {
//      if (haveLock) bufferColor.segmentTableLog.writeLock().unlock();
//    }
    /**
     * Wake up any waiting readers.
     */

    headSequence.set(writeSequenceNumber.get());
    bufferColor.wake();
  }


  @Override
  public void write(final int writeSize, final InputStream inputStream,
                    final BufferColor bufferColor, final BufferCallback callback) throws IOException {
    // try to obtain a lock. if it's taken, we can proceed anyways.
    // this is to prevent partial reads of the segmentMap by readers.
//    final boolean haveLock = bufferColor.segmentTableLog.writeLock().tryLock();
//    try {
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

    /**
     * Wake up any waiting readers.
     */
    headSequence.set(writeSequenceNumber.get());
    bufferColor.wakeLazy();
  }


  @Override
  public int read(final OutputStream outputStream, final BufferColor bufferColor) throws IOException {
    bufferColor.lock.lock();

    try {
      int head = getHead();
      long read = bufferColor.getSequence();
      long lastSeq = read;

      while ((read = readNextChunk(head, read, bufferColor, outputStream, null)) != -1)
        lastSeq = read;

      bufferColor.setSequence(lastSeq);
    }
    finally {
      bufferColor.lock.unlock();
    }
    return -1;
  }

  @Override
  public int read(final OutputStream outputStream, final BufferColor bufferColor, final BufferCallback callback) throws IOException {
    return read(outputStream, bufferColor, callback, bufferColor.getSequence());
  }

  @Override
  public int read(OutputStream outputStream, BufferColor bufferColor, BufferCallback callback, long sequence) throws IOException {
    bufferColor.lock.lock();

    try {
      int head = getHead();
      long read = bufferColor.getSequence();
      long lastSeq = read;

      callback.before(outputStream);

      while ((read = readNextChunk(head, read, bufferColor, outputStream, callback)) != -1)
        lastSeq = read;

      callback.after(outputStream);

      bufferColor.setSequence(lastSeq);
    }
    finally {
      bufferColor.lock.unlock();
    }
    return -1;
  }

  @Override
  public int readWait(final OutputStream outputStream, final BufferColor bufferColor) throws InterruptedException, IOException {
    bufferColor.lock.lockInterruptibly();

    try {
      boolean haveData = false;

      int head = getHead();
      long read = bufferColor.getSequence();
      long lastSeq = read;

      for (; ; ) {
        while ((read = readNextChunk(head, read, bufferColor, outputStream, null)) != -1) {
          lastSeq = read;
          haveData = true;
        }

        if (haveData) {
          bufferColor.setSequence(lastSeq);
          return -1;
        }

        try {
          bufferColor.dataWaiting.await();
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
  public int readWait(final TimeUnit unit, final long time,
                      final OutputStream outputStream, final BufferColor bufferColor) throws IOException, InterruptedException {
    bufferColor.lock.lockInterruptibly();

    try {

      long nanos = unit.toNanos(time);
      boolean haveData = false;

      for (; ; ) {
        int head = getHead();
        long read = bufferColor.getSequence();
        long lastSeq = read;

        while ((read = readNextChunk(head, read, bufferColor, outputStream, null)) != -1) {
          lastSeq = read;
          haveData = true;
        }

        // return if data is ready to return or we're timed out.
        if (haveData || nanos <= 0) {
          bufferColor.setSequence(lastSeq);
          return -1;
        }

        try {
          nanos = bufferColor.dataWaiting.awaitNanos(nanos);
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
  public int readWait(OutputStream outputStream, BufferColor bufferColor, BufferCallback callback) throws IOException, InterruptedException {
    return readWait(TimeUnit.NANOSECONDS, -1, outputStream, bufferColor, callback);
  }

  @Override
  public int readWait(TimeUnit unit, long time, OutputStream outputStream, BufferColor bufferColor, BufferCallback callback) throws IOException, InterruptedException {
    bufferColor.lock.lockInterruptibly();

    try {
      long nanos = time == -1 ? 1 : unit.toNanos(time);
      boolean havaData = false;

      callback.before(outputStream);

      for (; ; ) {
        int head = getHead();
        long read = bufferColor.getSequence();
        long lastSeq = read;

        while ((read = readNextChunk(head, read, bufferColor, outputStream, callback)) != -1) {
          lastSeq = read;
          havaData = true;
        }

        // return if data is ready to return or we're timed out.
        if (havaData || nanos <= 0) {
          bufferColor.setSequence(lastSeq);
          callback.after(outputStream);
          return -1;
        }

        try {
          if (time == -1) {
            bufferColor.dataWaiting.await();
          }
          else {
            nanos = bufferColor.dataWaiting.awaitNanos(nanos);
          }
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

  private int getNextSegment(final BufferColor bufferColor, final int head, final int segment) {
    final int color = bufferColor.getColor();

    if (head > segment) {
      for (int i = segment; i < head; i++) {
        short seg = segmentMap[i];
        if (seg == color || seg == Short.MIN_VALUE) {
          return i;
        }
      }
    }
    else if (head < segment) {
      /**
       * Handle loop-around at end of buffer if head is behind us.
       */
      for (int i = segment; i < segments; i++) {
        short seg = segmentMap[i];
        if (seg == color || seg == Short.MIN_VALUE) {
          return i;
        }
      }

      for (int i = 0; i < head; i++) {
        short seg = segmentMap[i];
        if (seg == color || seg == Short.MIN_VALUE) {
          return i;
        }
      }
    }

    return -1;
  }

  private long readNextChunk(final int head, final long sequence, final BufferColor color,
                             final OutputStream outputStream, final BufferCallback callback) throws IOException {
    int segmentToRead = getNextSegment(color, head, (int) sequence % segments);
    if (segmentToRead != -1) {
      int readCursor = segmentToRead * segmentSize;

      if (readCursor == bufferSize) {
        readCursor = 0;
      }

      final int readSize = readChunkSize(readCursor);

      readCursor += 4;

      final int endRead = readCursor + readSize;

      if (callback == null) {
        for (; readCursor < endRead && readCursor < bufferSize; readCursor++) {
          outputStream.write(buffer.get(readCursor));
        }

        if (readCursor < endRead) {
          int remaining = endRead - bufferSize;
          for (int i = 0; i < remaining; i++) {
            outputStream.write(buffer.get(i));
          }
        }
      }
      else {
        for (; readCursor < endRead && readCursor < bufferSize; readCursor++) {
          outputStream.write(callback.each(buffer.get(readCursor), outputStream));
        }

        if (readCursor < endRead) {
          int remaining = endRead - bufferSize;
          for (int i = 0; i < remaining; i++) {
            outputStream.write(callback.each(buffer.get(i), outputStream));
          }
        }
      }

      return segmentToRead + ((readSize + 4) / segmentSize + 1);
    }
    else {
      return -1;
    }
  }

  private void writeChunkSize(int position, int size) {
    buffer.putInt(position, size);
  }

  private int readChunkSize(int position) {
    return buffer.getInt(position);
  }

  private int getHead() {
    return (int) headSequence.get() % segments;
  }

  public void dumpSegments() {

    System.out.println();
    System.out.println("SEGMENT DUMP");

    for (int i = 0; i < segmentMap.length && i < headSequence.get(); i++) {
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

    for (int i = 0; i < segmentMap.length && i < headSequence.get(); i++) {
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
