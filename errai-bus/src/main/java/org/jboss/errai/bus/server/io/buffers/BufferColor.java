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
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Mike Brock
 */
public final class BufferColor {
  private AtomicInteger sequence = new AtomicInteger();

  private final int color;
  private final ReentrantLock lock = new ReentrantLock(true);
  private final Condition dataWaiting = lock.newCondition();

  public BufferColor(int color) {
    this.color = color;
  }

  public int getColor() {
    return color;
  }

  public int getSequence() {
    return sequence.intValue();
  }

  public void incrementSequence() {
    sequence.incrementAndGet();
  }


  public void setSequence(int s) {
    sequence.lazySet(s);
  }

  public final ReentrantLock getLock() {
    return lock;
  }

  public final Condition getDataWaiting() {
    return dataWaiting;
  }

  public void wake() {
    try {
      lock.lock();
      dataWaiting.signal();
    }
    finally {
      lock.unlock();
    }
  }
}

