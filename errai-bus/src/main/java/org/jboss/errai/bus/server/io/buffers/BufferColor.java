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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Mike Brock
 */
public class BufferColor {

  // an automatic counter to ensure each buffer has a unique color
  private static final AtomicInteger bufferColorCounter = new AtomicInteger();

  private static final BufferColor allBuffersColor = new BufferColor(Short.MIN_VALUE);

  /**
   * start class members *
   */

  final AtomicLong sequence = new AtomicLong();
  final short color;
  final ReentrantLock lock = new ReentrantLock(true);
  final Condition dataWaiting = lock.newCondition();

  public short getColor() {
    return color;
  }

//  public long getSequence() {
//    return sequence.get();
//  }
//
  public AtomicLong getSequence() {
    return sequence;
  }

//  public void setSequence(long seq) {
//    sequence.set(seq);
//  }

  public void wake() {
    lock.lock();
    dataWaiting.signal();
    lock.unlock();
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

