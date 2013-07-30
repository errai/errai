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

import static org.jboss.errai.databinding.client.api.Convert.toModelValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.databinding.client.api.Convert;
import org.jboss.errai.databinding.client.api.Converter;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.InitialState;
import org.jboss.errai.databinding.client.api.PropertyChangeEvent;
import org.jboss.errai.databinding.client.api.PropertyChangeHandler;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

/**
 * Manages bindings and acts in behalf of a {@link BindableProxy} to keep the target model and bound
 * widgets in sync.
 * <p>
 * An agent will:
 * <ul>
 * <li>Carry out an initial state sync between the bound widgets and the target model, if specified
 * (see {@link DataBinder#DataBinder(Object, InitialState)})</li>
 * 
 * <li>Update the bound widget when a setter method is invoked on the model (see
 * {@link #updateWidgetsAndFireEvent(String, Object, Object)}). Works for widgets that either
 * implement {@link HasValue} or {@link HasText})</li>
 * 
 * <li>Update the bound widgets when a non-accessor method is invoked on the model (by comparing all
 * bound properties to detect changes). See {@link #updateWidgetsAndFireEvents()}. Works for widgets
 * that either implement {@link HasValue} or {@link HasText})</li>
 * 
 * <li>Update the target model in response to value change events (only works for bound widgets that
 * implement {@link HasValue})</li>
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
  final Multimap<String, Binding> bindings = LinkedHashMultimap.create();
  final Map<String, PropertyType> propertyTypes = new HashMap<String, PropertyType>();
  final Map<String, DataBinder> binders = new HashMap<String, DataBinder>();
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
   * Copies the values of all properties to be able to compare them in case they change outside a
   * setter method.
   */
  void copyValues() {
    for (String property : propertyTypes.keySet()) {
      knownValues.put(property, proxy.get(property));
    }
  }

  /**
   * Binds the provided widget to the specified property (or property chain) of the model instance
   * associated with this proxy (see {@link DataBinder#setModel(Object, InitialState)}).
   * 
   * @param widget
   *          the widget to bind to, must not be null.
   * @param property
   *          the property of the model to bind the widget to, must not be null.
   * @param converter
   *          the converter to use for this binding, null if default conversion should be used.
   * @return binding the created binding.
   */
  public Binding bind(final Widget widget, final String property, final Converter converter) {
    validatePropertyExpr(property);

    if (property.indexOf(".") > 0) {
      createNestedBinders(widget, property, converter);
      Binding binding = new Binding(property, widget, converter, null);
      bindings.put(property, binding);
      return binding;
    }

    if (!propertyTypes.containsKey(property)) {
      throw new NonExistingPropertyException(property);
    }

    for (Binding binding : bindings.values()) {
      if (binding.getWidget().equals(widget) && !property.equals(binding.getProperty())) {
        throw new WidgetAlreadyBoundException("Widget already bound to property: " + binding.getProperty());
      }
    }

    HandlerRegistration handlerRegistration = null;
    if (widget instanceof HasValue) {
      handlerRegistration = ((HasValue) widget).addValueChangeHandler(new ValueChangeHandler() {
        @Override
        public void onValueChange(ValueChangeEvent event) {
          Object oldValue = proxy.get(property);
          Object newValue = toModelValue(propertyTypes.get(property).getType(), widget, event.getValue(), converter);
          proxy.set(property, newValue);
          updateWidgetsAndFireEvent(property, oldValue, newValue, widget);
        }
      });
    }
    else if (!(widget instanceof HasText)) {
      throw new RuntimeException("Widget must implement either " + HasValue.class.getName() +
          " or " + HasText.class.getName() + "!");
    }

    Binding binding = new Binding(property, widget, converter, handlerRegistration);
    bindings.put(property, binding);

    if (propertyTypes.get(property).isList()) {
      proxy.set(property, ensureBoundListIsProxied(property));
    }
    syncState(widget, property, initialState);

    return binding;
  }

  /**
   * Creates a data binder for a nested property to support property chains. The nested data binder
   * is initialized with the current value of the specified property, or with a new instance of the
   * property type if the value is null. The proxy's value for this property is then replaced with
   * the proxy managed by the nested data binder.
   * 
   * @param widget
   *          the widget to bind to, must not be null.
   * @param property
   *          the property of the model to bind the widget to, must not be null. The property must
   *          be of a @Bindable type.
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
      else {
        binder.setModel(proxy.get(bindableProperty), initialState);
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
   * Unbinds the property with the given name.
   * 
   * @param property
   *          the name of the model property to unbind, must not be null.
   */
  public void unbind(final Binding binding) {
    String property = binding.getProperty();
    validatePropertyExpr(property);

    int dotPos = property.indexOf(".");
    if (dotPos > 0) {
      String bindableProperty = property.substring(0, dotPos);
      DataBinder binder = binders.get(bindableProperty);
      if (binder != null) {
        binder.unbind(property.substring(dotPos + 1));
      }
    }
    binding.removeHandler();
    bindings.remove(property, binding);

    if (bindings.isEmpty()) {
      BindableProxyFactory.removeCachedProxyForModel(target);
    }
  }

  /**
   * Updates all bound widgets if necessary (if a bound property's value has changed). This method
   * is invoked in case a bound property changed outside the property's write method (when using a
   * non accessor method).
   */
  void updateWidgetsAndFireEvents() {
    for (String property : propertyTypes.keySet()) {
      Object knownValue = knownValues.get(property);
      Object actualValue = proxy.get(property);

      if ((knownValue == null && actualValue != null) ||
          (knownValue != null && !knownValue.equals(actualValue))) {

        DataBinder nestedBinder = binders.get(property);
        if (nestedBinder != null) {
          nestedBinder.setModel(actualValue, InitialState.FROM_MODEL);
          proxy.set(property, nestedBinder.getModel());
        }
        updateWidgetsAndFireEvent(property, knownValue, actualValue);
      }
    }
  }

  /**
   * Updates all bound widgets and fires the corresponding {@link PropertyChangeEvent}.
   * 
   * @param <P>
   *          The property type of the changed property.
   * @param property
   *          The name of the property that changed. Must not be null.
   * @param oldValue
   *          The old value of the property.
   * @param newValue
   *          The new value of the property.
   */
  <P> void updateWidgetsAndFireEvent(final String property, final P oldValue, final P newValue) {
    updateWidgetsAndFireEvent(property, oldValue, newValue, null);
  }

  /**
   * Updates all bound widgets and fires the corresponding {@link PropertyChangeEvent}.
   * 
   * @param <P>
   *          The property type of the changed property.
   * @param property
   *          The name of the property that changed.
   * @param oldValue
   *          The old value of the property.
   * @param newValue
   *          The new value of the property.
   * @param excluding
   *          A widget reference that does not need to be updated (the origin of the value change
   *          event).
   */
  private <P> void updateWidgetsAndFireEvent(final String property, final P oldValue, final P newValue,
      final Widget excluding) {

    for (Binding binding : bindings.get(property)) {
      Widget widget = binding.getWidget();
      Converter converter = binding.getConverter();

      if (widget == excluding)
        continue;

      if (widget instanceof HasValue) {
        HasValue hv = (HasValue) widget;
        Object widgetValue =
            Convert.toWidgetValue(widget, propertyTypes.get(property).getType(), newValue, converter);
        hv.setValue(widgetValue);
      }
      else if (widget instanceof HasText) {
        HasText ht = (HasText) widget;
        Object widgetValue =
            Convert.toWidgetValue(String.class, propertyTypes.get(property).getType(), newValue, converter);
        ht.setText((String) widgetValue);
      }
    }

    firePropertyChangeEvent(property, oldValue, newValue);
  }

  /**
   * Fires a property change event.
   * 
   * @param <P>
   *          The property type of the changed property.
   * @param property
   *          The name of the property that changed. Must not be null.
   * @param oldValue
   *          The old value of the property.
   * @param newValue
   *          The new value of the property.
   */
  private <P> void firePropertyChangeEvent(final String property, final P oldValue, final P newValue) {
    PropertyChangeEvent<P> event = new PropertyChangeEvent<P>(proxy, Assert.notNull(property), oldValue, newValue);
    propertyChangeHandlerSupport.notifyHandlers(event);

    knownValues.put(property, newValue);
  }

  /**
   * Synchronizes the state of the provided widgets and model property based on the value of the
   * provided {@link InitialState}.
   * 
   * @param widget
   *          The widget to synchronize. Must not be null.
   * @param property
   *          The name of the model property that should be synchronized. Must not be null.
   * @param initialState
   *          Specifies the origin of the initial state of both model and UI widget. If null, no
   *          state synchronization should be carried out.
   */
  private void syncState(final Widget widget, final String property, final InitialState initialState) {
    Assert.notNull(widget);
    Assert.notNull(property);

    if (initialState != null) {
      Object value = proxy.get(property);
      if (widget instanceof HasValue) {
        value = initialState.getInitialValue(value, ((HasValue) widget).getValue());
      }
      else if (widget instanceof HasText) {
        value = initialState.getInitialValue(value, ((HasText) widget).getText());
      }

      if (initialState == InitialState.FROM_MODEL) {
        updateWidgetsAndFireEvent(property, knownValues.get(property), value);
      }
      else if (initialState == InitialState.FROM_UI) {
        proxy.set(property, value);
        firePropertyChangeEvent(property, knownValues.get(property), value);
      }
    }
  }

  /**
   * Ensures that the given list property is wrapped in a {@link BindableListWrapper}, so changes to
   * the list become observable.
   * 
   * @param property
   *          the name of the list property
   * 
   * @return a new the wrapped (proxied) list or the provided list if already proxied
   */
  private List ensureBoundListIsProxied(String property) {
    List newList = ensureBoundListIsProxied(property, (List) proxy.get(property));
    updateWidgetsAndFireEvent(property, proxy.get(property), newList);
    return newList;
  }

  /**
   * Ensures that the given list property is wrapped in a {@link BindableListWrapper}, so changes to
   * the list become observable.
   * 
   * @param property
   *          the name of the list property
   * @param list
   *          the list that needs to be proxied
   * 
   * @return a new the wrapped (proxied) list or the provided list if already proxied
   */
  List ensureBoundListIsProxied(final String property, final List list) {
    if (!(list instanceof BindableListWrapper) && bindings.containsKey(property) && list != null) {
      final BindableListWrapper newList = new BindableListWrapper(list);
      newList.addChangeHandler(new UnspecificListChangeHandler() {
        @Override
        void onListChanged(List oldList) {
          firePropertyChangeEvent(property, oldList, newList);
        }
      });

      return newList;
    }

    return list;
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
   * Returns the {@link PropertyChangeHandlerSupport} object of this agent containing all change
   * handlers that have been registered for the corresponding model proxy.
   * 
   * @return propertyChangeHandlerSupport object, never null.
   */
  public PropertyChangeHandlerSupport getPropertyChangeHandlers() {
    return propertyChangeHandlerSupport;
  }

  @Override
  public void addPropertyChangeHandler(PropertyChangeHandler handler) {
    propertyChangeHandlerSupport.addPropertyChangeHandler(handler);
  }

  @Override
  public <P> void addPropertyChangeHandler(String property, PropertyChangeHandler<P> handler) {
    propertyChangeHandlerSupport.addPropertyChangeHandler(property, handler);
  }

  @Override
  public void removePropertyChangeHandler(PropertyChangeHandler handler) {
    propertyChangeHandlerSupport.removePropertyChangeHandler(handler);
  }

  @Override
  public void removePropertyChangeHandler(String property, PropertyChangeHandler handler) {
    propertyChangeHandlerSupport.removePropertyChangeHandler(property, handler);
  }
}
