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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

/**
 * A ring-based buffer implementation that provides contention-free writing of <i>1..n</i> colors. In this case,
 * colors refer to the unique attribute that separates one topic of data from another. Global data, which is visible
 * to all topics may also be written to this buffer.
 * </p>
 * Given the ring-buffer design, data is never de-allocated from the buffer when it is no longer needed. Instead,
 * it is assumed that old data will be consumed from the buffer before that space in the buffer is reclaimed.
 * <p/>
 * Since this is a multi-colored buffer, it has multiple tails -- one for each color.
 * <p/>
 * The amount of free space available in the buffer at any time is the delta between the head and maximum physical
 * extent of the buffer, plus the delta from the beginning of the physical buffer in memory to the closest tail.
 * </p>
 *
 * @author Mike Brock
 * @since Errai v2.0
 */
public class TransmissionBuffer implements Buffer {
  public static int DEFAULT_SEGMENT_SIZE = 1024 * 16;             /* 16 Kilobytes */
  public static int DEFAULT_BUFFER_SIZE = 2048;                   /* 2048 x 16kb = 32 Megabytes */

  public static int SEGMENT_HEADER_SIZE = 4;    /* to accomodate a 16-bit short */


  /**
   * The main buffer where the data is stored
   */
  // final ByteBuffer buffer;
  final byte[] buffer;

  /**
   * The segment map where allocation data is stored
   */
  final short[] segmentMap;

  /**
   * The absolute size (in bytes) of the buffer
   */
  final int bufferSize;

  /**
   * The size of an individual segment in the buffer
   */
  final int segmentSize;

  /**
   * The total number of allocable segments in the buffer
   */
  final int segments;

  /**
   * The internal write sequence number used by the writers to allocate write space within the buffer.
   */
  private final AtomicLong writeSequenceNumber = new AtomicLong() {
    public volatile long a1
            ,
            a2
            ,
            a3
            ,
            a4
            ,
            a5
            ,
            a6
            ,
            a7 = 7L;
  };

  /**
   * The visible head sequence number seen by the readers.
   */
  private final AtomicLong headSequence = new AtomicLong() {
    public volatile long a1
            ,
            a2
            ,
            a3
            ,
            a4
            ,
            a5
            ,
            a6
            ,
            a7 = 7L;
  };

  private static final Object memBoundary = new Object();

  private TransmissionBuffer(boolean directBuffer, int segmentSize, int segments) {
    // must pad segment size for size headers -- or the last segment may be odd-sized (that would not be good)
    this.segmentSize = segmentSize;
    this.bufferSize = segmentSize * segments;
    this.segments = segments;

    if (directBuffer) {
      // buffer = ByteBuffer.allocateDirect(bufferSize);
    }
    else {
      // buffer = ByteBuffer.wrap(new byte[bufferSize]);
    }

    buffer = new byte[bufferSize];

    segmentMap = new short[segments];

//    locks = new Object[segmentMap.length / 4];
//    for (int i = 0; i < locks.length; i++) {
//      locks[i] = new Object();
//    }
  }

  /**
   * Creates a transmission buffer with the default segment and buffer size, using a regular heap allocated buffer.
   *
   * @return an instance of the transmission buffer.
   */
  public static TransmissionBuffer create() {
    return new TransmissionBuffer(false, DEFAULT_SEGMENT_SIZE, DEFAULT_BUFFER_SIZE);
  }

  /**
   * Creates a transmission buffer with the default segment and buffer size, using a direct memory buffer.
   *
   * @return an instance of the tranmission buffer.
   */
  public static TransmissionBuffer createDirect() {
    return new TransmissionBuffer(true, DEFAULT_SEGMENT_SIZE, DEFAULT_BUFFER_SIZE);
  }

  /**
   * Creates a heap allocated transmission buffer with a specified segment size and segments. The resulting buffer
   * will be of size: <i>segmentSize * segments</i>.
   *
   * @param segmentSize
   * @param segments
   * @return
   */
  public static TransmissionBuffer create(int segmentSize, int segments) {
    return new TransmissionBuffer(false, segmentSize, segments);
  }

