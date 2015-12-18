/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.client.tests.support;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

import javax.annotation.Nonnull;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Test for regression in marshaller generator.
 * 
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@Portable
public class TestingTickCache implements Iterable<TestingTick> {

  /**
   * The entries in this cache. On the server side, we inject a ConcurrentLinkedQueue, which supports simultaneous
   * iteration and modification (because CDI events are dispatched asynchronously). On the client, it's a plain old
   * LinkedList.
   */
  private final Queue<TestingTick> entries;

  /**
   * The amount of time ticks should be retained within the cache. Default is 3 minutes.
   */
  private long timeSpan = 3 * 60 * 1000;

  /**
   * The entry most recently added to this cache.
   */
  private TestingTick newestEntry;

  // The @Nonnull annotation is there to ensure that multiple annotations can be specified on a constructor parameter
  // without affecting the behaviour for @MapsTo (see https://issues.jboss.org/browse/ERRAI-312)
  public TestingTickCache(@Nonnull @MapsTo("entries") Queue<TestingTick> queueImpl) {
    entries = queueImpl;
  }

  // We add this constructor to ensure that it is not picked up for mapping (it has no @MapsTo annotation on all its
  // parameters)
  public TestingTickCache(@Nonnull String s) {
    entries = null;
  }
  
  // We add this constructor to ensure that it is not picked up for mapping (it has no @MapsTo annotation on all its
  // parameters)
  /*public TestingTickCache(@Nonnull @MapsTo("entries") Queue<TestingTick> queueImpl, @Nonnull String s) {
    entries = null;
  }*/

  /**
   * Adds the given tick to this cache, pruning ticks that are older than {@link #timeSpan} milliseconds.
   * 
   * @param tick
   *          The tick to add
   */
  public void add(TestingTick tick) {
    entries.add(tick);
    newestEntry = tick;
    prune();
  }

  /**
   * Removes all leading entries that are older than the time span set on this cache.
   */
  private void prune() {
    long cutoff = System.currentTimeMillis() - timeSpan;
    while ((!entries.isEmpty()) && entries.element().getTime().getTime() < cutoff) {
      entries.remove(0);
    }
  }

  /**
   * Returns an iterator over this cache's entries. The returned iterator will not throw
   * {@link ConcurrentModificationException} even if entries are added to the cache during iteration.
   */
  @Override
  public Iterator<TestingTick> iterator() {
    return entries.iterator();
  }

  /**
   * The amount of time, in milliseconds, ticks are be retained within the cache.
   */
  public long getTimeSpan() {
    return timeSpan;
  }

  /**
   * The amount of time, in milliseconds, ticks should be retained within this cache.
   * <p>
   * Default is 3 minutes.
   */
  public void setTimeSpan(long timeSpan) {
    this.timeSpan = timeSpan;
  }

  /**
   * Returns the newest entry in this cache.
   * 
   * @throws NoSuchElementException
   *           if the cache is empty
   */
  public TestingTick getNewestEntry() {
    return newestEntry;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((entries == null) ? 0 : entries.hashCode());
    result = prime * result
        + ((newestEntry == null) ? 0 : newestEntry.hashCode());
    result = prime * result + (int) (timeSpan ^ (timeSpan >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    TestingTickCache other = (TestingTickCache) obj;
    if (entries == null) {
      if (other.entries != null)
        return false;
    }
    else if (!entries.equals(other.entries))
      return false;
    if (newestEntry == null) {
      if (other.newestEntry != null)
        return false;
    }
    else if (!newestEntry.equals(other.newestEntry))
      return false;
    if (timeSpan != other.timeSpan)
      return false;
    return true;
  }

}
