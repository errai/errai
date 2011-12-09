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

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Mike Brock
 */
public final class Segment {
  private final int start;
  private final int end;
  private volatile int read;
  private volatile int write;

  private final ReentrantLock lock = new ReentrantLock(true);
  private final Condition dataWaiting = lock.newCondition();

  public Segment(int start, int end) {
    this.read = this.write = this.start = start;
    this.end = end;
  }

  public int getStart() {
    return start;
  }

  public int getReadCursor() {
    return read;
  }

  public int getWriteCursor() {
    return write;
  }

  public void setReadCursor(int read) {
    this.read = read;
  }

  public void setWriteCursor(int write) {
    this.write = write;
  }

  public int getToRead() {
    if (read > write) {
      return (end - read) + (write - start);
    }
    else {
      return write - read;
    }
  }

  public int getFree() {
    if (read < write) {
      return end - read;
    }
    else {
      return (end - start) - (read - write);
    }
  }

  public int getWriteLineSize() {
    return end - write;
  }

  public int getReadLineSize() {
    return end - read;
  }

  public final ReentrantLock getLock() {
    return lock;
  }

  public final Condition getDataWaiting() {
    return dataWaiting;
  }

}

