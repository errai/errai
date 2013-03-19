/*
 * Copyright 2012 JBoss, by Red Hat, Inc
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
package org.jboss.errai.common.client.ui;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

/**
 * A widget that wraps an {@link Element} to support the registration of event listeners and data
 * binding.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class ElementWrapperWidget extends Widget implements HasHTML, HasValue {

  private static Map<Element, ElementWrapperWidget> widgetMap = new HashMap<Element, ElementWrapperWidget>();

  private Object value;

  private ElementWrapperWidget(Element wrapped) {
    if (wrapped == null) {
      throw new IllegalArgumentException(
              "Element to be wrapped must not be null - Did you forget to initialize or @Inject a UI field?");
    }
    this.setElement(wrapped);
    DOM.setEventListener(this.getElement(), this);
  }

  public static ElementWrapperWidget getWidget(Element element) {
    ElementWrapperWidget widget = widgetMap.get(element);
    if (widget == null) {
      widget = new ElementWrapperWidget(element);
      widgetMap.put(element, widget);
    }
    return widget;
  }

  @Override
  public String getText() {
    return getElement().getInnerText();
  }

  @Override
  public void setText(String text) {
    getElement().setInnerText(text);
  }

  @Override
  public String getHTML() {
    return getElement().getInnerHTML();
  }

  @Override
  public void setHTML(String html) {
    getElement().setInnerHTML(html);
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler handler) {
    return addDomHandler(new ChangeHandler() {
      
      @Override
      public void onChange(ChangeEvent event) {
        ValueChangeEvent.fire(ElementWrapperWidget.this, getValue());
      }
    }, ChangeEvent.getType());
  }

  @Override
  public Object getValue() {
    return value;
  }

  @Override
  public void setValue(Object value) {
    setValue(value, false);
  }

  @Override
  public void setValue(Object value, boolean fireEvents) {
    Object oldValue = getValue();
    setText(value.toString());
    if (fireEvents) {
      ValueChangeEvent.fireIfNotEqual(this, oldValue, value);
    }
  }

}