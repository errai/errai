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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Defines a buffer color, which is a unique identifier for data within a {@link TransmissionBuffer}.
 *
 * @author Mike Brock
 */
public class BufferColor {
  // an automatic counter to ensure each buffer has a unique color
  private static final AtomicInteger bufferColorCounter = new AtomicInteger();
  private static final BufferColor allBuffersColor = new BufferColor(Short.MIN_VALUE);

  /**
   * The current tail position for this buffer color.
   */
  final AtomicLong sequence = new AtomicLong(TransmissionBuffer.STARTING_SEQUENCE);

  /**
   * The color.
   */
  final short color;

  /**
   * Lock for reads and writes on this buffer color.
   */
  final ReentrantLock lock = new ReentrantLock(false);

  /**
   * Condition used for notifying waiting read locks when new data is available.
   */
  final Condition dataWaiting = lock.newCondition();

  public short getColor() {
    return color;
  }

  public AtomicLong getSequence() {
    return sequence;
  }

  /**
   * Wake up all threads which are monitoring this color.
   */
  public void wake() {
    dataWaiting.signal();
  }

  /**
   * Return an instance to the lock for this color.
   *
   * @return an instance of the {@link ReentrantLock} associated with this color.
   *         This lock is unique to, immutable and is guaranteed  to always be the
   *         same lock for this color.
   */
  public ReentrantLock getLock() {
    return lock;
  }

  private BufferColor(final short color) {
    this.color = color;
  }

  /**
   * Return a new unique BufferColor.
   *
   * @return a new unique BufferColor
   *
   * @see #getNewColorFromHead(TransmissionBuffer)
   */
  public static BufferColor getNewColor() {
    short val = (short) bufferColorCounter.incrementAndGet();

    // in a long-running system, do not allow it to recycle over the global
    // color.
    if (val == Short.MIN_VALUE) {
      val = (short) bufferColorCounter.incrementAndGet();
    }

    return new BufferColor(val);
  }

  /**
   * Returns a new unique BufferColor set to the head sequence of the specified TransmissionBuffer.
   *
   * @param buffer
   *     the buffer instance to obtain the head sequence from.
   *
   * @return a new unique BufferColor instance.
   */
  public static BufferColor getNewColorFromHead(final TransmissionBuffer buffer) {
    final BufferColor color = getNewColor();
    color.sequence.set(buffer.getHeadSequence());
    return color;
  }

  /**
   * Returns the all colors BufferColor which creates buffer data visible to all colors.
   *
   * @return the all colors (global) BufferColor instance.
   */
  public static BufferColor getAllBuffersColor() {
    return allBuffersColor;
  }
}

