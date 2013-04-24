/*
 * Copyright 2013 JBoss, by Red Hat, Inc
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

package org.jboss.errai.databinding.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Wraps a List<M> to notify change handlers of all operations that mutate the underlying list.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 *
 * @param <M>
 */
public class BindableListWrapper<M> implements List<M> {

  private final List<M> list;
  private final List<BindableListChangeHandler<M>> handlers = new ArrayList<BindableListChangeHandler<M>>();

  public BindableListWrapper(List<M> list) {
    this.list = list;
  }

  @Override
  public boolean add(M element) {
    boolean b = list.add(element);
    for (BindableListChangeHandler<M> handler : handlers) {
      handler.onItemAdded(element);
    }
    return b;
  }

  @Override
  public void add(int index, M element) {
    list.add(index, element);
    for (BindableListChangeHandler<M> handler : handlers) {
      handler.onItemAddedAt(index, element);
    }
  }

  @Override
  public boolean addAll(Collection<? extends M> c) {
    boolean b = list.addAll(c);
    for (BindableListChangeHandler<M> handler : handlers) {
      handler.onItemsAdded(c);
    }
    return b;
  }

  @Override
  public boolean addAll(int index, Collection<? extends M> c) {
    boolean b = list.addAll(index, c);
    for (BindableListChangeHandler<M> handler : handlers) {
      handler.onItemsAddedAt(index,c);
    }
    return b;
  }

  @Override
  public void clear() {
    list.clear();
    for (BindableListChangeHandler<M> handler : handlers) {
      handler.onItemsCleared();
    }
  }

  @Override
  public boolean contains(Object o) {
    return list.contains(o);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return list.containsAll(c);
  }

  @Override
  public M get(int index) {
    return list.get(index);
  }

  @Override
  public int indexOf(Object o) {
    return list.indexOf(o);
  }

  @Override
  public boolean isEmpty() {
    return list.isEmpty();
  }

  @Override
  public Iterator<M> iterator() {
    return list.iterator();
  }

  @Override
  public int lastIndexOf(Object o) {
    return list.lastIndexOf(o);
  }

  @Override
  public ListIterator<M> listIterator() {
    return list.listIterator();
  }

  @Override
  public ListIterator<M> listIterator(int index) {
    return list.listIterator(index);
  }

  @Override
  public boolean remove(Object o) {
    int index = list.indexOf(o);
    boolean b = list.remove(o);
    if (b) {
      for (BindableListChangeHandler<M> handler : handlers) {
        handler.onItemRemovedAt(index);
      }
    }
    return b;
  }

  @Override
  public M remove(int index) {
    M m = list.remove(index);
    for (BindableListChangeHandler<M> handler : handlers) {
      handler.onItemRemovedAt(index);
    }
    return m;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    List<Integer> indexes = new ArrayList<Integer>();
    for (Object m : c) {
      Integer index = list.indexOf(m);
      if (!indexes.contains(index)) {
        indexes.add(index);
      }
    }
    Collections.sort(indexes, Collections.reverseOrder());

    boolean b = list.removeAll(c);
    if (b) {
      for (BindableListChangeHandler<M> handler : handlers) {
        handler.onItemsRemovedAt(indexes);
      }
    }
    return b;
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    return list.retainAll(c);
  }

  @Override
  public M set(int index, M element) {
    M m = list.set(index, element);
    for (BindableListChangeHandler<M> handler : handlers) {
      handler.onItemChanged(index, element);
    }
    return m;
  }

  @Override
  public int size() {
    return list.size();
  }

  @Override
  public List<M> subList(int fromIndex, int toIndex) {
    return list.subList(fromIndex, toIndex);
  }

  @Override
  public Object[] toArray() {
    return list.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return list.toArray(a);
  }

  public void addChangeHandler(BindableListChangeHandler<M> handler) {
    handlers.add(handler);
  }
  
  public void removeChangeHandler(BindableListChangeHandler<M> handler) {
    handlers.remove(handler);
  }
}
