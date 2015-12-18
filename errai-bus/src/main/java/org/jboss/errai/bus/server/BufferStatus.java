/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.server;

/**
* @author Christian Sadilek
* @author Mike Brock
*/
class BufferStatus {
  private final int freeBytes;
  private final int tailRange;
  private final int activeTails;
  private final float free;

  BufferStatus(final int freeBytes, final int tailRange, final int activeTails, final float free) {
    this.freeBytes = freeBytes;
    this.tailRange = tailRange;
    this.activeTails = activeTails;
    this.free = free;
  }

  public int getFreeBytes() {
    return freeBytes;
  }

  public int getTailRange() {
    return tailRange;
  }

  public int getActiveTails() {
    return activeTails;
  }

  public float getFree() {
    return free;
  }
}
