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

package org.jboss.errai.databinding.client.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.databinding.client.BindableProxy;
import org.jboss.errai.databinding.client.BindableProxyAgent;
import org.jboss.errai.databinding.client.BindableProxyFactory;
import org.jboss.errai.databinding.client.Binding;
import org.jboss.errai.databinding.client.HasPropertyChangeHandlers;
import org.jboss.errai.databinding.client.InvalidPropertyExpressionException;
import org.jboss.errai.databinding.client.NonExistingPropertyException;
import org.jboss.errai.databinding.client.OneTimeUnsubscribeHandle;
import org.jboss.errai.databinding.client.PropertyChangeHandlerSupport;
import org.jboss.errai.databinding.client.PropertyChangeUnsubscribeHandle;
import org.jboss.errai.databinding.client.WidgetAlreadyBoundException;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides an API to programmatically bind properties of a data model instance
 * (any POJO annotated with {@link Bindable}) to UI fields/widgets. The
 * properties of the model and the UI components will automatically be kept in
 * sync for as long as they are bound.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class DataBinder<T> implements HasPropertyChangeHandlers {
  private final PropertyChangeHandlerSupport propertyChangeHandlerSupport = new PropertyChangeHandlerSupport();
  private final StateSync initialState;
  private Multimap<String, Binding> bindings = LinkedHashMultimap.create();
  
  private T proxy;
  private T paused;

  /**
   * Creates a {@link DataBinder} for a new model instance of the provided type
   * (see {@link #forType(Class)}).
   *
   * @param modelType
   *          The bindable type, must not be null.
   */
  private DataBinder(Class<T> modelType) {
    this(modelType, null);
  }

  /**
   * Creates a {@link DataBinder} for a new model instance of the provided type
   * (see {@link #forType(Class)}), initializing either model or UI widgets from
   * the values defined by {@link StateSync} (see
   * {@link #forModel(Object, StateSync)}).
   *
   * @param modelType
   *          The bindable type, must not be null.
   * @param initialState
   *          Specifies the origin of the initial state of both model and UI
   *          widget. Null if no initial state synchronization should be carried
   *          out.
   */
  private DataBinder(Class<T> modelType, StateSync initialState) {
    this.initialState = initialState;
    this.proxy = BindableProxyFactory.getBindableProxy(Assert.notNull(modelType), initialState);
  }

  /**
   * Creates a {@link DataBinder} for the provided model instance (see
   * {@link #forModel(Object)}).
   *
   * @param model
   *          The instance of a {@link Bindable} type, must not be null.
   */
  private DataBinder(T model) {
    this(Assert.notNull(model), null);
  }

  /**
   * Creates a {@link DataBinder} for the provided model instance, initializing
   * either model or UI widgets from the values defined by {@link StateSync}
   * (see {@link #forModel(Object, StateSync)}).
   *
   * @param model
   *          The instance of a {@link Bindable} type, must not be null.
   * @param initialState
   *          Specifies the origin of the initial state of both model and UI
   *          widget. Null if no initial state synchronization should be carried
   *          out.
   */
  private DataBinder(T model, StateSync initialState) {
    this.initialState = initialState;
    this.proxy = BindableProxyFactory.getBindableProxy(Assert.notNull(model), initialState);
  }

  /**
   * Creates a {@link DataBinder} for a new model instance of the provided type.
   *
   * @param modelType
   *          The bindable type, must not be null.
   */
  public static <T> DataBinder<T> forType(Class<T> modelType) {
    return forType(modelType, null);
  }

  /**
   * Creates a {@link DataBinder} for a new model instance of the provided type,
   * initializing either model or UI widgets from the values defined by
   * {@link StateSync}.
   *
   * @param modelType
   *          The bindable type, must not be null.
   * @param initialState
   *          Specifies the origin of the initial state of both model and UI
   *          widget. Null if no initial state synchronization should be carried
   *          out.
   */
  public static <T> DataBinder<T> forType(Class<T> modelType, StateSync initialState) {
    return new DataBinder<T>(modelType, initialState);
  }

  /**
   * Creates a {@link DataBinder} for the provided model instance.
   *
   * @param model
   *          The instance of a {@link Bindable} type, must not be null.
   */
  public static <T> DataBinder<T> forModel(T model) {
    return forModel(model, null);
  }

  /**
   * Creates a {@link DataBinder} for the provided model instance, initializing
   * either model or UI widgets from the values defined by {@link StateSync}.
   *
   * @param model
   *          The instance of a {@link Bindable} type, must not be null.
   * @param intialState
   *          Specifies the origin of the initial state of both model and UI
   *          widget. Null if no initial state synchronization should be carried
   *          out.
   */
  public static <T> DataBinder<T> forModel(T model, StateSync initialState) {
    return new DataBinder<T>(model, initialState);
  }

  /**
   * Binds the provided widget to the specified property of the model instance
   * associated with this {@link DataBinder}. If the provided widget already
   * participates in another binding managed by this {@link DataBinder}, a
   * {@link WidgetAlreadyBoundException} will be thrown.
   *
   * @param widget
   *          The widget the model instance should be bound to, must not be
   *          null.
   * @param property
   *          The name of the model property that should be used for the
   *          binding, following Java bean conventions. Chained (nested)
   *          properties are supported and must be dot (.) delimited (e.g.
   *          customer.address.street). Must not be null.
   * @return the same {@link DataBinder} instance to support call chaining.
   * @throws NonExistingPropertyException
   *           If the {@code model} does not have a property with the given
   *           name.
   * @throws InvalidPropertyExpressionException
   *           If the provided property chain expression is invalid.
   * @throws WidgetAlreadyBoundException
   *           If the provided {@code widget} is already bound to a property of
   *           the model.
   */
  public DataBinder<T> bind(final Widget widget, final String property) {
    bind(widget, property, null);
    return this;
  }

  /**
   * Binds the provided widget to the specified property of the model instance
   * associated with this {@link DataBinder}. If the provided widget already
   * participates in another binding managed by this {@link DataBinder}, a
   * {@link WidgetAlreadyBoundException} will be thrown.
   *
   * @param widget
   *          The widget the model instance should be bound to, must not be
   *          null.
   * @param property
   *          The name of the model property that should be used for the
   *          binding, following Java bean conventions. Chained (nested)
   *          properties are supported and must be dot (.) delimited (e.g.
   *          customer.address.street). Must not be null.
   * @param converter
   *          The converter to use for the binding, null if default conversion
   *          should be used (see {@link Convert}).
   * @return the same {@link DataBinder} instance to support call chaining.
   * @throws NonExistingPropertyException
   *           If the {@code model} does not have a property with the given
   *           name.
   * @throws InvalidPropertyExpressionException
   *           If the provided property chain expression is invalid.
   * @throws WidgetAlreadyBoundException
   *           If the provided {@code widget} is already bound to a property of
   *           the model.
   */
  public DataBinder<T> bind(final Widget widget, final String property,
          @SuppressWarnings("rawtypes") final Converter converter) {

    return bind(widget, property, converter, false);
  }

  /**
   * Binds the provided widget to the specified property of the model instance
   * associated with this {@link DataBinder}. If the provided widget already
   * participates in another binding managed by this {@link DataBinder}, a
   * {@link WidgetAlreadyBoundException} will be thrown.
   *
   * @param widget
   *          The widget the model instance should be bound to, must not be
   *          null.
   * @param property
   *          The name of the model property that should be used for the
   *          binding, following Java bean conventions. Chained (nested)
   *          properties are supported and must be dot (.) delimited (e.g.
   *          customer.address.street). Must not be null.
   * @param converter
   *          The converter to use for the binding, null if default conversion
   *          should be used (see {@link Convert}).
   * @param bindOnKeyUp
   *          A boolean value that allows models bound to text-based widgets to
   *          be updated on a {@link com.google.gwt.event.dom.client.KeyUpEvent}
   *          as well as the default
   *          {@link com.google.gwt.event .logical.shared.ValueChangeEvent}
   *
   * @return the same {@link DataBinder} instance to support call chaining.
   * @throws NonExistingPropertyException
   *           If the {@code model} does not have a property with the given
   *           name.
   * @throws InvalidPropertyExpressionException
   *           If the provided property chain expression is invalid.
   * @throws WidgetAlreadyBoundException
   *           If the provided {@code widget} is already bound to a property of
   *           the model.
   * @throws InvalidBindEventException
   *           If the bindOnKeyUp flag is true and the {@code widget} does not
   *           extend ValueBoxBase.
   */
  public DataBinder<T> bind(final Widget widget, final String property,
          @SuppressWarnings("rawtypes") final Converter converter, final boolean bindOnKeyUp) {

    Assert.notNull(widget);
    Assert.notNull(property);

    if (!(proxy instanceof BindableProxy<?>)) {
      proxy = BindableProxyFactory.getBindableProxy(Assert.notNull(proxy), initialState);
    }

    Binding binding = getAgent().bind(widget, property, converter, bindOnKeyUp);
    bindings.put(property, binding);
    return this;
  }

  /**
   * Unbinds all widgets bound to the specified model property by previous calls
   * to {@link #bind(HasValue, Object, String)}. This method has no effect if
   * the specified property was never bound.
   *
   * @param property
   *          The name of the property (or a property chain) to unbind, Must not
   *          be null.
   *
   * @return the same {@link DataBinder} instance to support call chaining.
   * @throws InvalidPropertyExpressionException
   *           If the provided property chain expression is invalid.
   */
  public DataBinder<T> unbind(String property) {
    for (Binding binding : bindings.get(property)) {
      getAgent().unbind(binding);
    }
    bindings.removeAll(property);

    if (bindings.isEmpty()) {
      // Proxies without bindings will be removed from the cache to make sure
      // the garbage collector can do its job (see
      // BindableProxyFactory#removeCachedProxyForModel). We throw away the
      // reference to the proxy to force a new lookup in case this data binder
      // will be reused.
      unwrapProxy();
    }
    return this;
  }

  /**
   * Unbinds all widgets bound by previous calls to
   * {@link #bind(HasValue, Object, String)} and all
   * {@link PropertyChangeHandler handlers} bound by previous calls to
   * {@link #addPropertyChangeHandler(PropertyChangeHandler)}.
   *
   * @return the same {@link DataBinder} instance to support call chaining.
   */
  public DataBinder<T> unbind() {
    return unbind(true);
  }

  private DataBinder<T> unbind(boolean clearBindings) {
    for (Binding binding : bindings.values()) {
      getAgent().unbind(binding);
    }
    if (clearBindings) {
      bindings.clear();
    }

    // Proxies without bindings will be removed from the cache to make sure the
    // garbage collector can do its job (see
    // BindableProxyFactory#removeCachedProxyForModel). We throw away the
    // reference to the proxy to force a new lookup in case this data binder
    // will be reused.
    unwrapProxy();
    return this;
  }

  /**
   * Returns the model instance associated with this {@link DataBinder}.
   *
   * @return The model instance which has to be used in place of the provided
   *         model (see {@link #forModel(Object)} and {@link #forType(Class)})
   *         if changes should be automatically synchronized with the UI.
   */
  public T getModel() {
    ensureProxied();
    return (paused == null) ? proxy : paused;
  }

  /**
   * Changes the underlying model instance. The existing bindings stay intact
   * but only affect the new model instance. The previously associated model
   * instance will no longer be kept in sync with the UI. The bound UI widgets
   * will be updated based on the new model state. Bindings will be resumed if
   * they are currently paused.
   *
   * @param model
   *          The instance of a {@link Bindable} type, must not be null.
   * @return The model instance which has to be used in place of the provided
   *         model (see {@link #forModel(Object)} and {@link #forType(Class)})
   *         if changes should be automatically synchronized with the UI (also
   *         accessible using {@link #getModel()}).
   */
  public T setModel(T model) {
    return setModel(model, StateSync.FROM_MODEL);
  }

  /**
   * Changes the underlying model instance. The existing bindings stay intact
   * but only affect the new model instance. The previously associated model
   * instance will no longer be kept in sync with the UI. Bindings will be
   * resumed if they are currently paused.
   *
   * @param model
   *          The instance of a {@link Bindable} type, must not be null.
   * @param initialState
   *          Specifies the origin of the initial state of both model and UI
   *          widget. Null if no initial state synchronization should be carried
   *          out.
   * @return The model instance which has to be used in place of the provided
   *         model (see {@link #forModel(Object)} and {@link #forType(Class)})
   *         if changes should be automatically synchronized with the UI (also
   *         accessible using {@link #getModel()}).
   */
  public T setModel(T model, StateSync initialState) {
    return setModel(model, initialState, false);
  }

  /**
   * Changes the underlying model instance. The existing bindings stay intact
   * but only affect the new model instance. The previously associated model
   * instance will no longer be kept in sync with the UI. Bindings will be
   * resumed if they are currently paused.
   *
   * @param model
   *          The instance of a {@link Bindable} type, must not be null.
   * @param initialState
   *          Specifies the origin of the initial state of both model and UI
   *          widget. Null if no initial state synchronization should be carried
   *          out.
   * @param fireChangeEvents
   *          Specifies whether or not {@link PropertyChangeEvent}s should be
   *          fired as a consequence of the model change.
   * @return The model instance which has to be used in place of the provided
   *         model (see {@link #forModel(Object)} and {@link #forType(Class)})
   *         if changes should be automatically synchronized with the UI (also
   *         accessible using {@link #getModel()}).
   */
  @SuppressWarnings("unchecked")
  public T setModel(T model, StateSync initialState, boolean fireChangeEvents) {
    Assert.notNull(model);

    BindableProxy<T> newProxy;
    StateSync newInitState = (initialState != null) ? initialState : getAgent().getInitialState();
    if (model instanceof BindableProxy) {
      newProxy = (BindableProxy<T>) model;
      newProxy.getBindableProxyAgent().setInitialState(newInitState);
    }
    else {
      newProxy = (BindableProxy<T>) BindableProxyFactory.getBindableProxy(model, newInitState);
    }

    newProxy.getBindableProxyAgent().mergePropertyChangeHandlers(propertyChangeHandlerSupport);
    if (fireChangeEvents) {
      newProxy.getBindableProxyAgent().fireChangeEvents(getAgent());
    }

    if (newProxy != this.proxy) {
      // unbind the old proxy
      unbind(false);
    }

    // replay all bindings
    final Multimap<String, Binding> bindings = LinkedHashMultimap.create();
    for (Binding b : this.bindings.values()) {
      // must be checked before unbind() removes the handlers
      boolean bindOnKeyUp = b.needsKeyUpBinding();

      newProxy.getBindableProxyAgent().unbind(b);
      bindings.put(b.getProperty(), newProxy.getBindableProxyAgent()
                                      .bind(b.getWidget(), b.getProperty(), b.getConverter(), bindOnKeyUp));
    }
    this.paused = null;
    this.bindings = bindings;
    this.proxy = (T) newProxy;
    return this.proxy;
  }

  /**
   * Returns the widgets currently bound to the provided model property (see
   * {@link #bind(Widget, String)}).
   *
   * @param property
   *          The name of the property (or a property chain). Must not be null.
   * @return the list of widgets currently bound to the provided property or an
   *         empty list if no widget was bound to the property.
   */
  public List<Widget> getWidgets(String property) {
    Assert.notNull(property);
    List<Widget> widgets = new ArrayList<Widget>();
    for (Binding binding : bindings.get(property)) {
      widgets.add(binding.getWidget());
    }
    return widgets;
  }

  /**
   * Returns a set of the currently bound property names.
   *
   * @return all bound properties, or an empty set if no properties have been
   *         bound.
   */
  public Set<String> getBoundProperties() {
    return bindings.keySet();
  }

  /**
   * Pauses all bindings. The model and UI fields are no longer kept in sync
   * until either {@link #resume(StateSync)} or {@link #setModel(Object)} is
   * called. This method has no effect if the bindings are already paused.
   */
  @SuppressWarnings("unchecked")
  public void pause() {
    if (paused != null) return;
    
    T paused = proxy;
    T clone = (T) ((BindableProxy<?>) proxy).deepUnwrap();
    setModel(clone);
    this.paused = paused;
  }  
  
  /**
   * Resumes the previously paused bindings (see {@link #pause()}) and carries
   * out state synchronization to catch up on changes that happened in the
   * meantime. This method has no effect if {@link #pause()} was never called.
   * 
   * @param resumeState
   *          the state to resume from. Must not be null.
   */
  public void resume(final StateSync resumeState) {
    if (paused == null) return;
    
    Assert.notNull(resumeState);
    setModel(paused, resumeState);
  }
  
  @Override
  public PropertyChangeUnsubscribeHandle addPropertyChangeHandler(PropertyChangeHandler<?> handler) {
    propertyChangeHandlerSupport.addPropertyChangeHandler(handler);
    final PropertyChangeUnsubscribeHandle agentUnsubHandle = getAgent().addPropertyChangeHandler(handler);

    return new OneTimeUnsubscribeHandle() {
      @Override
      public void doUnsubscribe() {
        agentUnsubHandle.unsubscribe();
      }
    };
  }

  @Override
  public <P> PropertyChangeUnsubscribeHandle addPropertyChangeHandler(String property, PropertyChangeHandler<P> handler) {
    propertyChangeHandlerSupport.addPropertyChangeHandler(property, handler);
    final PropertyChangeUnsubscribeHandle agentUnsubHandle = getAgent().addPropertyChangeHandler(property, handler);

    return new OneTimeUnsubscribeHandle() {

      @Override
      public void doUnsubscribe() {
        agentUnsubHandle.unsubscribe();
      }
    };
  }
  
  @SuppressWarnings("unchecked")
  private BindableProxyAgent<T> getAgent() {
    ensureProxied();
    return ((BindableProxy<T>) this.proxy).getBindableProxyAgent();
  }

  @SuppressWarnings("unchecked")
  private void unwrapProxy() {
    if (proxy instanceof BindableProxy<?>) {
      proxy = (T) ((BindableProxy<T>) proxy).unwrap();
    }
  }

  private void ensureProxied() {
    if (!(proxy instanceof BindableProxy<?>)) {
      proxy = BindableProxyFactory.getBindableProxy(Assert.notNull(proxy), null);
    }
  }
}
