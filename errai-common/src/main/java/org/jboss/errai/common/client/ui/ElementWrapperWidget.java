/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.common.client.ui;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * A widget that wraps an {@link Element} to support the registration of event listeners and data
 * binding.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ElementWrapperWidget extends Widget implements HasHTML {
  private static Map<Element, ElementWrapperWidget> widgetMap = new HashMap<Element, ElementWrapperWidget>();

  public static ElementWrapperWidget getWidget(final Element element) {
    ElementWrapperWidget widget = widgetMap.get(element);
    if (widget == null) {
      widget = new ElementWrapperWidget(element);
      widgetMap.put(element, widget);
    }
    return widget;
  }

  public static ElementWrapperWidget removeWidget(final Element element) {
    return widgetMap.remove(element);
  }

  public static ElementWrapperWidget removeWidget(final ElementWrapperWidget widget) {
    return widgetMap.remove(widget.getElement());
  }

  private EventListener listener = new EventListener() {
    @Override
    public void onBrowserEvent(final Event event) {
      ElementWrapperWidget.super.onBrowserEvent(event);
    }
  };

  private ElementWrapperWidget(Element wrapped) {
    if (wrapped == null) {
      throw new IllegalArgumentException(
              "Element to be wrapped must not be null - Did you forget to initialize or @Inject a UI field?");
    }
    this.setElement(wrapped);
    DOM.setEventListener(this.getElement(), this);
  }

  @Override
  public String getText() {
    return getElement().getInnerText();
  }

  @Override
  public void setText(final String text) {
    getElement().setInnerText(text);
  }

  @Override
  public String getHTML() {
    return getElement().getInnerHTML();
  }

  @Override
  public void setHTML(final String html) {
    getElement().setInnerHTML(html);
  }

  public void setEventListener(final int eventsToSink, final EventListener listener) {
    if (listener == null) {
      throw new IllegalArgumentException("EventListener cannot be null.");
    }

    sinkEvents(eventsToSink);
    this.listener = listener;
  }

  @Override
  public void onBrowserEvent(Event event) {
    listener.onBrowserEvent(event);
  }

}
