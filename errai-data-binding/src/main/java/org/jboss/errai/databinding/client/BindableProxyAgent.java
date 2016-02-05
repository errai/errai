/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.databinding.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.databinding.client.api.Convert;
import org.jboss.errai.databinding.client.api.Converter;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.StateSync;
import org.jboss.errai.databinding.client.api.PropertyChangeEvent;
import org.jboss.errai.databinding.client.api.PropertyChangeHandler;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.ValueBoxBase;
import com.google.gwt.user.client.ui.Widget;

/**
 * Manages bindings and acts in behalf of a {@link BindableProxy} to keep the
 * target model and bound widgets in sync.
 * <p>
 * An agent will:
 * <ul>
 * <li>Carry out an initial state sync between the bound widgets and the target
 * model, if specified (see {@link DataBinder#DataBinder(Object, StateSync)})
 * </li>
 *
 * <li>Update the bound widget when a setter method is invoked on the model (see
 * {@link #updateWidgetsAndFireEvent(String, Object, Object)}). Works for
 * widgets that either implement {@link TakesValue} or {@link HasText})</li>
 *
 * <li>Update the bound widgets when a non-accessor method is invoked on the
 * model (by comparing all bound properties to detect changes). See
 * {@link #updateWidgetsAndFireEvents()}. Works for widgets that either
 * implement {@link TakesValue} or {@link HasText})</li>
 *
 * <li>Update the target model in response to value change events (only works
 * for bound widgets that implement {@link HasValue})</li>
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
  StateSync initialState;

  /**
   * Updates a bound property of a model in response to UI changes.
   */
  private interface ModelUpdater {
    public void update(Object value);
  }

  BindableProxyAgent(BindableProxy<T> proxy, T target, StateSync initialState) {
    this.proxy = proxy;
    this.target = target;
    this.initialState = initialState;
  }

  /**
   * Copies the values of all properties to be able to compare them in case they
   * change outside a setter method.
   */
  void copyValues() {
    for (String property : propertyTypes.keySet()) {
      knownValues.put(property, proxy.get(property));
    }
  }

  /**
   * Binds the provided widget to the specified property (or property chain) of
   * the model instance associated with this proxy (see
   * {@link DataBinder#setModel(Object, StateSync)}).
   *
   * @param widget
   *          the widget to bind to, must not be null.
   * @param property
   *          the property of the model to bind the widget to, must not be null.
   * @param converter
   *          the converter to use for this binding, null if default conversion
   *          should be used.
   * @return binding the created binding.
   */
  public Binding bind(final Widget widget, final String property, final Converter converter) {
    return bind(widget, property, converter, false);
  }

  /**
   * Binds the provided widget to the specified property (or property chain) of
   * the model instance associated with this proxy (see
   * {@link DataBinder#setModel(Object, StateSync)}).
   *
   * @param widget
   *          the widget to bind to, must not be null.
   * @param property
   *          the property of the model to bind the widget to, must not be null.
   * @param userProvidedConverter
   *          the converter to use for this binding, null if default conversion
   *          should be used.
   * @param bindOnKeyUp
   *          a flag indicating that the property should be updated when the
   *          widget fires a {@link com.google.gwt.event.dom.client.KeyUpEvent}
   *          along with the default
   *          {@link com.google.gwt.event.logical.shared.ValueChangeEvent}.
   * @return binding the created binding.
   */
  public Binding bind(final Widget widget, final String property, Converter userProvidedConverter, final boolean bindOnKeyUp) {
    validatePropertyExpr(property);

    final Converter converter;

    int dotPos = property.indexOf(".");
    if (dotPos > 0) {
      DataBinder nested = createNestedBinder(property);
      converter = findConverter(property, getNestedPropertyType(property), widget, userProvidedConverter);
      nested.bind(widget, property.substring(dotPos + 1), converter, bindOnKeyUp);
      Binding binding = new Binding(property, widget, converter, null);
      bindings.put(property, binding);
      return binding;
    }
    else if (propertyTypes.containsKey(property)) {
      converter = findConverter(property, propertyTypes.get(property).getType(), widget, userProvidedConverter);
    }
    else {
      throw new NonExistingPropertyException(property);
    }

    for (Binding binding : bindings.values()) {
      if (binding.getWidget().equals(widget) && !property.equals(binding.getProperty())) {
        throw new WidgetAlreadyBoundException("Widget already bound to property: " + binding.getProperty());
      }
    }

    Map<Class <? extends GwtEvent>, HandlerRegistration> handlerMap = addWidgetHandlers(widget, bindOnKeyUp, new ModelUpdater() {
      @Override
      public void update(final Object value) {
        final Object oldValue = proxy.get(property);
        final Object newValue = converter.toModelValue(value);
        try {
          proxy.set(property, newValue);
        } catch (Throwable t) {
                  throw new RuntimeException("Error while setting property [" + property + "] to [" + newValue
                          + "] converted from [" + value + "] with converter [" + converter.getModelType().getName()
                          + " -> " + converter.getWidgetType().getName() + "].", t);
                }
        updateWidgetsAndFireEvent(property, oldValue, newValue, widget);
      }
    });

    Binding binding = new Binding(property, widget, converter, handlerMap);
    bindings.put(property, binding);

    if (propertyTypes.get(property).isList()) {
      proxy.set(property, ensureBoundListIsProxied(property));
    }
    syncState(widget, property, converter);

    return binding;
  }

  private Class<?> getNestedPropertyType(final String property) {
    final String[] subProperties = property.split("\\.");
    BindableProxyAgent<?> agent = this;
    for (int i = 0; i < subProperties.length - 1; i++) {
      final String subProperty = subProperties[i];
      final DataBinder binder = Assert.notNull(
              "Could not find subproperty " + i + ", " + subProperty + ", in " + property,
              agent.binders.get(subProperty));
      agent = ((BindableProxy<?>) binder.getModel()).getBindableProxyAgent();
    }
    final String lastSubProperty = subProperties[subProperties.length-1];

    return Assert.notNull("Could not find last subproperty, " + lastSubProperty + ", in " + property,
            agent.propertyTypes.get(lastSubProperty)).getType();
  }

  private Converter findConverter(final String property, final Class<?> propertyType, final Widget widget, final Converter userProvidedConverter) {
    final Class<?> widgetValueType = Convert.inferWidgetValueType(widget, propertyType);
    if (userProvidedConverter != null) {
      validateTypes(property, propertyType, userProvidedConverter.getModelType(), "model");
      validateTypes(property, widgetValueType, userProvidedConverter.getWidgetType(), "widget");

      return userProvidedConverter;
    }
    else {
      final Converter<?, ?> converter = Convert.getConverter(propertyType, widgetValueType);

      if (converter == null) {
        throw new RuntimeException(
                "Cannot convert between " + propertyType.getName() + " and " + widgetValueType.getName()
                        + " for property [" + property + "] in " + proxy.unwrap().getClass().getName());
      }

      return converter;
    }
  }

  private void validateTypes(final String property, final Class<?> actualType, final Class<?> converterType, final String modelOrWidget) {
    if (!actualType.equals(converterType) && !oneTypeIsInterface(actualType, converterType)) {
      throw new RuntimeException("Converter " + modelOrWidget + " type, " + converterType.getName()
              + ", does not match the required type, " + actualType.getName());
    }
  }

  private boolean oneTypeIsInterface(Class<?> propertyType, Class<?> converterModelType) {
    return propertyType.isInterface() ^ converterModelType.isInterface();
  }

  /**
   * Adds the required event handlers to the provided widget. A
   * {@link ValueChangeHandler} is added by default, a {@link KeyUpHandler} is
   * added if bindOnKeyUp is true.
   *
   * @param widget
   *          the bound widget, must not be null.
   * @param bindOnKeyUp
   *          a flag indicating that the property should be updated when the
   *          widget fires a {@link com.google.gwt.event.dom.client.KeyUpEvent}
   *          , in addition to the default
   *          {@link com.google.gwt.event.logical.shared.ValueChangeEvent}.
   * @param updater
   *          A {@link ModelUpdater} that updates the bound property of the
   *          model in response to UI changes.
   * @return collection of event handler registrations.
   */
  private Map<Class<? extends GwtEvent>, HandlerRegistration> addWidgetHandlers(final Widget widget,
                                                              final boolean bindOnKeyUp, final ModelUpdater updater) {

    HashMap<Class<? extends GwtEvent>, HandlerRegistration> handlerMap =
                                                          new HashMap<Class<? extends GwtEvent>, HandlerRegistration>();

    if (widget instanceof HasValue) {
      final HandlerRegistration valueHandlerReg = ((HasValue) widget).addValueChangeHandler(event -> {
        final Object value = ((HasValue) widget).getValue();
        updater.update(value);
      });
      handlerMap.put(ValueChangeEvent.class, valueHandlerReg);
    }
    else if (!(widget instanceof HasText) && !(widget instanceof TakesValue)) {
      throw new RuntimeException("Widget must implement either " + TakesValue.class.getName() + " or "
              + HasText.class.getName() + "!");
    }

    if (bindOnKeyUp) {
      if (widget instanceof ValueBoxBase) {
        HandlerRegistration keyUpHandlerReg = ((ValueBoxBase) widget)
                .addKeyUpHandler(event -> updater.update(((ValueBoxBase) widget).getText()));
        handlerMap.put(KeyUpEvent.class, keyUpHandlerReg);
      }
      else {
        throw new RuntimeException("Cannot bind widget " + widget.toString() + " on KeyUpEvents, " + widget.toString()
                + " is not an instance of ValueBoxBase");
      }
    }

    return handlerMap;
  }

  /**
   * Creates a data binder for a nested property to support property chains. The
   * nested data binder is initialized with the current value of the specified
   * property, or with a new instance of the property type if the value is null.
   * The proxy's value for this property is then replaced with the proxy managed
   * by the nested data binder.
   *
   * @param property
   *          the property of the model to bind the widget to, must not be null.
   *          The property must be of a @Bindable type.
   */
  private DataBinder createNestedBinder(final String property) {
    String bindableProperty = property.substring(0, property.indexOf("."));
    DataBinder<Object> binder = binders.get(bindableProperty);

    if (!propertyTypes.containsKey(bindableProperty)) {
      throw new NonExistingPropertyException(bindableProperty);
    }

    if (!propertyTypes.get(bindableProperty).isBindable()) {
      throw new RuntimeException("The type of property " + bindableProperty + " ("
              + propertyTypes.get(bindableProperty).getType().getName() + ") is not a @Bindable type!");
    }

    if (binder == null) {
      if (proxy.get(bindableProperty) == null) {
        binder = DataBinder.forType(propertyTypes.get(bindableProperty).getType(), initialState);
      }
      else {
        binder = DataBinder.forModel(proxy.get(bindableProperty), initialState);
      }
      binders.put(bindableProperty, binder);
      for (PropertyChangeHandler<?> handler : propertyChangeHandlerSupport.specificPropertyHandlers.get("**")) {
        binder.addPropertyChangeHandler("**", handler);
      }
    }
    else {
      binder.setModel(proxy.get(bindableProperty), initialState, true);
    }
    proxy.set(bindableProperty, binder.getModel());
    knownValues.put(bindableProperty, binder.getModel());

    if (property.indexOf('.') != property.lastIndexOf('.')) {
      ((BindableProxy<?>) binder.getModel()).getBindableProxyAgent().createNestedBinder(property.substring(property.indexOf('.')+1));
    }

    return binder;
  }

  private void validatePropertyExpr(String property) {
    if (property.startsWith(".") || property.endsWith(".")) {
      throw new InvalidPropertyExpressionException(
              "Binding expression (property chain) cannot start or end with '.' : " + property);
    }
    if (property.contains("*.")) {
      throw new InvalidPropertyExpressionException("Wildcards can only appear at the end of property expressions : "
              + property);
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
      final String bindableProperty = property.substring(0, dotPos);
      final DataBinder binder = binders.get(bindableProperty);
      if (binder != null) {
        final BindableProxyAgent<T> nestedAgent = ((BindableProxy<T>) binder.getModel()).getBindableProxyAgent();
        final Collection<Binding> nestedBindings = nestedAgent.bindings.get(property.substring(dotPos + 1));
        for (Binding nestedBinding : nestedBindings.toArray(new Binding[nestedBindings.size()])) {
          if (binding.getWidget() == nestedBinding.getWidget()) {
            nestedAgent.unbind(nestedBinding);
          }
        }
      }
    }
    binding.removeHandlers();
    bindings.remove(property, binding);

    if (bindings.isEmpty()) {
      BindableProxyFactory.removeCachedProxyForModel(target);
    }
  }

  /**
   * Updates all bound widgets if necessary (if a bound property's value has
   * changed). This method is invoked in case a bound property changed outside
   * the property's write method (when using a non accessor method).
   */
  void updateWidgetsAndFireEvents() {
    for (String property : propertyTypes.keySet()) {
      Object knownValue = knownValues.get(property);
      Object actualValue = proxy.get(property);

      if ((knownValue == null && actualValue != null) || (knownValue != null && !knownValue.equals(actualValue))) {

        DataBinder nestedBinder = binders.get(property);
        if (nestedBinder != null) {
          nestedBinder.setModel(actualValue, StateSync.FROM_MODEL, true);
          proxy.set(property, nestedBinder.getModel());
        }
        updateWidgetsAndFireEvent(property, knownValue, actualValue);
      }
    }
  }

  /**
   * Updates all bound widgets and fires the corresponding
   * {@link PropertyChangeEvent}.
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
   * Updates all bound widgets and fires the corresponding
   * {@link PropertyChangeEvent}.
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
   *          A widget reference that does not need to be updated (the origin of
   *          the value change event).
   */
  private <P> void updateWidgetsAndFireEvent(final String property, final P oldValue, final P newValue,
          final Widget excluding) {

    for (Binding binding : bindings.get(property)) {
      final Widget widget = binding.getWidget();
      final Converter converter = binding.getConverter();

      if (widget == excluding)
        continue;

      if (widget instanceof TakesValue) {
        TakesValue hv = (TakesValue) widget;
        Object widgetValue = converter.toWidgetValue(newValue);
        hv.setValue(widgetValue);
      }
      else if (widget instanceof HasText) {
        HasText ht = (HasText) widget;
        assert String.class.equals(converter.getWidgetType());
        Object widgetValue = converter.toWidgetValue(newValue);
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
    knownValues.put(property, newValue);

    PropertyChangeEvent<P> event = new PropertyChangeEvent<P>(proxy, Assert.notNull(property), oldValue, newValue);
    propertyChangeHandlerSupport.notifyHandlers(event);
  }

  /**
   * Synchronizes the state of the provided widgets and model property based on
   * the value of the provided {@link StateSync}.
   *
   * @param widget
   *          The widget to synchronize. Must not be null.
   * @param property
   *          The name of the model property that should be synchronized. Must
   *          not be null.
   * @param converter
   *          The converter specified for the property binding. Null allowed.
   */
  private void syncState(final Widget widget, final String property, final Converter converter) {
    Assert.notNull(widget);
    Assert.notNull(property);

    if (initialState != null) {
      Object value = proxy.get(property);
      if (widget instanceof TakesValue) {
        value = initialState.getInitialValue(value, ((TakesValue) widget).getValue());
      }
      else if (widget instanceof HasText) {
        value = initialState.getInitialValue(value, ((HasText) widget).getText());
      }

      if (initialState == StateSync.FROM_MODEL) {
        updateWidgetsAndFireEvent(property, knownValues.get(property), value);
      }
      else if (initialState == StateSync.FROM_UI) {
        Object newValue = converter.toModelValue(value);
        proxy.set(property, newValue);
        firePropertyChangeEvent(property, knownValues.get(property), newValue);
        updateWidgetsAndFireEvent(property, knownValues.get(property), newValue, widget);
      }
    }
  }

  /**
   * Ensures that the given list property is wrapped in a
   * {@link BindableListWrapper}, so changes to the list become observable.
   *
   * @param property
   *          the name of the list property
   *
   * @return a new the wrapped (proxied) list or the provided list if already
   *         proxied
   */
  private List ensureBoundListIsProxied(String property) {
    List newList = ensureBoundListIsProxied(property, (List) proxy.get(property));
    updateWidgetsAndFireEvent(property, proxy.get(property), newList);
    return newList;
  }

  /**
   * Ensures that the given list property is wrapped in a
   * {@link BindableListWrapper}, so changes to the list become observable.
   *
   * @param property
   *          the name of the list property
   * @param list
   *          the list that needs to be proxied
   *
   * @return a new the wrapped (proxied) list or the provided list if already
   *         proxied
   */
  List ensureBoundListIsProxied(final String property, final List list) {
    if (!(list instanceof BindableListWrapper) && bindings.containsKey(property) && list != null) {
      final BindableListWrapper newList = new BindableListWrapper(list);
      newList.addChangeHandler(new UnspecificListChangeHandler() {
        @Override
        void onListChanged(List oldList) {
          updateWidgetsAndFireEvent(property, oldList, newList);
        }
      });

      return newList;
    }

    return list;
  }

  /**
   * Returns the {@link StateSync} configured when the proxy was created.
   *
   * @return initial state, can be null.
   */
  public StateSync getInitialState() {
    return initialState;
  }

  /**
   * Configures the {@link StateSync}.
   */
  public void setInitialState(StateSync initialState) {
    this.initialState = initialState;
  }

  /**
   * Returns the {@link PropertyChangeHandlerSupport} object of this agent
   * containing all change handlers that have been registered for the
   * corresponding model proxy.
   *
   * @return propertyChangeHandlerSupport object, never null.
   */
  public PropertyChangeHandlerSupport getPropertyChangeHandlers() {
    return propertyChangeHandlerSupport;
  }

  @Override
  public PropertyChangeUnsubscribeHandle addPropertyChangeHandler(final PropertyChangeHandler handler) {
    propertyChangeHandlerSupport.addPropertyChangeHandler(handler);

    return new OneTimeUnsubscribeHandle() {

      @Override
      public void doUnsubscribe() {
        propertyChangeHandlerSupport.removePropertyChangeHandler(handler);
      }
    };
  }

  @Override
  public <P> PropertyChangeUnsubscribeHandle addPropertyChangeHandler(final String property, final PropertyChangeHandler<P> handler) {
    validatePropertyExpr(property);

    final Collection<PropertyChangeUnsubscribeHandle> unsubHandles = new ArrayList<PropertyChangeUnsubscribeHandle>();

    int dotPos = property.indexOf(".");
    if (dotPos > 0) {
      DataBinder nested = createNestedBinder(property);
      unsubHandles.add(nested.addPropertyChangeHandler(property.substring(dotPos + 1), handler));
    }
    else if (property.equals("*")) {
      propertyChangeHandlerSupport.addPropertyChangeHandler(handler);
      unsubHandles.add(new PropertyChangeUnsubscribeHandle() {

        @Override
        public void unsubscribe() {
          propertyChangeHandlerSupport.removePropertyChangeHandler(handler);
        }
      });
    }
    else if (property.equals("**")) {
      for (DataBinder nested : binders.values()) {
        unsubHandles.add(nested.addPropertyChangeHandler(property, handler));
      }
      propertyChangeHandlerSupport.addPropertyChangeHandler(handler);
      unsubHandles.add(new PropertyChangeUnsubscribeHandle() {

        @Override
        public void unsubscribe() {
          propertyChangeHandlerSupport.removePropertyChangeHandler(handler);
        }
      });
    }

    propertyChangeHandlerSupport.addPropertyChangeHandler(property, handler);
    unsubHandles.add(new PropertyChangeUnsubscribeHandle() {

      @Override
      public void unsubscribe() {
        propertyChangeHandlerSupport.removePropertyChangeHandler(property, handler);
      }
    });

    return new OneTimeUnsubscribeHandle() {

      @Override
      public void doUnsubscribe() {
        for (final PropertyChangeUnsubscribeHandle handle : unsubHandles) {
          handle.unsubscribe();
        }
      }
    };
  }

  /**
   * Merges the provided {@link PropertyChangeHandler}s of the provided agent
   * instance. If a handler instance is already registered on this agent, it
   * will NOT be added again.
   *
   * @param pchs
   *          the instance who's change handlers will be merged, must not be
   *          null.
   */
  public void mergePropertyChangeHandlers(PropertyChangeHandlerSupport pchs) {
    Assert.notNull(pchs);

    for (PropertyChangeHandler pch : pchs.handlers) {
      if (!propertyChangeHandlerSupport.handlers.contains(pch)) {
        addPropertyChangeHandler(pch);
      }
    }

    for (String pchKey : pchs.specificPropertyHandlers.keys()) {
      for (PropertyChangeHandler pch : pchs.specificPropertyHandlers.get(pchKey)) {
        if (!propertyChangeHandlerSupport.specificPropertyHandlers.containsEntry(pchKey, pch)) {
          addPropertyChangeHandler(pchKey, pch);
        }
      }
    }
  }

  /**
   * Compares property values between this agent and the provided agent
   * recursively and fires {@link PropertyChangeEvent}s for all differences.
   *
   * @param other
   *          the agent to compare against.
   */
  public void fireChangeEvents(BindableProxyAgent other) {
    for (String property : propertyTypes.keySet()) {
      final Object curValue,
                   oldValue,
                   thisValue = knownValues.get(property),
                   otherValue = other.knownValues.get(property);

      final StateSync initalState = (getInitialState() != null ? getInitialState() : StateSync.FROM_MODEL);
      curValue = initalState.getInitialValue(thisValue, otherValue);
      oldValue = initalState.getInitialValue(otherValue, thisValue);

      if ((curValue == null && oldValue != null) || (curValue != null && !curValue.equals(oldValue))) {

        DataBinder nestedBinder = binders.get(property);
        DataBinder otherNestedBinder = (DataBinder) other.binders.get(property);
        if (nestedBinder != null && otherNestedBinder != null) {
          BindableProxyAgent nestedAgent = ((BindableProxy<T>) nestedBinder.getModel()).getBindableProxyAgent();
          BindableProxyAgent otherNestedAgent = ((BindableProxy<T>) otherNestedBinder.getModel()).getBindableProxyAgent();
          nestedAgent.fireChangeEvents(otherNestedAgent);
        }

        firePropertyChangeEvent(property, oldValue, curValue);
      }
    }
  }
}
