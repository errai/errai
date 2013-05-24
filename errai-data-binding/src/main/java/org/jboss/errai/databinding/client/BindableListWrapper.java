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
    final List<M> oldValue = new ArrayList<M>(list);
    
    boolean b = list.add(element);
    for (BindableListChangeHandler<M> handler : handlers) {
      handler.onItemAdded(oldValue, element);
    }
    return b;
  }

  @Override
  public void add(int index, M element) {
    final List<M> oldValue = new ArrayList<M>(list);
    
    list.add(index, element);
    for (BindableListChangeHandler<M> handler : handlers) {
      handler.onItemAddedAt(oldValue, index, element);
    }
  }

  @Override
  public boolean addAll(Collection<? extends M> c) {
    final List<M> oldValue = new ArrayList<M>(list);
    
    boolean b = list.addAll(c);
    for (BindableListChangeHandler<M> handler : handlers) {
      handler.onItemsAdded(oldValue, c);
    }
    return b;
  }

  @Override
  public boolean addAll(int index, Collection<? extends M> c) {
    final List<M> oldValue = new ArrayList<M>(list);
    
    boolean b = list.addAll(index, c);
    for (BindableListChangeHandler<M> handler : handlers) {
      handler.onItemsAddedAt(oldValue, index,c);
    }
    return b;
  }

  @Override
  public void clear() {
    final List<M> oldValue = new ArrayList<M>(list);
    list.clear();
    for (BindableListChangeHandler<M> handler : handlers) {
      handler.onItemsCleared(oldValue);
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
    final List<M> oldValue = new ArrayList<M>(list);
    
    int index = list.indexOf(o);
    boolean b = list.remove(o);
    if (b) {
      for (BindableListChangeHandler<M> handler : handlers) {
        handler.onItemRemovedAt(oldValue, index);
      }
    }
    return b;
  }

  @Override
  public M remove(int index) {
    final List<M> oldValue = new ArrayList<M>(list);
    
    M m = list.remove(index);
    for (BindableListChangeHandler<M> handler : handlers) {
      handler.onItemRemovedAt(oldValue, index);
    }
    return m;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    final List<M> oldValue = new ArrayList<M>(list);
    
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
        handler.onItemsRemovedAt(oldValue, indexes);
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
    final List<M> oldValue = new ArrayList<M>(list);
    
    M m = list.set(index, element);
    for (BindableListChangeHandler<M> handler : handlers) {
      handler.onItemChanged(oldValue, index, element);
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
