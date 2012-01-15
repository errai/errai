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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Mike Brock
 */
public class BufferColor {
  // an automatic counter to ensure each buffer has a unique color
  private static final AtomicInteger bufferColorCounter = new AtomicInteger();

  private static final BufferColor allBuffersColor = new BufferColor(Short.MIN_VALUE);

  /**
   * The current tail position for this buffer color.
   */
  final AtomicLong sequence = new AtomicLong();

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

  public void wake() {
    dataWaiting.signalAll();
  }

  public ReentrantLock getLock() {
    return lock;
  }

  private BufferColor(int color) {
    this.color = (short) color;
  }

  private BufferColor(short color) {
    this.color = color;
  }

  public static BufferColor getNewColor() {
    return new BufferColor(bufferColorCounter.incrementAndGet());
  }

  public static BufferColor getAllBuffersColor() {
    return allBuffersColor;
  }
}

