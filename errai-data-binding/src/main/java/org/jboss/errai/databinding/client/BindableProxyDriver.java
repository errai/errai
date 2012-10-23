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

import org.jboss.errai.databinding.client.api.Convert;
import org.jboss.errai.databinding.client.api.Converter;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.InitialState;
import org.jboss.errai.databinding.client.api.PropertyChangeEvent;
import org.jboss.errai.databinding.client.api.PropertyChangeHandler;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

/**
 * Manages bindings and property change handlers for a {@link BindableProxy}.
 * 
 * Each generated {@link BindableProxy} uses an instance of this class to keep the widgets and corresponding model in
 * sync.
 * <p>
 * The driver will:
 * <ul>
 * <li>Carry out an initial state sync between the bound widgets and the target model, if specified (see
 * {@link DataBinder#DataBinder(Object, InitialState)})</li>
 * 
 * <li>Update the bound widget when a setter method is invoked on the model (see
 * {@link #updateWidgetAndFireEvents(String, Object, Object)}). Works for widgets that either implement {@link HasValue}
 * or {@link HasText})</li>
 * 
 * <li>Update the target model's state in response to value change events (only works for widgets that implement
 * {@link HasValue})</li>
 * <ul>
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 * 
 * @param <T>
 *          The type of the target model being proxied.
 * 
 */
@SuppressWarnings("rawtypes")
public final class BindableProxyDriver<T> implements HasPropertyChangeHandlers {
  final Map<String, Class> propertyTypes = new HashMap<String, Class>();
  final Map<String, Widget> bindings = new HashMap<String, Widget>();
  final Map<String, Converter> converters = new HashMap<String, Converter>();
  final Map<String, HandlerRegistration> handlerRegistrations = new HashMap<String, HandlerRegistration>();
  final PropertyChangeHandlerSupport propertyChangeHandlerSupport = new PropertyChangeHandlerSupport();

  final BindableProxy<T> proxy;
  final T target;
  final InitialState initialState;

