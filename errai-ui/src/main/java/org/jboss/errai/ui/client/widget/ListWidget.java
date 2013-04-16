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

package org.jboss.errai.ui.client.widget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.SyncToAsyncBeanManagerAdpater;
import org.jboss.errai.ioc.client.container.async.AsyncBeanDef;
import org.jboss.errai.ioc.client.container.async.AsyncBeanManager;
import org.jboss.errai.ioc.client.container.async.CreationalCallback;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A type of widget that displays and manages a child widget for each item in a list of model
 * objects. The widget instances are managed by Errai's IOC container and are arranged in a
 * {@link ComplexPanel}. By default, a {@link VerticalPanel} is used, but an alternative can be
 * specified using {@link #ListWidget(ComplexPanel)}.
 * 
 * @param <M>
 *          the model type
 * @param <W>
 *          the item widget type, needs to implement {@link HasModel} for associating the widget
 *          instance with the corresponding model instance.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public abstract class ListWidget<M, W extends HasModel<M> & IsWidget> extends Composite implements HasValue<List<M>> {

  private final ComplexPanel panel;
  private List<M> items;

  private final List<WidgetCreationalCallback> callbacks = new LinkedList<WidgetCreationalCallback>();
  private int pendingCallbacks;

  private boolean valueChangeHandlerInitialized;
  
  protected ListWidget() {
    this(new VerticalPanel());
  }

  protected ListWidget(ComplexPanel panel) {
    this.panel = Assert.notNull(panel);
    initWidget(panel);
  }

  /**
   * Returns the class object for the item widget type <W> to look up new instances of the widget
   * using the client-side bean manager.
   * 
   * @return the item widget type.
   */
  protected abstract Class<W> getItemWidgetType();

  /**
   * Called after all item widgets have been rendered. By default, this is a NOOP, but subclasses
   * can add behaviour if needed.
   * <p>
   * Using the standard synchronous bean manager this method is invoked before
   * {@link #setItems(List)} returns. However, when using the asynchronous bean manager and
   * declaring @LoadAsync on the item widget, this method might be called after
   * {@link #setItems(List)} returns and after the corresponding JavaScript code has been
   * downloaded.
   * 
   * @param items
   *          the rendered item list. Every change to this list will update the corresponding
   *          rendered item widgets.
   */
  protected void onItemsRendered(List<M> items) {};

  /**
   * Returns the panel that contains all item widgets.
   * 
   * @return the item widget panel, never null.
   */
  protected ComplexPanel getPanel() {
    return panel;
  }

  /**
   * Sets the list of model objects. A widget instance of type <W> will be added to the panel for
   * each object in the list.
   * <p>
   * If the standard synchronous bean manager is used it is guaranteed that all widgets have been
   * added to the panel when this method returns. In case the asynchronous bean manager is used this
   * method might return before the widgets have been added to the panel. See
   * {@link #onItemsRendered()}.
   * 
   * @param items
   *          The list of model objects. If null or empty all existing child widgets will be
   *          removed.
   */
  public void setItems(List<M> items) {
    this.items = new ListWrapper(items);

    // The AsyncBeanManager API works in both synchronous and asynchronous IOC mode
    AsyncBeanManager bm = IOC.getAsyncBeanManager();

    // In the case that this method is executed before the first call has successfully processed all
    // of its callbacks, we must cancel those uncompleted callbacks in flight to prevent duplicate
    // data in the ListWidget.
    for (WidgetCreationalCallback callback : callbacks) {
      callback.discard();
    }
    callbacks.clear();
    pendingCallbacks = 0;

    // clean up the old widgets before we add new ones (this will eventually become a feature of the
    // framework: ERRAI-375)
    Iterator<Widget> it = panel.iterator();
    while (it.hasNext()) {
      bm.destroyBean(it.next());
      it.remove();
    }

    if (items == null)
      return;

    pendingCallbacks = items.size();
    AsyncBeanDef<W> itemBeanDef = bm.lookupBean(getItemWidgetType());
    for (final M item : items) {
      final WidgetCreationalCallback callback = new WidgetCreationalCallback(item);
      callbacks.add(callback);
      itemBeanDef.newInstance(callback);
    }
  }

  /**
   * Returns the widget at the specified index.
   * 
   * @param index
   *          the index to be retrieved
   * 
   * @return the widget at the specified index
   * 
   * @throws IndexOutOfBoundsException
   *           if the index is out of range
   */
  @SuppressWarnings("unchecked")
  public W getWidget(int index) {
    return (W) panel.getWidget(index);
  }

  
  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<M>> handler) {
    if (!valueChangeHandlerInitialized) {
      valueChangeHandlerInitialized = true;
      addDomHandler(new ChangeHandler() {
        @Override
        public void onChange(ChangeEvent event) {
          ValueChangeEvent.fire(ListWidget.this, getValue());
        }
      }, ChangeEvent.getType());
    }
    return addHandler(handler, ValueChangeEvent.getType());
  }

  @Override
  public List<M> getValue() {
   return items;
  }

  @Override
  public void setValue(List<M> value) {
    setValue(value, false);
  }

  @Override
  public void setValue(List<M> value, boolean fireEvents) {
    List<M> oldValue = getValue();
    setItems(value);
    if (fireEvents) {
      ValueChangeEvent.fireIfNotEqual(this, oldValue, value);
    }
  }
  
  /**
   * A callback invoked by the {@link AsyncBeanManager} or {@link SyncToAsyncBeanManagerAdpater}
   * when the widget instance was created. It will associate the corresponding model instance with
   * the widget and add the widget to the panel.
   */
  private class WidgetCreationalCallback implements CreationalCallback<W> {
    private boolean discard;
    private final M item;

    private WidgetCreationalCallback(M item) {
      this.item = item;
    }

    @Override
    public void callback(W widget) {
      if (!discard) {
        widget.setModel(item);
        panel.add(widget);

        if (--pendingCallbacks == 0) {
          onItemsRendered(items);
        }
      }
    }

    public void discard() {
      this.discard = true;
    }
  }

  private class ListWrapper implements List<M> {
    private final List<M> list;

    public ListWrapper(List<M> list) {
      this.list = list;
    }

    @Override
    public boolean add(M e) {
      boolean b = list.add(e);
      addWidget(e);
      return b;
    }

    @Override
    public void add(int index, M element) {
      list.add(index, element);
      for (int i = index; i < list.size(); i++) {
        addAndReplaceWidget(index, i);
      }
    }

    @Override
    public boolean addAll(Collection<? extends M> c) {
      boolean b = list.addAll(c);
      for (M m : c) {
        addWidget(m);
      }
      return b;
    }

    @Override
    public boolean addAll(int index, Collection<? extends M> c) {
      boolean b = list.addAll(index, c);
      for (int i = index; i < list.size(); i++) {
        addAndReplaceWidget(index, i);
      }
      return b;
    }

    @Override
    public void clear() {
      list.clear();
      panel.clear();
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
        panel.remove(index);
      }
      return b;
    }

    @Override
    public M remove(int index) {
      M m = list.remove(index);
      panel.remove(index);
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
        for (Integer index : indexes) {
          panel.remove(index);
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
      for (int i = index; i < list.size(); i++) {
        addAndReplaceWidget(index, i);
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

    private void addAndReplaceWidget(final int startIndex, final int index) {
      if (index < panel.getWidgetCount()) {
        panel.remove(startIndex);
      }
      addWidget(list.get(index));
    }

    private void addWidget(final M m) {
      // This call is always synchronous, since the list can only be manipulated after
      // onItemsRendered was called. At that point the code of a potential split point must have
      // already been downloaded.
      AsyncBeanDef<W> itemBeanDef = IOC.getAsyncBeanManager().lookupBean(getItemWidgetType());
      itemBeanDef.getInstance(new CreationalCallback<W>() {
        @Override
        public void callback(W widget) {
          widget.setModel(m);
          panel.add(widget);
        }
      });
    }
  }
}