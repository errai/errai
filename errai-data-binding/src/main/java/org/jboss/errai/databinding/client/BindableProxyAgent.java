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
 * Manages bindings and acts in behalf of a {@link BindableProxy} to keep the target model and bound widgets in sync.
 * <p>
 * An agent will:
 * <ul>
 * <li>Carry out an initial state sync between the bound widgets and the target model, if specified (see
 * {@link DataBinder#DataBinder(Object, InitialState)})</li>
 *
 * <li>Update the bound widget when a setter method is invoked on the model (see
 * {@link #updateWidgetAndFireEvents(String, Object, Object)}). Works for widgets that either implement {@link HasValue}
 * or {@link HasText})</li>
 *
 * <li>Update the bound widgets when a non-accessor method is invoked on the model (by comparing all bound properties to
 * detect changes). See {@link #updateWidgetsAndFireEvents()}. Works for widgets that either implement {@link HasValue}
 * or {@link HasText})</li>
 *
 * <li>Update the target model in response to value change events (only works for bound widgets that implement
 * {@link HasValue})</li>
 * <ul>
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 *
 * @param <T>
 *          The type of the target model being proxied.
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public final class BindableProxyAgent<T> implements HasPropertyChangeHandlers {
  final Map<String, PropertyType> propertyTypes = new HashMap<String, PropertyType>();
  final Map<String, Widget> bindings = new HashMap<String, Widget>();
  final Map<String, DataBinder> binders = new HashMap<String, DataBinder>();
  final Map<String, Converter> converters = new HashMap<String, Converter>();
  final Map<String, HandlerRegistration> handlerRegistrations = new HashMap<String, HandlerRegistration>();
  final Map<String, Object> knownValues = new HashMap<String, Object>();

  PropertyChangeHandlerSupport propertyChangeHandlerSupport = new PropertyChangeHandlerSupport();

  final BindableProxy<T> proxy;
  final T target;
  final InitialState initialState;

  BindableProxyAgent(BindableProxy<T> proxy, T target, InitialState initialState) {
    this.proxy = proxy;
    this.target = target;
    this.initialState = initialState;
  }

  /**
   * Makes the settings of this BindableProxyAgent match those of the given
   * agent.
   * <p>
   * IMPORTANT NOTE: this is currently implemented by sharing the
   * PropertyChangeHandler registrations with the given agent. You should
   * discard all references to the "from" agent after calling this method.
   *
   * @param other
   *          the agent to copy/share settings from. Should not be used after
   *          you pass it to this method.
   */
  public void copyStateFrom(BindableProxyAgent<T> other) {
    for (String boundProperty : other.getBoundProperties()) {
      bind(other.getWidget(boundProperty), boundProperty, other.getConverter(boundProperty));
    }

    propertyChangeHandlerSupport = other.propertyChangeHandlerSupport;
  }

  /**
   * Returns a set of the currently bound property names.
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
   * Binds the provided widget to the specified property (or property chain) of the model instance associated with this
   * proxy (see {@link #setModel(Object, InitialState)}).
   *
   * @param widget
   *          the widget to bind to, must not be null.
   * @param property
   *          the property of the model to bind the widget to, must not be null.
   * @param converter
   *          the converter to use for this binding, null if default conversion should be used.
   */
  public void bind(final Widget widget, final String property, final Converter converter) {
    validatePropertyExpr(property);

    if (property.indexOf(".") > 0) {
      createNestedBinders(widget, property, converter);
      bindings.put(property, widget);
      return;
    }

    if (!propertyTypes.containsKey(property)) {
      throw new NonExistingPropertyException(property);
    }

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

          Object value =
              Convert.toModelValue(propertyTypes.get(property).getType(), widget, event.getValue(), converter);
          proxy.set(property, value);

          propertyChangeHandlerSupport
              .notifyHandlers(new PropertyChangeEvent<Object>(proxy, property, oldValue, value));
        }
      }));
    }
    syncState(widget, property, initialState);
  }

  /**
   * Creates a data binder for a nested property to support property chains. The nested data binder is initialized with
   * the current value of the specified property, or with a new instance of the property type if the value is null. The
   * proxy's value for this property is then replaced with the proxy managed by the nested data binder.
   *
   * @param widget
   *          the widget to bind to, must not be null.
   * @param property
   *          the property of the model to bind the widget to, must not be null. The property must be of a @Bindable
   *          type.
   * @param converter
   *          the converter to use for this binding, null if default conversion should be used.
   */
  private void createNestedBinders(final Widget widget, final String property, final Converter converter) {
    int dotPos = property.indexOf(".");
    if (dotPos > 0) {
      String bindableProperty = property.substring(0, dotPos);

      if (!propertyTypes.containsKey(bindableProperty)) {
        throw new NonExistingPropertyException(bindableProperty);
      }

      if (!propertyTypes.get(bindableProperty).isBindable()) {
        throw new RuntimeException("The type of property " + bindableProperty + " ("
            + propertyTypes.get(bindableProperty).getType().getName() + ") is not a @Bindable type!");
      }

      DataBinder<Object> binder = binders.get(bindableProperty);
      if (binder == null) {
        if (proxy.get(bindableProperty) == null) {
          binder = DataBinder.forType(propertyTypes.get(bindableProperty).getType(), initialState);
        }
        else {
          binder = DataBinder.forModel(proxy.get(bindableProperty), initialState);
        }
        binders.put(bindableProperty, binder);
      }
      binder.bind(widget, property.substring(dotPos + 1), converter);
      proxy.set(bindableProperty, binder.getModel());
    }
  }

  private void validatePropertyExpr(String property) {
    if (property.startsWith(".") || property.endsWith(".")) {
      throw new RuntimeException("Binding expression (property chain) cannot start or end with '.' :" + property);
    }
  }

  /**
   * Unbinds all properties.
   */
  public void unbind() {
    for (DataBinder binder : binders.values()) {
      binder.unbind();
    }
    binders.clear();

    for (Object reg : handlerRegistrations.keySet()) {
      (handlerRegistrations.get(reg)).removeHandler();
    }
    handlerRegistrations.clear();

    bindings.clear();
    converters.clear();
    knownValues.clear();
  }

  /**
   * Unbinds the property with the given name.
   *
   * @param property
   *          the name of the model property to unbind, must not be null.
   */
  public void unbind(final String property) {
    validatePropertyExpr(property);

    int dotPos = property.indexOf(".");
    if (dotPos > 0) {
      String bindableProperty = property.substring(0, dotPos);
      DataBinder binder = binders.get(bindableProperty);
      if (binder != null) {
        binder.unbind(property.substring(dotPos + 1));
        return;
      }
    }

    knownValues.remove(property);
    bindings.remove(property);
    converters.remove(property);
    HandlerRegistration reg = handlerRegistrations.remove(property);
    if (reg != null) {
      reg.removeHandler();
    }
  }

  /**
   * Updates all bound widgets if necessary (if a bound property's value has changed). This method is invoked in case a
   * bound property changed outside the property's write method (using a non accessor method).
   *
   * @param <P>
   *          The property type of the changed property.
   */
  void updateWidgetsAndFireEvents() {
    for (String boundProperty : bindings.keySet()) {
      // we don't need to handle property chains here, since the nested binders/proxies take care of that
      if (boundProperty.contains("."))
        continue;

      Object knownValue = knownValues.get(boundProperty);

      Object actualValue = proxy.get(boundProperty);
      if ((knownValue == null && actualValue != null) ||
          (knownValue != null && !knownValue.equals(actualValue))) {
        updateWidgetAndFireEvents(boundProperty, knownValue, actualValue);
      }
    }
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
  <P> void updateWidgetAndFireEvents(final String property, final P oldValue, final P newValue) {
    Widget widget = bindings.get(property);
    if (widget instanceof HasValue) {
      HasValue hv = (HasValue) widget;
      Object widgetValue =
          Convert.toWidgetValue(widget, propertyTypes.get(property).getType(), newValue, converters.get(property));
      hv.setValue(widgetValue, true);
    }
    else if (widget instanceof HasText) {
      HasText ht = (HasText) widget;
      Object widgetValue =
          Convert
              .toWidgetValue(String.class, propertyTypes.get(property).getType(), newValue, converters.get(property));
      ht.setText((String) widgetValue);
    }

    knownValues.put(property, newValue);
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
      int dotPos = property.indexOf(".");
      if (dotPos > 0) {
        String bindableProperty = property.substring(0, dotPos);
        binders.get(bindableProperty).setModel(proxy.get(bindableProperty), initialState);
      }
      else {
        syncState(bindings.get(property), property, initialState);
      }
    }
  }

  private void syncState(final Widget widget, final String property, final InitialState initialState) {
    if (initialState != null) {
      Object value = null;
      if (widget instanceof HasValue) {
        HasValue hasValue = (HasValue) widget;
        value = initialState.getInitialValue(proxy.get(property), hasValue.getValue());
        if (initialState == InitialState.FROM_MODEL) {
          Object widgetValue =
              Convert.toWidgetValue(widget, propertyTypes.get(property).getType(), value, converters.get(property));
          hasValue.setValue(widgetValue);
        }
      }
      else if (widget instanceof HasText) {
        HasText hasText = (HasText) widget;
        value = initialState.getInitialValue(proxy.get(property), hasText.getText());
        if (initialState == InitialState.FROM_MODEL) {
          Object widgetValue =
              Convert.toWidgetValue(String.class, propertyTypes.get(property).getType(), value, converters
                  .get(property));
          hasText.setText((String) widgetValue);
        }
      }
      if (initialState == InitialState.FROM_UI) {
        proxy.set(property, value);
      }
    }
  }

  @Override
  public void addPropertyChangeHandler(PropertyChangeHandler handler) {
    propertyChangeHandlerSupport.addPropertyChangeHandler(handler);
  }

  @Override
  public <P> void addPropertyChangeHandler(String name, PropertyChangeHandler<P> handler) {
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
