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

import java.util.Collection;
import java.util.Iterator;

import org.jboss.errai.databinding.client.api.Converter;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;

/**
 * Represents the binding of a bean property to a widget and holds all relevant binding-specific
 * metadata.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public final class Binding {

  private final String property;
  private final Widget widget;
  private final Converter<?, ?> converter;
  private final Collection<HandlerRegistration> handlerRegistrations;

  public Binding(String property, Widget widget, Converter<?, ?> converter, Collection<HandlerRegistration>
                                                                              handlerRegistrations) {
    this.property = property;
    this.widget = widget;
    this.converter = converter;
    this.handlerRegistrations = handlerRegistrations;
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

  public Collection<HandlerRegistration> getHandlerRegistrations() {
    return handlerRegistrations;
  }

  public void removeHandlers() {
    if (handlerRegistrations != null) {
      Iterator<HandlerRegistration> itr = handlerRegistrations.iterator();
      while (itr.hasNext()) {
        itr.next().removeHandler();
        itr.remove();
      }
    }
  }

  @Override
  public String toString() {
    return "Binding [property=" + property + ", widget=" + widget + "]";
  }

}