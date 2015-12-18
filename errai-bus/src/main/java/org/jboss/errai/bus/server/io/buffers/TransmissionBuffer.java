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

package org.jboss.errai.bus.server.io.buffers;

import org.jboss.errai.bus.server.io.ByteWriteAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

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
 * @see BufferColor
 * @see BufferFilter
 * @since Errai v2.0
 */
public class TransmissionBuffer implements Buffer {
  public static final long STARTING_SEQUENCE = 0;

  public static final int DEFAULT_SEGMENT_SIZE = 1024 * 16;              /* 16 Kilobytes */
  private static final int DEFAULT_BUFFER_SIZE = 2048;                   /* 2048 x 16kb = 32 Megabytes */

  private static final int SEGMENT_HEADER_SIZE = 4;                      /* to accommodate a 32-bit integer  */

  /**
   * The main buffer where the data is stored
   */
  private final ByteBuffer _buffer;

  /**
   * The segment map where allocation data is stored
   */
  private final short[] segmentMap;

  /**
   * The absolute size (in bytes) of the buffer
   */
  private final int bufferSize;

  /**
   * The size of an individual segment in the buffer
   */
  private final int segmentSize;

  /**
   * The total number of allocable segments in the buffer
   */
  private final int segments;

  /**
   * The internal write sequence number used by the writers to allocate write space within the buffer.
   */
  private final AtomicLong writeSequenceNumber = new AtomicLong(STARTING_SEQUENCE) {
    @SuppressWarnings("UnusedDeclaration") public volatile long a1
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
  private volatile long headSequence = STARTING_SEQUENCE;

  private TransmissionBuffer(final boolean directBuffer, final int segmentSize, final int segments) {
    this.segmentSize = segmentSize;
    this.bufferSize = segmentSize * segments;
    this.segments = segments;

    if (directBuffer) {
      this._buffer = ByteBuffer.allocateDirect(bufferSize);
    }
    else {
      this._buffer = ByteBuffer.allocate(bufferSize);
    }

    _buffer.put(0, (byte) 0);

    segmentMap = new short[segments];
    segmentMap[0] = (short) 0;
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
   *     the size of individual segments
   * @param segments
   *     the total number of segments
   *
   * @return an instance of the transmission buffer
   */
  public static TransmissionBuffer create(final int segmentSize, final int segments) {
    return new TransmissionBuffer(false, segmentSize, segments);
  }

  /**
   * Creates a direct allocated transmission buffer with a custom segment size and segments. The resulting buffer
   * will be of size: <i>segmentSize * segments</i>.
   *
   * @param segmentSize
   *     the size of the individual segments
   * @param segments
   *     the total number of segments
   *
   * @return an instance of the transmission buffer
   */
  public static TransmissionBuffer createDirect(final int segmentSize, final int segments) {
    return new TransmissionBuffer(true, segmentSize, segments);
  }

  /**
   * Writes from the {@link InputStream} into the buffer. Space is allocated and the data expected to be written
   * by checking the {@link java.io.InputStream#available()} value.
   *
   * @param inputStream
   *     the input stream to read into the buffer.
   * @param bufferColor
   *     the color of the data to be inserted.
   *
   * @throws IOException
   */
  @Override
  public void write(final InputStream inputStream, final BufferColor bufferColor) throws IOException {
    write(inputStream.available(), inputStream, bufferColor);
  }

  /**
   * Writes from an {@link InputStream} into the buffer using the specified {@param writeSize} to allocate space
   * in the buffer.
   *
   * @param writeSize
   *     the size in bytes to be allocated.
   * @param inputStream
   *     the input stream to read into the buffer.
   * @param bufferColor
   *     the color of the data to be inserted.
   *
   * @throws IOException
   */
  @Override
  public void write(final int writeSize,
                    final InputStream inputStream,
                    final BufferColor bufferColor) throws IOException {

    if (writeSize > bufferSize) {
      throw new IOException("write size larger than buffer can fit");
    }

    final ReentrantLock lock = bufferColor.lock;
    lock.lock();
    try {
      final int allocSize = ((writeSize + SEGMENT_HEADER_SIZE) / segmentSize) + 1;
      final long writeHead = writeSequenceNumber.getAndAdd(allocSize);
      final int seq = (int) writeHead % segments;

      int writeCursor = seq * segmentSize;

      // write the chunk size header for the data we're about to write
      writeChunkSize(writeCursor, writeSize);

      final int end = (writeCursor += SEGMENT_HEADER_SIZE) + writeSize;
      final int initialRead = end > bufferSize ? bufferSize : end;

      /*
      * Allocate the segments to the this color
      */
      final short color = bufferColor.color;
      for (int i = 0; i < allocSize; i++) {
        segmentMap[((seq + i) % segments)] = color;
      }

      for (; writeCursor < initialRead; writeCursor++) {
        _buffer.put(writeCursor, (byte) inputStream.read());
      }

      if (writeCursor < end) {
        for (int i = 0; i < end - bufferSize; i++) {
          _buffer.put(i, (byte) inputStream.read());
        }
      }

      headSequence = writeHead + allocSize;
    }
    finally {
      bufferColor.wake();
      lock.unlock();
    }
  }

  /**
   * Reads all the available data of the specified color from the buffer into the provided <tt>OutputStream</tt>
   *
   * @param outputStream
   *     the <tt>OutputStream</tt> to read into.
   * @param bufferColor
   *     the buffer color
   *
   * @return returns a boolean indicating whether or not the cursor advanced.
   *
   * @throws IOException
   */
  @Override
  public boolean read(final ByteWriteAdapter outputStream, final BufferColor bufferColor) throws IOException {
    // obtain this color's read lock
    bufferColor.lock.lock();

    // get the current head position.
    final long writeHead = headSequence;

    // get the tail position for the color.
    long read = bufferColor.sequence.get();
    long lastSeq = read;

    // checkOverflow(read);

    try {
      while ((read = readNextChunk(writeHead, read, bufferColor, outputStream, null)) != -1)
        lastSeq = read;

      return lastSeq != read;
    }
    finally {
      // move the tail sequence for this color up.
      if (lastSeq != -1)
        bufferColor.sequence.set(lastSeq);

      // release the read lock on this color/
      bufferColor.lock.unlock();
    }
  }

  /**
   * Reads all the available data of the specified color from the buffer into the provided <tt>OutputStream</tt>
   * with a provided {@link BufferFilter}.
   *
   * @param outputStream
   *     the <tt>OutputStream</tt> to read into.
   * @param bufferColor
   *     the buffer color
   * @param callback
   *     a callback to be used during the read operation.
   *
   * @return returns a boolean indicating whether or not the cursor advanced.
   *
   * @throws IOException
   */
  @Override
  public boolean read(final ByteWriteAdapter outputStream,
                      final BufferColor bufferColor,
                      final BufferFilter callback) throws IOException {

    return read(outputStream, bufferColor, callback, (int) headSequence % segments);
  }

  /**
   * Reads all the available data of the specified color from the buffer into the provided <tt>OutputStream</tt>
   * with a provided {@link BufferFilter}.
   *
   * @param outputStream
   *     the <tt>OutputStream</tt> to read into.
   * @param bufferColor
   *     the buffer color.
   * @param callback
   *     a callback to be used during the read operation.
   * @param sequence
   *     the sequence number to seek from in the buffer.
   *
   * @return returns a boolean indicating whether or not the cursor advanced.
   *
   * @throws IOException
   */
  @Override
  public boolean read(final ByteWriteAdapter outputStream,
                      final BufferColor bufferColor,
                      final BufferFilter callback,
                      final long sequence) throws IOException {

    // attempt to obtain this color's read lock
    if (bufferColor.lock.tryLock()) {
      try {
        // get the current head position.
        final long writeHead = headSequence;

        // get the current tail position for this color.
        long read = bufferColor.sequence.get();

        // checkOverflow(read);

        long lastSeq = read;

        // if you need to do something before we write to output, do it now mr. callback.
        callback.before(outputStream);

        while ((read = readNextChunk(writeHead, read, bufferColor, outputStream, callback)) != -1)
          lastSeq = read;

        // we're done writing, so do your after thing, mr. callback.
        callback.after(outputStream);

        if (lastSeq != -1)
          bufferColor.sequence.set(lastSeq);

        return read != lastSeq;
      }
      finally {
        // release the read lock on this color
        bufferColor.lock.unlock();
      }
    }
    return false;
  }

  /**
   * Reads from the buffer into the provided <tt>OutputStream</tt>, waiting indefinitely for data to arrive that is
   * relavent to the specified {@link BufferColor}
   *
   * @param outputStream
   *     the <tt>OutputStream</tt> to read into.
   * @param bufferColor
   *     the buffer color
   *
   * @return returns a boolean indicating whether or not the cursor advanced.
   *
   * @throws IOException
   *     an IOException is thrown if there is an inability to read from the buffer or write to
   *     the specified <tt>OuputStream</tt>
   * @throws InterruptedException
   *     thrown if the monitor is interrupted while waiting to receive dta.
   */
  @Override
  public boolean readWait(final ByteWriteAdapter outputStream,
                          final BufferColor bufferColor) throws InterruptedException, IOException {
    bufferColor.lock.lockInterruptibly();

    try {
      for (; ; ) {
        long read = bufferColor.sequence.get();
        //checkOverflow(read);
        long lastRead = -1;

        while ((read = readNextChunk(headSequence, read, bufferColor, outputStream, null)) != -1) {
          lastRead = read;
        }

        if (lastRead != -1) {
          bufferColor.sequence.set(lastRead);
          return true;
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
   * @param unit
   *     the unit of time that will be used as the basis for waiting
   * @param time
   *     the amount of time to wait in the specified units
   * @param outputStream
   *     the <tt>OutputStream</tt> to write to.
   * @param bufferColor
   *     the buffer color
   *
   * @return returns a boolean indicating whether or not the cursor advanced.
   *
   * @throws IOException
   *     an IOException is thrown if there is an inability to read from the buffer or write to
   *     the specified <tt>OuputStream</tt>
   * @throws InterruptedException
   *     thrown if the monitor is interrupted while waiting to receive dta.
   */
  @Override
  public boolean readWait(final TimeUnit unit,
                          final long time,
                          final ByteWriteAdapter outputStream,
                          final BufferColor bufferColor) throws IOException, InterruptedException {
    final ReentrantLock lock = bufferColor.getLock();
    lock.lockInterruptibly();

    long nanos = unit.toNanos(time);

    try {
      for (; ; ) {
        long read = bufferColor.sequence.get();
        //checkOverflow(read);
        long lastRead = -1;

        while ((read = readNextChunk(headSequence, read, bufferColor, outputStream, null)) != -1) {
          lastRead = read;
        }

        // return if data is ready to return or we're timed out.
        if (nanos <= 0 || lastRead != -1) {
          if (lastRead != -1) {
            bufferColor.sequence.set(lastRead);
          }
          return lastRead != read;
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
   * @param outputStream
   *     the <tt>OutputStream</tt> to write to.
   * @param bufferColor
   *     the buffer color
   *
   * @return returns a boolean indicating whether or not the cursor advanced.
   *
   * @throws IOException
   *     an IOException is thrown if there is an inability to read from the buffer or write to
   *     the specified <tt>OutputStream</tt>
   * @throws InterruptedException
   *     thrown if the monitor is interrupted while waiting to receive dta.
   */
  @Override
  public boolean readWait(final ByteWriteAdapter outputStream,
                          final BufferColor bufferColor,
                          final BufferFilter callback) throws IOException, InterruptedException {
    return readWait(TimeUnit.NANOSECONDS, -1, outputStream, bufferColor, callback);
  }

  /**
   * Reads from the buffer into the provided <tt>OutputStream</tt>, waiting indefinitely for data
   * of the specified color to become available with the provided callback. Otherwise, the method returns
   * without error, having read nothing.
   *
   * @param outputStream
   *     the <tt>OutputStream</tt> to write to.
   * @param bufferColor
   *     the buffer color
   *
   * @return returns a boolean indicating whether or not the cursor advanced.
   *
   * @throws IOException
   *     an IOException is thrown if there is an inability to read from the buffer or write to
   *     the specified <tt>OutputStream</tt>
   * @throws InterruptedException
   *     thrown if the monitor is interrupted while waiting to receive dta.
   */
  @Override
  public boolean readWait(final TimeUnit unit,
                          final long time,
                          final ByteWriteAdapter outputStream,
                          final BufferColor bufferColor,
                          final BufferFilter callback) throws IOException, InterruptedException {
    final ReentrantLock lock = bufferColor.lock;
    lock.lockInterruptibly();

    long nanos = time == -1 ? 1 : unit.toNanos(time);

    try {
      callback.before(outputStream);

      for (; ; ) {
        long read = bufferColor.sequence.get();
        //checkOverflow(read);
        long lastRead = -1;
        while ((read = readNextChunk(headSequence, read, bufferColor, outputStream, callback)) != -1) {
          lastRead = read;
        }

        // return if data is ready to return or we're timed out.
        if (lastRead != -1 || nanos <= 0) {
          if (lastRead != -1) {
            bufferColor.sequence.set(lastRead);
          }
          callback.after(outputStream);
          return lastRead != read;
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
      lock.unlock();
    }
  }

  @Override
  public long getHeadSequence() {
    return headSequence;
  }

  @Override
  public int getHeadPositionBytes() {
    return ((int) headSequence % segments) * segmentSize;
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
   * @param bufferColor
   *     the buffer color
   * @param headSeq
   *     the sequence position to seek up to
   * @param colorSeq
   *     the sequence position to seek from.
   *
   * @return returns an long representing the initial sequence to read from
   */
  private long getNextSegment(final BufferColor bufferColor, final long headSeq, long colorSeq) {
    for (final int color = bufferColor.getColor(); colorSeq < headSeq; colorSeq++) {
      final short seg = segmentMap[((int) colorSeq % segments)];

      if (seg == color || seg == Short.MIN_VALUE) {
        return colorSeq;
      }
    }
    return -1;
  }

  /**
   * Read in the next data chunk up to the specified {@param head} position, from the specified {@param sequence},
   * for the specifed {@param color} into the provided <tt>OutputStream</tt>.
   * <p/>
   * This method accepts an optional {@link BufferFilter}. Null can be passed if no callback is needed.
   *
   * @param head
   *     the head position to seek to.
   * @param sequence
   *     the sequence position to seek from
   * @param color
   *     the data color for the buffer.
   * @param outputStream
   *     the <tt>OutputStream</tt> to read into.
   * @param callback
   *     an optional {@link BufferFilter}.
   *
   * @return returns the segment position after reading + 1.
   *
   * @throws IOException
   *     thrown if data cannot be read from the buffer or written to the OutputStream.
   */
  private long readNextChunk(final long head,
                             final long sequence,
                             final BufferColor color,
                             final ByteWriteAdapter outputStream,
                             final BufferFilter callback) throws IOException {

    final long sequenceToRead = getNextSegment(color, head, sequence);
    if (sequenceToRead != -1) {
      int readCursor = ((int) sequenceToRead % segments) * segmentSize;

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
          outputStream.write(_buffer.get(readCursor));
        }

        if (readCursor < endRead) {
          final int remaining = endRead - bufferSize;
          for (int i = 0; i < remaining; i++) {
            outputStream.write(_buffer.get(i));
          }
        }
      }
      else {
        for (; readCursor < maxInitialRead; readCursor++) {
          outputStream.write(callback.each(_buffer.get(readCursor), outputStream));
        }

        if (readCursor < endRead) {
          final int remaining = endRead - bufferSize;
          for (int i = 0; i < remaining; i++) {
            outputStream.write(callback.each(_buffer.get(i), outputStream));
          }
        }
      }
      return sequenceToRead + ((readSize + SEGMENT_HEADER_SIZE) / segmentSize) + 1;
    }
    else {
      return -1;
    }
  }

  /**
   * Read in the size of the chunk.
   *
   * @param position
   *     the position in the buffer to read the data.
   *
   * @return the size in bytes.
   */
  private int readChunkSize(final int position) {
    return ((((int) _buffer.get(position + 3)) & 0xFF)) +
        ((((int) _buffer.get(position + 2)) & 0xFF) << 8) +
        ((((int) _buffer.get(position + 1)) & 0xFF) << 16) +
        ((((int) _buffer.get(position)) & 0xFF) << 24);
  }

  private void writeChunkSize(final int position, final int size) {
    _buffer.put(position, (byte) ((size >> 24) & 0xFF));
    _buffer.put(position + 1, (byte) ((size >> 16) & 0xFF));
    _buffer.put(position + 2, (byte) ((size >> 8) & 0xFF));
    _buffer.put(position + 3, (byte) (size & 0xFF));
  }

  /**
   * Clear the current buffer.
   */
  public void clear() {
    _buffer.clear();
  }

  public void dumpSegments(final PrintWriter writer) {
    writer.println();
    writer.println("SEGMENT DUMP");

    for (int i = 0; i < segmentMap.length && i < headSequence; i++) {
      final StringBuilder build = new StringBuilder();
      int pos = i * segmentSize;
      int length = readChunkSize(pos);
      build.append("Segment ").append(i).append(" <color:")
          .append((int) segmentMap[i]).append(";length:").append(length)
          .append(";location:").append(pos).append(">");

      pos += SEGMENT_HEADER_SIZE;

      final byte[] buf = new byte[length];

      final ByteBuffer dupBuf = _buffer.duplicate();
      dupBuf.position(pos);
      dupBuf.put(buf, 0, length);

      build.append("::'").append(new String(buf)).append("'");
      length += SEGMENT_HEADER_SIZE;

      if (length > segmentSize) {
        final int segSpan = (length / segmentSize) + 1;

        i += segSpan;
      }

      writer.println(build.toString());
    }
  }

  @SuppressWarnings("UnusedDeclaration")
  public List<String> dumpSegmentsAsList() {
    final List<String> list = new ArrayList<String>();

    for (int i = 0; i < segmentMap.length && i < headSequence; i++) {
      int pos = i * segmentSize;
      int length = readChunkSize(pos);

      pos += SEGMENT_HEADER_SIZE;

      final byte[] buf = new byte[length];

      final ByteBuffer dupBuf = _buffer.duplicate();
      dupBuf.position(pos);
      dupBuf.put(buf, 0, length);

      list.add(new String(buf));

      length += SEGMENT_HEADER_SIZE;

      if (length > segmentSize) {
        final int segSpan = (length / segmentSize) + 1;

        i += segSpan;
      }
    }
    return list;
  }
}