  BindableProxyDriver(BindableProxy<T> proxy, T target, InitialState initialState) {
    this.proxy = proxy;
    this.target = target;
    this.initialState = initialState;
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
   * Returns the widget currently bound to the provided property (see
   * {@link BindableProxy#bind(Widget, String, Converter)}).
   * 
   * @param property
   *          the name of the model property
   * @return the widget currently bound to the provided property or null if no widget was bound to the property.
   */
  public Widget getWidget(final String property) {
    return bindings.get(property);
  }

  /**
   * Returns the converter used for the binding of the provided property (see
   * {@link BindableProxy#bind(Widget, String, Converter)}).
   * 
   * @param property
   *          the name of the model property
   * @return the converter used for the bound property or null if the property was not bound or no converter was
   *         specified for the binding.
   */
  public Converter getConverter(final String property) {
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

  /**
   * Binds the provided widget to the specified property of the model instance associated with this proxy (see
   * {@link #setModel(Object, InitialState)}).
   * 
   * @param widget
   *          the widget to bind, must not be null.
   * @param property
   *          the property of the model to bind the widget to, must not be null.
   * @param converter
   *          the converter to use for this binding, null if default conversion should be used.
   */
  @SuppressWarnings("unchecked")
  public void bind(final Widget widget, final String property, final Converter converter) {
    // This call ensures an exception is thrown for bindings to non existing properties.
    // Reusing this method for this purpose helps to keep the generated code size smaller.
    proxy.get(property);
    unbind(property);
    if (bindings.containsValue(widget)) {
      throw new RuntimeException("Widget already bound to a different property!");
    }
    bindings.put(property, widget);
    converters.put(property, converter);
    if (widget instanceof HasValue) {
      handlerRegistrations.put(property, ((HasValue) widget).addValueChangeHandler(new ValueChangeHandler() {
        @Override
        public void onValueChange(ValueChangeEvent event) {
          Object oldValue = proxy.get(property);

          Object value = Convert.toModelValue(propertyTypes.get(property),
              bindings.get(property), event.getValue(), converters.get(property));
          proxy.set(property, value);

          propertyChangeHandlerSupport.notifyHandlers(new PropertyChangeEvent(proxy, property, oldValue, value));
        }
      }));
    }
    syncState(widget, property, initialState);
  }

  /**
   * Updates bound widgets and fires {@link PropertyChangeEvent}s.
   * 
   * @param <P>
   *          The property type of the changed property.
   * @param source
   *          The source object.
   * @param property
   *          The name of the property that changed.
   * @param oldValue
   *          The old value of the property.
   * @param newValue
   *          The new value of the property.
   */
  @SuppressWarnings("unchecked")
  <P> void updateWidgetAndFireEvents(final String property, final P oldValue, final P newValue) {
    Widget widget = bindings.get(property);
    if (widget instanceof HasValue) {
      HasValue hv = (HasValue) widget;
      Object widgetValue =
          Convert.toWidgetValue(widget, propertyTypes.get(property), newValue, converters.get(property));
      hv.setValue(widgetValue, true);
    }
    else if (widget instanceof HasText) {
      HasText ht = (HasText) widget;
      Object widgetValue =
          Convert.toWidgetValue(String.class, propertyTypes.get(property), newValue, converters.get(property));
      ht.setText((String) widgetValue);
    }

    PropertyChangeEvent<P> event = new PropertyChangeEvent<P>(proxy, property, oldValue, newValue);
    propertyChangeHandlerSupport.notifyHandlers(event);
  }

  /**
   * Synchronizes the state of the model and the bound widgets based on the value of the provided {@link InitialState}.
   * 
   * @param initialState
   *          Specifies the origin of the initial state of both model and UI widget. Null if no initial state
   *          synchronization should be carried out.
   */
  void syncState(final InitialState initialState) {
    for (String property : bindings.keySet()) {
      syncState(bindings.get(property), property, initialState);
    }
  }

  @SuppressWarnings("unchecked")
  private void syncState(final Widget widget, final String property, final InitialState initialState) {
    if (initialState != null) {
      Object value = null;
      if (widget instanceof HasValue) {
        HasValue hasValue = (HasValue) widget;
        value = initialState.getInitialValue(proxy.get(property), hasValue.getValue());
        if (initialState == InitialState.FROM_MODEL) {
          Object widgetValue =
              Convert.toWidgetValue(widget, propertyTypes.get(property), value, converters.get(property));
          hasValue.setValue(widgetValue);
        }
      }
      else if (widget instanceof HasText) {
        HasText hasText = (HasText) widget;
        value = initialState.getInitialValue(proxy.get(property), hasText.getText());
        if (initialState == InitialState.FROM_MODEL) {
          Object widgetValue =
              Convert.toWidgetValue(String.class, propertyTypes.get(property), value, converters.get(property));
          hasText.setText((String) widgetValue);
        }
      }
      if (initialState == InitialState.FROM_UI) {
        proxy.set(property, value);
      }
    }
  }

  /**
   * Unbinds all properties.
   */
  public void unbind() {
    for (Object reg : handlerRegistrations.keySet()) {
      (handlerRegistrations.get(reg)).removeHandler();
    }
    bindings.clear();
    handlerRegistrations.clear();
    converters.clear();
  }

  /**
   * Unbinds the property with the given name.
   * 
   * @param property
   *          the name of the model property to unbind, must not be null.
   */
  public void unbind(final String property) {
    bindings.remove(property);
    converters.remove(property);
    HandlerRegistration reg = handlerRegistrations.remove(property);
    if (reg != null) {
      reg.removeHandler();
    }
  }

  @Override
  public void addPropertyChangeHandler(PropertyChangeHandler handler) {
    propertyChangeHandlerSupport.addPropertyChangeHandler(handler);
  }

  @Override
  public void addPropertyChangeHandler(String name, PropertyChangeHandler handler) {
    propertyChangeHandlerSupport.addPropertyChangeHandler(name, handler);
  }

  @Override
  public void removePropertyChangeHandler(PropertyChangeHandler handler) {
    propertyChangeHandlerSupport.removePropertyChangeHandler(handler);
  }

  @Override
  public void removePropertyChangeHandler(String name, PropertyChangeHandler handler) {
    propertyChangeHandlerSupport.removePropertyChangeHandler(name, handler);
  }
}
