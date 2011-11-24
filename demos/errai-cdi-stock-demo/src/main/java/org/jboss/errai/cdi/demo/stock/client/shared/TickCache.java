package org.jboss.errai.cdi.demo.stock.client.shared;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.jboss.errai.common.client.api.annotations.ExposeEntity;

/**
 * A cache that can hold ticks over an arbitrary time interval.
 * 
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@ExposeEntity
public class TickCache {

  /**
   * The entries in this cache.
   */
  private final List<TickBuilder> entries = new LinkedList<TickBuilder>();
  
  /**
   * The amount of time ticks should be retained within the cache. Default is 3
   * minutes.
   */
  private long timeSpan = 3 * 60 * 1000;
  
  /**
   * Adds the given tick to this cache, pruning ticks that are older than
   * {@link #timeSpan} milliseconds.
   * 
   * @param tick The tick to add
   */
  public void add(TickBuilder tick) {
    entries.add(tick);
    prune();
  }

  /**
   * Removes all leading entries that are older than the time span set on this
   * cache.
   */
  private void prune() {
    long cutoff = System.currentTimeMillis() - timeSpan;
    while (entries.size() > 0 && entries.get(0).getTime().getTime() < cutoff) {
      entries.remove(0);
    }
  }
  
  /**
   * Returns an unmodifiable view of this cache's entries.
   */
  public List<TickBuilder> getEntries() {
    return Collections.unmodifiableList(entries);
  }

  /**
   * Provided for the benefit of Errai serialization. Do not call directly.
   */
  @Deprecated
  public void setEntries(List<TickBuilder> newEntries) {
    if (!entries.isEmpty()) {
      throw new IllegalStateException("This method should only be called by Errai serialization, and only when the cache is empty");
    }
    entries.addAll(newEntries);
  }
  
  /**
   * The amount of time, in milliseconds, ticks are be retained within the cache.
   */
  public long getTimeSpan() {
    return timeSpan;
  }

  /**
   * The amount of time, in milliseconds, ticks should be retained within this
   * cache.
   * <p>
   * Default is 3 minutes.
   */
  public void setTimeSpan(long timeSpan) {
    this.timeSpan = timeSpan;
  }

  /**
   * Returns the newest entry in this cache.
   * 
   * @throws NoSuchElementException if the cache is empty
   */
  public TickBuilder getNewestEntry() {
    return entries.get(entries.size() - 1);
  }
}