  /**
   * Creates a direct allocated transmission buffer with a custom segment size and segments. The resulting buffer
   * will be of size: <i>segmentSize * segments</i>.
   *
   * @param segmentSize
   * @param segments
   * @return
   */
  public static TransmissionBuffer createDirect(int segmentSize, int segments) {
    return new TransmissionBuffer(true, segmentSize, segments);
  }


  /**
   * Writes from an {@link InputStream} into the buffer using the specified {@param writeSize} to allocate space
   * in the buffer.
   *
   * @param writeSize   the size in bytes to be allocated.
   * @param inputStream the input stream to read into the buffer.
   * @param bufferColor the color of the data to be inserted.
   * @throws IOException
   */
  @Override
  public void write(final int writeSize, final InputStream inputStream, final BufferColor bufferColor) throws IOException {
    if (writeSize > bufferSize) {
      throw new RuntimeException("write size larger than buffer can fit");
    }
    int allocSize = ((writeSize + SEGMENT_HEADER_SIZE) / segmentSize) + 1;

    long head = headSequence.get();
    long writeHead = writeSequenceNumber.getAndAdd(allocSize);
    int seq = (int) writeHead % segments;


    int writeCursor = seq * segmentSize;

    // write the chunk size header for the data we're about to write
    writeChunkSize(writeCursor, writeSize);

    writeCursor += SEGMENT_HEADER_SIZE;

    // write the data to the buffer.
    int end = writeCursor + writeSize;
    int initialRead = end > bufferSize ? bufferSize : end;

    long newHead = writeHead + allocSize;

    /*
    * Allocate the segments to the this color
    */
    for (int i = 0; i < allocSize; i++) {
      segmentMap[(seq + i) % segments] = bufferColor.color;
    }

    for (; writeCursor < initialRead; writeCursor++) {
      buffer[writeCursor] = (byte) inputStream.read();
    }

    if (writeCursor < end) {
      for (int i = 0; i < end - bufferSize; i++) {
        buffer[i] = (byte) inputStream.read();
      }
    }


    // update the head sequence number.
    headSequence.set(newHead);

    // knock! knock! If there is a waiting reader on this color, wake it up.{
    bufferColor.wake();
  }


  /**
   * Reads all the available data of the specified color from the buffer into the provided <tt>OutputStream</tt>
   *
   * @param outputStream the <tt>OutputStream</tt> to read into.
   * @param bufferColor  the buffer color
   * @return returns an int representing the initial segment read from (note: not actually implemented yet)
   * @throws IOException
   */
  @Override
  public int read(final OutputStream outputStream, final BufferColor bufferColor) throws IOException {
    // obtain this color's read lock
    bufferColor.lock.lock();

    // get the current head position.
    int head = (int) headSequence.get() % segments;

    // get the tail position for the color.
    long read = bufferColor.sequence.get();
    long lastSeq = read;


    try {
      while ((read = readNextChunk(head, read, bufferColor, outputStream, null)) != -1)
        lastSeq = read;

      // move the tail sequence for this color up.
    }
    finally {
      // release the read lock on this color/
      bufferColor.sequence.set(lastSeq);
      bufferColor.lock.unlock();
    }
    return -1;
  }


  /**
   * Reads all the available data of the specified color from the buffer into the provided <tt>OutputStream</tt>
   * with a provided {@link BufferCallback}.
   *
   * @param outputStream the <tt>OutputStream</tt> to read into.
   * @param bufferColor  the buffer color
   * @param callback     a callback to be used during the read operation.
   * @return returns an int representing the initial segment read from (note: not actually implemented yet)
   * @throws IOException
   */
  @Override
  public int read(final OutputStream outputStream, final BufferColor bufferColor, final BufferCallback callback) throws IOException {
    return read(outputStream, bufferColor, callback, (int) headSequence.get() % segments);
  }

