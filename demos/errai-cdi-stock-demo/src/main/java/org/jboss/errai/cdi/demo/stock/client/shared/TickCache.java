package org.jboss.errai.cdi.demo.stock.client.shared;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.marshalling.client.api.annotations.MapsTo;

/**
 * A cache that can hold ticks over an arbitrary time interval.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@Portable
public class TickCache implements Iterable<Tick> {

  /**
   * The entries in this cache. On the server side, we inject a
   * ConcurrentLinkedQueue, which supports simultaneous iteration and
   * modification (because CDI events are dispatched asynchronously).
   * On the client, it's a plain old LinkedList.
   */
  private final Queue<Tick> entries;

  /**
   * The amount of time ticks should be retained within the cache. Default is 3 minutes.
   */
  private long timeSpan = 3 * 60 * 1000;

  /**
   * The entry most recently added to this cache.
   */
  private Tick newestEntry;

  public TickCache(@MapsTo("entries") Queue<Tick> queueImpl) {
    entries = queueImpl;
  }

  /**
   * Adds the given tick to this cache, pruning ticks that are older than {@link #timeSpan} milliseconds.
   *
   * @param tick
   *          The tick to add
   */
  public void add(Tick tick) {
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
      entries.remove();
    }
  }

  /**
   * Returns an iterator over this cache's entries. The returned iterator will
   * not throw {@link ConcurrentModificationException} even if entries are added
   * to the cache during iteration.
   */
  @Override
  public Iterator<Tick> iterator() {
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
  public Tick getNewestEntry() {
    return newestEntry;
  }
}
