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

package org.jboss.errai.databinding.client;

import org.jboss.errai.databinding.client.api.Converter;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;

/**
 * Represents the binding of a bean property to a widget and holds all relevant binding-specific metadata.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public final class Binding {

  private final String property;
  private final Widget widget;
  private final Converter<?, ?> converter;
  private final HandlerRegistration handlerRegistration;

  public Binding(String property, Widget widget, Converter<?, ?> converter, HandlerRegistration handlerRegistration) {
    this.property = property;
    this.widget = widget;
    this.converter = converter;
    this.handlerRegistration = handlerRegistration;
  }

  public String getProperty() {
    return property;
  }

  public Converter<?, ?> getConverter() {
    return converter;
  }

  public Widget getWidget() {
    return widget;
  }

  public HandlerRegistration getHandlerRegistration() {
    return handlerRegistration;
  }

  public void removeHandler() {
    if (handlerRegistration != null) {
      handlerRegistration.removeHandler();
    }
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((converter == null) ? 0 : converter.hashCode());
    result = prime * result + ((handlerRegistration == null) ? 0 : handlerRegistration.hashCode());
    result = prime * result + ((property == null) ? 0 : property.hashCode());
    result = prime * result + ((widget == null) ? 0 : widget.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Binding other = (Binding) obj;
    if (converter == null) {
      if (other.converter != null)
        return false;
    }
    else if (!converter.equals(other.converter))
      return false;
    if (handlerRegistration == null) {
      if (other.handlerRegistration != null)
        return false;
    }
    else if (!handlerRegistration.equals(other.handlerRegistration))
      return false;
    if (property == null) {
      if (other.property != null)
        return false;
    }
    else if (!property.equals(other.property))
      return false;
    if (widget == null) {
      if (other.widget != null)
        return false;
    }
    else if (!widget.equals(other.widget))
      return false;
    return true;
  }

}