  /**
   * Reads all the available data of the specified color from the buffer into the provided <tt>OutputStream</tt>
   * with a provided {@link BufferCallback}.
   *
   * @param outputStream the <tt>OutputStream</tt> to read into.
   * @param bufferColor  the buffer color.
   * @param callback     a callback to be used during the read operation.
   * @param sequence     the sequence number to seek from in the buffer.
   * @return returns an int representing the initial segment read from (note: not actually implemented yet)
   * @throws IOException
   */
  @Override
  public int read(OutputStream outputStream, BufferColor bufferColor, BufferCallback callback, long sequence) throws IOException {
    // obtain this color's read lock
    bufferColor.lock.lock();

    try {
      // get the current head position.
      int head = (int) headSequence.get() % segments;

      // get the current tail position for this color.
      long read = bufferColor.sequence.get();

      long lastSeq = read;

      // if you need to do something before we write to output, do it now mr. callback.
      callback.before(outputStream);

      while ((read = readNextChunk(head, read, bufferColor, outputStream, callback)) != -1)
        lastSeq = read;

      // we're done writing, so do your after thing, mr. callback.
      callback.after(outputStream);

      bufferColor.sequence.set(lastSeq);
    }
    finally {
      // release the read lock on this color
      bufferColor.lock.unlock();
    }
    return -1;
  }


