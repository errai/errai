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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.enterprise.context.Dependent;

import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.databinding.client.BindableListChangeHandler;
import org.jboss.errai.databinding.client.BindableListWrapper;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.SyncToAsyncBeanManagerAdapter;
import org.jboss.errai.ioc.client.container.async.AsyncBeanDef;
import org.jboss.errai.ioc.client.container.async.AsyncBeanManager;
import org.jboss.errai.ui.client.local.spi.InvalidBeanScopeException;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.InsertPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * A type of widget that displays and manages a child widget for each item in a
 * list of model objects. The widget instances are managed by Errai's IOC
 * container and are arranged in a {@link ComplexPanel}. By default, a
 * {@link FlowPanel} is used, but an alternative can be specified using
 * {@link #ListWidget(ComplexPanel)}.
 * 
 * @param <M>
 *          the model type
 * @param <W>
 *          the item widget type, needs to implement {@link HasModel} for
 *          associating the widget instance with the corresponding model
 *          instance.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public abstract class ListWidget<M, W extends HasModel<M> & IsWidget> extends Composite 
  implements HasValue<List<M>>, BindableListChangeHandler<M> {

  private final ComplexPanel panel;
  private BindableListWrapper<M> items;

  private final List<WidgetCreationalCallback> callbacks = new LinkedList<WidgetCreationalCallback>();
  private int pendingCallbacks;

  private boolean valueChangeHandlerInitialized;

  protected ListWidget() {
    this(new FlowPanel());
  }

  protected ListWidget(ComplexPanel panel) {
    this.panel = Assert.notNull(panel);
    initWidget(panel);
  }
  
  /**
   * Returns the class object for the item widget type <W> to look up new
   * instances of the widget using the client-side bean manager.
   * 
   * @return the item widget type.
   */
  protected abstract Class<W> getItemWidgetType();

  /**
   * Called after all item widgets have been rendered. By default, this is a
   * NOOP, but subclasses can add behaviour if needed.
   * <p>
   * Using the standard synchronous bean manager this method is invoked before
   * {@link #setItems(List)} returns. However, when using the asynchronous bean
   * manager and declaring @LoadAsync on the item widget, this method might be
   * called after {@link #setItems(List)} returns and after the corresponding
   * JavaScript code has been downloaded.
   * 
   * @param items
   *          the rendered item list. Every change to this list will update the
   *          corresponding rendered item widgets.
   */
  protected void onItemsRendered(List<M> items) {
  }

  /**
   * Returns the panel that contains all item widgets.
   * 
   * @return the item widget panel, never null.
   */
  protected ComplexPanel getPanel() {
    return panel;
  }

  /**
   * Sets the list of model objects. A widget instance of type <W> will be added
   * to the panel for each object in the list. The list will be wrapped in an
   * {@link BindableListWrapper} to make direct changes to the list observable.
   * <p>
   * If the standard synchronous bean manager is used it is guaranteed that all
   * widgets have been added to the panel when this method returns. In case the
   * asynchronous bean manager is used this method might return before the
   * widgets have been added to the panel. See {@link #onItemsRendered(List)}.
   * 
   * @param items
   *          The list of model objects. If null or empty all existing child
   *          widgets will be removed.
   */
  public void setItems(List<M> items) {
    boolean changed = this.items != items;

    if (items instanceof BindableListWrapper) {
      this.items = (BindableListWrapper<M>) items;
    }
    else {
      if (items != null) {
        this.items = new BindableListWrapper<M>(items);
      }
      else {
        this.items = new BindableListWrapper<M>(new ArrayList<M>());
      }
    }

    if (changed) {
      this.items.addChangeHandler(this);
      init();
    }
  }

  private void init() {
    // The AsyncBeanManager API works in both synchronous and asynchronous IOC mode
    AsyncBeanManager bm = IOC.getAsyncBeanManager();

    // In the case that this method is executed before the first call has
    // successfully processed all of its callbacks, we must cancel those
    // uncompleted callbacks in flight to prevent duplicate data in the
    // ListWidget.
    for (WidgetCreationalCallback callback : callbacks) {
      callback.discard();
    }
    callbacks.clear();
    pendingCallbacks = 0;

    // clean up the old widgets before we add new ones (this will eventually
    // become a feature of the framework: ERRAI-375)
    Iterator<Widget> it = panel.iterator();
    while (it.hasNext()) {
      bm.destroyBean(it.next());
      it.remove();
    }

    if (items == null)
      return;

    pendingCallbacks = items.size();
    AsyncBeanDef<W> itemBeanDef = bm.lookupBean(getItemWidgetType());

    if (!itemBeanDef.getScope().equals(Dependent.class))
      throw new InvalidBeanScopeException("ListWidget cannot contain ApplicationScoped widgets");

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

  /**
   * Returns the widget currently displaying the provided model.
   * 
   * @param model
   *          the model displayed by the widget
   * 
   * @return the widget displaying the provided model instance, null if no
   *         widget was found for the model.
   */
  public W getWidget(M model) {
    int index = items.indexOf(model);
    return getWidget(index);
  }
  
  /**
   * Returns the number of widgets currently being displayed.
   * 
   * @return the number of widgets.
   */
  public int getWidgetCount() {
    return getPanel().getWidgetCount();
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
    if (items == null) {
      items = new BindableListWrapper<M>(new ArrayList<M>());
      items.addChangeHandler(this);
    }
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
   * A callback invoked by the {@link AsyncBeanManager} or
   * {@link SyncToAsyncBeanManagerAdapter} when the widget instance was created.
   * It will associate the corresponding model instance with the widget and add
   * the widget to the panel.
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

  @Override
  public void onItemAdded(List<M> oldList, M item) {
    addWidget(item);
  }

  @Override
  public void onItemAddedAt(List<M> oldList, int index, M item) {
    if (panel instanceof InsertPanel) {
      addWidgetAt(index, items.get(index));
    }
    else {
      for (int i = index; i < items.size(); i++) {
        addAndReplaceWidget(index, i);
      }
    }
  }

  @Override
  public void onItemsAdded(List<M> oldList, Collection<? extends M> items) {
    for (M m : items) {
      addWidget(m);
    }
  }

  @Override
  public void onItemsAddedAt(List<M> oldList, int index, Collection<? extends M> item) {
    if (panel instanceof InsertPanel.ForIsWidget) {
      for (int i = index; i < index + item.size(); i++) {
        addWidgetAt(i, items.get(i));
      }
    }
    else {
      for (int i = index; i < items.size(); i++) {
        addAndReplaceWidget(index, i);
      }
    }
  }

  @Override
  public void onItemsCleared(List<M> oldList) {
    AsyncBeanManager bm = IOC.getAsyncBeanManager();
    Integer widgetCount = panel.getWidgetCount();

    Collection<Widget> widgets = new ArrayList<Widget>(widgetCount);
    for (int i = 0; i < widgetCount; i++) {
      widgets.add(panel.getWidget(i));
    }

    panel.clear();

    Iterator<Widget> itr = widgets.iterator();
    while (itr.hasNext()) {
      Widget w = (Widget) itr.next();
      bm.destroyBean(w);
    }
  }

  @Override
  public void onItemRemovedAt(List<M> oldList, int index) {
    Widget widget = panel.getWidget(index);
    panel.remove(index);
    IOC.getAsyncBeanManager().destroyBean(widget);
  }

  @Override
  public void onItemsRemovedAt(List<M> oldList, List<Integer> indexes) {
    for (Integer index : indexes) {
      Widget widget = panel.getWidget(index);
      panel.remove(index);
      IOC.getAsyncBeanManager().destroyBean(widget);
    }
  }

  @Override
  public void onItemChanged(List<M> oldList, int index, M item) {
    if (oldList.get(index) == item) 
      return;
    
    for (int i = index; i < items.size(); i++) {
      addAndReplaceWidget(index, i);
    }
  }

  private void addAndReplaceWidget(final int startIndex, final int index) {
    if (index < panel.getWidgetCount()) {
      panel.remove(startIndex);
    }
    addWidget(items.get(index));
  }
  
  private void addWidget(final M m) {
    AsyncBeanDef<W> itemBeanDef = IOC.getAsyncBeanManager().lookupBean(getItemWidgetType());
    itemBeanDef.getInstance(new CreationalCallback<W>() {
      @Override
      public void callback(W widget) {
        widget.setModel(m);
        panel.add(widget);
      }
    });
  }

  private void addWidgetAt(final int index, final M m) {
    if (!(panel instanceof InsertPanel.ForIsWidget)) {
      throw new RuntimeException("Method only supported for panels that implement: "
              + InsertPanel.ForIsWidget.class.getName());
    }

    AsyncBeanDef<W> itemBeanDef = IOC.getAsyncBeanManager().lookupBean(getItemWidgetType());
    itemBeanDef.getInstance(new CreationalCallback<W>() {
      @Override
      public void callback(W widget) {
        widget.setModel(m);
        ((InsertPanel.ForIsWidget) panel).insert(widget, index);
      }
    });
  }

}