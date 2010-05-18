/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.bus.server.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * A simple unbounded queue implementation that wraps {@link java.util.concurrent.ArrayBlockingQueue}.  When the capacity
 * of the queue is reached, the implementation will block an offering thread, while the underlying ArrayBlockQueue is
 * replaced with a queue double the original size.  This automatic growth is only supported by the {@link #offer(Object)}
 * and {@link #offer(Object, long, java.util.concurrent.TimeUnit)} methods.
 *
 * @param <E>
 */
public class UnboundedArrayBlockingQueue<E> implements BlockingQueue<E> {
    private volatile ArrayBlockingQueue<E> blockingQueue;
    private final Semaphore semaphore = new Semaphore(1000);
    private volatile int size;
    private final int maximumSize;
    private volatile boolean resizing = false;


    public UnboundedArrayBlockingQueue(int size) {
        blockingQueue = new ArrayBlockingQueue<E>(this.size = size);
        maximumSize = -1;
    }

    public UnboundedArrayBlockingQueue(int size, int maximumSize) {
        blockingQueue = new ArrayBlockingQueue<E>(this.size = size);
        this.maximumSize = maximumSize;
    }

    public boolean add(E o) {
        try {
            semaphore.acquireUninterruptibly();

            try {
                return blockingQueue.add(o);
            }
            catch (IllegalStateException e) {
                if (e.getMessage().equals("Queue full")) {
                    growQueue();
                    return add(o);
                } else {
                    throw e;
                }
            }
        }
        finally {
            semaphore.release();
        }
    }

    public boolean offer(E o) {
        try {
            semaphore.acquireUninterruptibly();
            if (blockingQueue.offer(o)) {
                return true;
            } else {
                growQueue();
                return offer(o);
            }
        }
        finally {
            semaphore.release();
        }
    }

    private void growQueue() {
        try {
            if (size >= maximumSize) return;
            semaphore.acquireUninterruptibly(999);
            if (resizing) {
                return;
            }
            resizing = true;

            if ((size *= 2) > maximumSize)
                size = maximumSize;

            ArrayBlockingQueue<E> newQueue = new ArrayBlockingQueue<E>(size *= 2);
            blockingQueue.drainTo(newQueue);
            blockingQueue = newQueue;

            System.out.println("grew to : " + size);
        }
        finally {
            resizing = false;
            semaphore.release(999);
        }
    }

    public void put(E o) throws InterruptedException {
        blockingQueue.offer(o);
    }

    public boolean offer(E o, long timeout, TimeUnit unit) throws InterruptedException {
        try {
            semaphore.acquireUninterruptibly();
            if (blockingQueue.offer(o, timeout, unit)) {
                return true;
            } else {
                growQueue();
                return offer(o);
            }
        }
        finally {
            semaphore.release();
        }
    }

    public E take() throws InterruptedException {
        return blockingQueue.take();
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        return blockingQueue.poll(timeout, unit);
    }

    public int remainingCapacity() {
        return blockingQueue.remainingCapacity();
    }

    public boolean remove(Object o) {
        return blockingQueue.remove(o);
    }

    public boolean contains(Object o) {
        return blockingQueue.contains(o);
    }

    public int drainTo(Collection<? super E> c) {
        return blockingQueue.drainTo(c);
    }

    public int drainTo(Collection<? super E> c, int maxElements) {
        return blockingQueue.drainTo(c, maxElements);
    }

    public E remove() {
        return blockingQueue.remove();
    }

    public E poll() {
        return blockingQueue.poll();
    }

    public E element() {
        return blockingQueue.element();
    }

    public E peek() {
        return blockingQueue.peek();
    }

    public int size() {
        return blockingQueue.size();
    }

    public boolean isEmpty() {
        return blockingQueue.isEmpty();
    }

    public Iterator<E> iterator() {
        return blockingQueue.iterator();
    }

    public Object[] toArray() {
        return blockingQueue.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return blockingQueue.toArray(a);
    }

    public boolean containsAll(Collection<?> c) {
        return blockingQueue.containsAll(c);
    }

    public boolean addAll(Collection c) {
        return blockingQueue.addAll(c);
    }

    public boolean removeAll(Collection<?> c) {
        return blockingQueue.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return blockingQueue.retainAll(c);
    }

    public void clear() {
        blockingQueue.clear();
    }
}