  /**
   * Reads from the buffer into the provided <tt>OutputStream</tt>, waiting indefinitely for data to arrive that is
   * relavent to the specified {@link BufferColor}
   *
   * @param outputStream the <tt>OutputStream</tt> to read into.
   * @param bufferColor  the buffer color
   * @return returns an int representing the initial segment read from (note: not actually implemented yet)
   * @throws IOException          an IOException is thrown if there is an inability to read from the buffer or write to
   *                              the specified <tt>OuputStream</tt>
   * @throws InterruptedException thrown if the monitor is interrupted while waiting to receive dta.
   */
  @Override
  public int readWait(final OutputStream outputStream, final BufferColor bufferColor) throws InterruptedException, IOException {
    bufferColor.lock.lockInterruptibly();

    try {
      boolean haveData = false;

      for (; ; ) {
        LockSupport.parkNanos(1);

        int head = (int) headSequence.get() % segments;
        long read = bufferColor.sequence.get();
        long lastSeq = read;

        while ((read = readNextChunk(head, read, bufferColor, outputStream, null)) != -1) {
          lastSeq = read;
          haveData = true;
        }

        // if we have data, lets return.
        if (haveData) {
          // update the tail sequence number for this color.
          outputStream.flush();
          bufferColor.sequence.set(lastSeq);
          return -1;
        }

        try {
          // lets wait for some data to come available
          bufferColor.dataWaiting.await();
        }
        catch (InterruptedException e) {
          // if there's another reader contending, they should probably know about this.
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
   * Reads from the buffer into the provided <tt>OutputStream</tt>, waiting up to the specified wait time for data
   * of the specified color to become available. Otherwise, the method returns without error, having read nothing.
   *
   * @param unit         the unit of time that will be used as the basis for waiting
   * @param time         the amount of time to wait in the specified units
   * @param outputStream the <tt>OutputStream</tt> to write to.
   * @param bufferColor  the buffer color
   * @return returns an int representing the initial segment read from (note: not actually implemented yet)
   * @throws IOException          an IOException is thrown if there is an inability to read from the buffer or write to
   *                              the specified <tt>OuputStream</tt>
   * @throws InterruptedException thrown if the monitor is interrupted while waiting to receive dta.
   */
  @Override
  public int readWait(final TimeUnit unit, final long time,
                      final OutputStream outputStream, final BufferColor bufferColor) throws IOException, InterruptedException {
    bufferColor.lock.lockInterruptibly();

    try {
      long readTail = bufferColor.sequence.get();
      long nanos = unit.toNanos(time);
      boolean haveData = false;

      for (; ; ) {
        LockSupport.parkNanos(1);

        int writeHead = (int) headSequence.get() % segments;
        long read = readTail;
        long lastSeq = read;

        while ((read = readNextChunk(writeHead, read, bufferColor, outputStream, null)) != -1) {
          lastSeq = read;
          haveData = true;
        }

        // return if data is ready to return or we're timed out.
        if (haveData || nanos <= 0) {
          bufferColor.sequence.set(lastSeq);
          outputStream.flush();
          return -1;
        }

        try {
          // wait around for some data.
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

  /**
   * Reads from the buffer into the provided <tt>OutputStream</tt>, waiting indefinitely for data
   * of the specified color to become available. Otherwise, the method returns without error, having read nothing.
   *
   * @param outputStream the <tt>OutputStream</tt> to write to.
   * @param bufferColor  the buffer color
   * @return returns an int representing the initial segment read from (note: not actually implemented yet)
   * @throws IOException          an IOException is thrown if there is an inability to read from the buffer or write to
   *                              the specified <tt>OuputStream</tt>
   * @throws InterruptedException thrown if the monitor is interrupted while waiting to receive dta.
   */
  @Override
  public int readWait(OutputStream outputStream, BufferColor bufferColor, BufferCallback callback) throws IOException, InterruptedException {
    return readWait(TimeUnit.NANOSECONDS, -1, outputStream, bufferColor, callback);
  }


  /**
   * Reads from the buffer into the provided <tt>OutputStream</tt>, waiting indefinitely for data
   * of the specified color to become available with the provided callback. Otherwise, the method returns
   * without error, having read nothing.
   *
   * @param outputStream the <tt>OutputStream</tt> to write to.
   * @param bufferColor  the buffer color
   * @return returns an int representing the initial segment read from (note: not actually implemented yet)
   * @throws IOException          an IOException is thrown if there is an inability to read from the buffer or write to
   *                              the specified <tt>OuputStream</tt>
   * @throws InterruptedException thrown if the monitor is interrupted while waiting to receive dta.
   */
  @Override
  public int readWait(TimeUnit unit, long time, OutputStream outputStream, final BufferColor bufferColor, BufferCallback callback) throws IOException, InterruptedException {
    bufferColor.lock.lockInterruptibly();

    try {
      long readTail = bufferColor.sequence.get();
      long nanos = time == -1 ? 1 : unit.toNanos(time);
      boolean havaData = false;

      callback.before(outputStream);

      for (; ; ) {
        LockSupport.parkNanos(1);

        int writeHead = (int) headSequence.get() % segments;
        long read = readTail;
        long lastSeq = read;

        while ((read = readNextChunk(writeHead, read, bufferColor, outputStream, callback)) != -1) {
          lastSeq = read;
          havaData = true;
        }

        // return if data is ready to return or we're timed out.
        if (havaData || nanos <= 0) {
          bufferColor.sequence.set(lastSeq);
          callback.after(outputStream);
          outputStream.flush();
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

  @Override
  public int getHeadPositionBytes() {
    return ((int) headSequence.get() % segments) * segmentSize;
  }

  @Override
  public int getBufferSize() {
    return bufferSize;
  }

  @Override
  public int getTotalSegments() {
    return segments;
  }

  @Override
  public int getSegmentSize() {
    return segmentSize;
  }


  /**
   * Returns the next segment containing data for the specified {@param bufferColor}, up to the specified
   * {@param head} position, from the specified {@param segment} position.
   *
   * @param bufferColor the buffer color
   * @param head        the head position to seek up to
   * @param segment     the segment to seek from
   * @return returns an int representing the initial segment read from (note: not actually implemented yet)
   */
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
      // Handle loop-around at end of buffer if head is behind us.
      for (int i = segment; i < segments; i++) {
        short seg = segmentMap[i];
        if (seg == color || seg == Short.MIN_VALUE) {
          return i;
        }
      }

      // start from the beginning of the buffer and scan up to the head position.
      for (int i = 0; i < head; i++) {
        short seg = segmentMap[i];
        if (seg == color || seg == Short.MIN_VALUE) {
          return i;
        }
      }
    }

    return -1;
  }

  /**
   * Read in the next data chunk up to the specified {@param head} position, from the specified {@param sequence},
   * for the specifed {@param color} into the provided <tt>OutputStream</tt>.
   * <p/>
   * This method accepts an optional {@link BufferCallback}. Null can be passed if no callback is needed.
   *
   * @param head         the head position to seek to.
   * @param sequence     the sequence position to seek from
   * @param color        the data color for the buffer.
   * @param outputStream the <tt>OuputStream</tt> to read into.
   * @param callback     an optional {@link BufferCallback}.
   * @return returns the segment position after reading + 1.
   * @throws IOException thrown if data cannot be read from the buffer or written to the OutputStream.
   */
  private long readNextChunk(final int head, final long sequence, final BufferColor color,
                             final OutputStream outputStream, final BufferCallback callback) throws IOException {
    int segmentToRead = getNextSegment(color, head, (int) sequence % segments);
    if (segmentToRead != -1) {

      int readCursor = segmentToRead * segmentSize;

      if (readCursor == bufferSize) {
        readCursor = 0;
      }

      final int readSize = readChunkSize(readCursor);

      readCursor += SEGMENT_HEADER_SIZE;

      final int endRead = readCursor + readSize;
      final int maxInitialRead;

      if (endRead < bufferSize) {
        maxInitialRead = endRead;
      }
      else {
        maxInitialRead = bufferSize;
      }

      if (callback == null) {
        for (; readCursor < maxInitialRead; readCursor++) {
          outputStream.write(buffer[readCursor]);
        }

        if (readCursor < endRead) {
          int remaining = endRead - bufferSize;
          for (int i = 0; i < remaining; i++) {
            outputStream.write(buffer[i]);
          }
        }
      }
      else {
        for (; readCursor < maxInitialRead; readCursor++) {
          outputStream.write(callback.each(buffer[readCursor], outputStream));
        }

        if (readCursor < endRead) {
          int remaining = endRead - bufferSize;
          for (int i = 0; i < remaining; i++) {
            outputStream.write(callback.each(buffer[readCursor], outputStream));
          }
        }
      }
      return segmentToRead + ((readSize + SEGMENT_HEADER_SIZE) / segmentSize) + 1;
    }
    else {
      return -1;
    }
  }

  /**
   * Read in the size of the chunk.
   *
   * @param position the position in the buffer to read the data.
   * @return the size in bytes.
   */
  private int readChunkSize(int position) {
    return (((((int) buffer[position + 3]) & 0xFF) << 32) +
            ((((int) buffer[position + 2]) & 0xFF) << 40) +
            ((((int) buffer[position + 1]) & 0xFF) << 48) +
            ((((int) buffer[position]) & 0xFF) << 56));
  }

  private void writeChunkSize(int position, int size) {
    buffer[position++] = (byte) ((size >> 24) & 0xFF);
    buffer[position++] = (byte) ((size >> 16) & 0xFF);
    buffer[position++] = (byte) ((size >> 8) & 0xFF);
    buffer[position] = (byte) (size & 0xFF);

  }


  public void dumpSegments(PrintWriter writer) {
    writer.println();
    writer.println("SEGMENT DUMP");

    for (int i = 0; i < segmentMap.length && i < headSequence.get(); i++) {
      StringBuilder build = new StringBuilder();
      int pos = i * segmentSize;
      int length = readChunkSize(pos);
      build.append("Segment " + i + " <color:" + (int) segmentMap[i] + ";length:" + length + ";location:" + pos + ">");
      pos += SEGMENT_HEADER_SIZE;

      byte[] buf = new byte[length];
      for (int x = 0; x < length; x++) {
        buf[x] = buffer[pos + x];
      }

      build.append("::'").append(new String(buf)).append("'");
      length += SEGMENT_HEADER_SIZE;

      if (length > segmentSize) {
        int segSpan = (length / segmentSize) + 1;

        i += segSpan;
      }

      writer.println(build.toString());
    }
  }

  public void rawDump(OutputStream stream) throws IOException {
//    ByteBuffer buf = buffer.duplicate();
//    buf.rewind();
//
//    while (buf.hasRemaining()) stream.write(buf.get());
  }

  @SuppressWarnings("UnusedDeclaration")
  public List<String> dumpSegmentsAsList() {
    List<String> list = new ArrayList<String>();

    for (int i = 0; i < segmentMap.length && i < headSequence.get(); i++) {
      int pos = i * segmentSize;
      int length = readChunkSize(pos);

      pos += SEGMENT_HEADER_SIZE;

      byte[] buf = new byte[length];
      for (int x = 0; x < length; x++) {
        buf[x] = buffer[pos + x];
      }

      list.add(new String(buf));

      length += SEGMENT_HEADER_SIZE;

      if (length > segmentSize) {
        int segSpan = (length / segmentSize) + 1;

        i += segSpan;
      }
    }
    return list;
  }
}
