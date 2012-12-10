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

import java.util.Iterator;
import java.util.List;

import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCBeanDef;
import org.jboss.errai.ioc.client.container.IOCBeanManager;

import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A type of widget that displays and manages a child widget for each item in a list of model objects. The widget
 * instances are managed by Errai's IOC container and are arranged in a {@link ComplexPanel}. By default a
 * {@link VerticalPanel} is used, but an alternative can be specified using {@link #ListWidget(ComplexPanel)}.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 *
 * @param <M>
 *          the model type
 * @param <W>
 *          the item widget type, needs to implement {@link HasModel} for associating the widget instance with the
 *          corresponding model instance.
 */
public abstract class ListWidget<M, W extends HasModel<M> & IsWidget> extends Composite {

  private final IOCBeanManager bm = IOC.getBeanManager();

  private final ComplexPanel panel;

  protected ListWidget() {
    this(new VerticalPanel());
  }

  protected ListWidget(ComplexPanel panel) {
    this.panel = Assert.notNull(panel);
    initWidget(panel);
  }

  /**
   * Returns the class object for the item widget type <W> to look up new instances of the widget using the client-side
   * bean manager.
   *
   * @return the item widget type.
   */
  protected abstract Class<W> getItemWidgetType();
  
  
  /**
   * Returns the panel that contains all item widgets.
   * 
   * @return the item widget panel, never null.
   */
  protected ComplexPanel getPanel() {
    return panel;
  }
  
  /**
   * Sets the list of model objects. A widget instance of type <W> will be added to the panel for each object in the
   * list.
   *
   * @param items
   *          The list of model objects. If null or empty all existing child widgets will be removed.
   */
  public void setItems(List<M> items) {
    // clean up the old widgets before we add new ones (this will eventually become a feature of the framework:
    // ERRAI-375)
    Iterator<Widget> it = panel.iterator();
    while (it.hasNext()) {
      bm.destroyBean(it.next());
      it.remove();
    }

    if (items == null)
      return;

    IOCBeanDef<W> itemBeanDef = bm.lookupBean(getItemWidgetType());
    for (M item : items) {
      W widget = itemBeanDef.newInstance();
      widget.setModel(item);
      panel.add((Widget) widget);
    }
  }
}