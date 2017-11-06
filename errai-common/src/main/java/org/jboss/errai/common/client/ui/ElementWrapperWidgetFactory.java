/*
 * Copyright (C) 2017 Red Hat, Inc. and/or its affiliates.
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

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.TextAreaElement;
import com.google.gwt.user.client.ui.RootPanel;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.common.client.dom.HTMLElement;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class ElementWrapperWidgetFactory {
  private static Map<Object, ElementWrapperWidget<?>> widgetMap = new HashMap<>();

  public static ElementWrapperWidget<?> getWidget(final Element element) {
    return getWidget(element, null);
  }

  public static ElementWrapperWidget<?> getWidget(final HTMLElement element) {
    return getWidget(element, null);
  }

  public static ElementWrapperWidget<?> getWidget(final Element element, final Class<?> valueType) {
    return getWidget((Object) element, valueType);
  }

  public static ElementWrapperWidget<?> getWidget(final Object obj, final Class<?> valueType) {
    final Element element = asElement(obj);
    ElementWrapperWidget<?> widget = widgetMap.get(element);
    if (widget == null) {
      widget = createElementWrapperWidget(element, valueType);
      // Always call onAttach so that events propogatge even if this has no widget parent.
      DOMUtil.onAttach(widget);
      RootPanel.detachOnWindowClose(widget);
      widgetMap.put(element, widget);
    }
    else if (valueType != null && !valueType.equals(widget.getValueType())) {
      throw new RuntimeException(
              "There already exists a widget for the given element with a different value type. Expected "
                      + widget.getValueType().getName() + " but was passed in " + valueType.getName());
    }

    return widget;
  }

  private static native Element asElement(Object obj) /*-{
      return obj;
  }-*/;

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private static ElementWrapperWidget<?> createElementWrapperWidget(final Element element, final Class<?> valueType) {
    if (valueType != null) {
      final NativeHasValueAccessors.Accessor accessor;
      if (NativeHasValueAccessors.hasValueAccessor(element)) {
        accessor = NativeHasValueAccessors.getAccessor(element);
      }
      else {
        accessor = new ElementWrapperWidget.DefaultAccessor((org.jboss.errai.common.client.ui.HasValue) element);
      }

      return new ElementWrapperWidget.JsTypeHasValueElementWrapperWidget<>(element, accessor, valueType);
    }
    else if (InputElement.is(element) || TextAreaElement.is(element)) {
      return new ElementWrapperWidget.InputElementWrapperWidget<>(element);
    }
    else {
      return new ElementWrapperWidget.DefaultElementWrapperWidget<>(element);
    }
  }


  public static ElementWrapperWidget<?> removeWidget(final Element element) {
    return widgetMap.remove(element);
  }

  public static ElementWrapperWidget<?> removeWidget(final ElementWrapperWidget<?> widget) {
    return widgetMap.remove(widget.getElement());
  }

}
