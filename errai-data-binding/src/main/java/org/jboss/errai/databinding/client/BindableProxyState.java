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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.errai.databinding.client.api.Converter;
import org.jboss.errai.databinding.client.api.InitialState;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;

/**
 * Holds all state of a generated {@link BindableProxy}.
 * 
 * This class exists to avoid code duplication in the generated proxies. We can't add a superclass to the proxies as
 * they already subclass the actual bindable type (the corresponding model).
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 * 
 * @param <T>
 *          The type of the target model being proxied.
 * 
 */
@SuppressWarnings("rawtypes")
public final class BindableProxyState<T> {
  final Map<String, Class<?>> propertyTypes = new HashMap<String, Class<?>>();
  final Map<String, Widget> bindings = new HashMap<String, Widget>();
  final Map<String, Converter> converters = new HashMap<String, Converter>();
  final Map<String, HandlerRegistration> handlerRegistrations = new HashMap<String, HandlerRegistration>();

  final PropertyChangeHandlerSupport propertyChangeHandlerSupport = new PropertyChangeHandlerSupport();

  final T target;
  final InitialState initialState;

  BindableProxyState(T target, InitialState initialState) {
    this.target = target;
    this.initialState = initialState;
  }

  void unbind() {
    for (Object reg : handlerRegistrations.keySet()) {
      (handlerRegistrations.get(reg)).removeHandler();
    }
    bindings.clear();
    handlerRegistrations.clear();
    converters.clear();
  }

  void unbind(String property) {
    bindings.remove(property);
    converters.remove(property);
    HandlerRegistration reg = handlerRegistrations.remove(property);
    if (reg != null) {
      reg.removeHandler();
    }
  }

  /**
   * Returns the set of bound properties of this proxy.
   * 
   * @return bound properties, an empty set if no properties have been bound.
   */
  public Set<String> getBoundProperties() {
    return bindings.keySet();
  }
  
  /**
   * Returns the widget currently bound to the provided property (see {@link BindableProxy#bind(Widget, String, Converter)}).
   * 
   * @param property
   *          the name of the model property
   * @return the widget currently bound to the provided property or null if no widget was bound to the property.
   */
  public Widget getWidget(String property) {
    return bindings.get(property);
  }

  /**
   * Returns the converter used for the binding of the provided property (see {@link BindableProxy#bind(Widget, String, Converter)}).
   * 
   * @param property
   *          the name of the model property
   * @return the converter used for the bound property or null if the property was not bound or no converter was
   *         specified for the binding.
   */
  public Converter getConverter(String property) {
    return converters.get(property);
  }
  
  /**
   * Returns the {@link InitialState} configured when the proxy was created.
   * 
   * @return initial state, can be null.
   */
  public InitialState getInitialState() {
    return initialState;
  }
}
