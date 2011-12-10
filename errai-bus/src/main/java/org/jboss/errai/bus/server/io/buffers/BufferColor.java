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
public final class BufferColor {
  private final AtomicLong sequence = new AtomicLong();

  private final short color;
  final ReentrantLock lock = new ReentrantLock(true);
  final Condition dataWaiting = lock.newCondition();

  public BufferColor(int color) {
    this.color = (short) color;
  }

  public BufferColor(short color) {
    this.color = color;
  }

  public short getColor() {
    return color;
  }

  public long getSequence() {
    return sequence.get();
  }

  public void incrementSequence(long delta) {
    sequence.addAndGet(delta);
  }

  public void wake() {
    lock.lock();
    dataWaiting.signal();
    lock.unlock();
  }
}

