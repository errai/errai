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
import org.jboss.errai.common.client.function.Optional;
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
import org.jboss.errai.databinding.client.ComponentAlreadyBoundException;
import org.jboss.errai.databinding.client.api.handler.property.PropertyChangeEvent;
import org.jboss.errai.databinding.client.api.handler.property.PropertyChangeHandler;

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
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class DataBinder<T> implements HasPropertyChangeHandlers {
  private final PropertyChangeHandlerSupport propertyChangeHandlerSupport = new PropertyChangeHandlerSupport();
  private Multimap<String, Binding> bindings = LinkedHashMultimap.create();

  private T proxy;
  private T paused;

  protected DataBinder() {
    throw new RuntimeException(
            "This constructor exists to allow external access to protected methods through subclassing. There should be no instantiable subclasses of "
                    + getClass().getName());
  }

  /**
   * Creates a {@link DataBinder} for a new model instance of the provided type
   * (see {@link #forType(Class)}).
   *
   * @param modelType
   *          The bindable type, must not be null.
   */
  private DataBinder(Class<T> modelType) {
    this.proxy = BindableProxyFactory.getBindableProxy(Assert.notNull(modelType));
  }

  /**
   * Creates a {@link DataBinder} for the provided model instance.
   *
   * @param model
   *          The instance of a {@link Bindable} type, must not be null.
   */
  private DataBinder(T model) {
    this.proxy = BindableProxyFactory.getBindableProxy(Assert.notNull(model));
  }

  /**
   * Creates a {@link DataBinder} for a new model instance of the provided type.
   *
   * @param modelType
   *          The bindable type, must not be null.
   */
  public static <T> DataBinder<T> forType(Class<T> modelType) {
    return new DataBinder<T>(modelType);
  }

  /**
   * Creates a {@link DataBinder} for a list with models of the provided type.
   *
   * @param modelType
   *          The bindable type, must not be null.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static <T> DataBinder<List<T>> forListOfType(Class<T> modelType) {
    return new DataBinder<List<T>>((Class) List.class);
  }

  /**
   * Creates a {@link DataBinder} for the provided model instance.
   *
   * @param model
   *          The instance of a {@link Bindable} type, must not be null.
   */
  public static <T> DataBinder<T> forModel(T model) {
    return new DataBinder<T>(model);
  }

  /**
   * Binds the provided component to the specified property of the model instance
   * associated with this {@link DataBinder}. If the provided component already
   * participates in another binding managed by this {@link DataBinder}, a
   * {@link ComponentAlreadyBoundException} will be thrown.
   *
   * @param component
   *          The UI component the model instance should be bound to, must not be
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
   * @throws ComponentAlreadyBoundException
   *           If the provided {@code component} is already bound to a property of
   *           the model.
   */
  public DataBinder<T> bind(final Object component, String property) {
    return bind(component, property, null);
  }

  /**
   * Binds the provided component to the specified property of the model instance
   * associated with this {@link DataBinder}. If the provided component already
   * participates in another binding managed by this {@link DataBinder}, a
   * {@link ComponentAlreadyBoundException} will be thrown.
   *
   * @param component
   *          The UI component the model instance should be bound to, must not be
   *          null.
   * @param property
   *          The name of the model property that should be used for the
   *          binding, following Java bean conventions. Chained (nested)
   *          properties are supported and must be dot (.) delimited (e.g.
   *          customer.address.street). Must not be null.
   * @param converter
   *          The converter to use for the binding, null if default conversion
   *          should be used (see {@link Convert#getConverter(Class, Class)} or
   *          {@link Convert#identityConverter(Class)} for possible arguments).
   * @return the same {@link DataBinder} instance to support call chaining.
   * @throws NonExistingPropertyException
   *           If the {@code model} does not have a property with the given
   *           name.
   * @throws InvalidPropertyExpressionException
   *           If the provided property chain expression is invalid.
   * @throws ComponentAlreadyBoundException
   *           If the provided {@code component} is already bound to a property of
   *           the model.
   */
  public DataBinder<T> bind(final Object component, final String property,
          @SuppressWarnings("rawtypes") final Converter converter) {
    bind(component, property, converter, StateSync.FROM_MODEL);
    return this;
  }

  /**
   * Binds the provided component to the specified property of the model instance
   * associated with this {@link DataBinder}. If the provided component already
   * participates in another binding managed by this {@link DataBinder}, a
   * {@link ComponentAlreadyBoundException} will be thrown.
   *
   * @param component
   *          The UI component the model instance should be bound to, must not be
   *          null.
   * @param property
   *          The name of the model property that should be used for the
   *          binding, following Java bean conventions. Chained (nested)
   *          properties are supported and must be dot (.) delimited (e.g.
   *          customer.address.street). Must not be null.
   * @param converter
   *          The converter to use for the binding, null if default conversion
   *          should be used (see {@link Convert#getConverter(Class, Class)} or
   *          {@link Convert#identityConverter(Class)} for possible arguments).
   * @param initialState
   *          Specifies the origin of the initial state of both model and UI
   *          component. Null if no initial state synchronization should be carried
   *          out.
   * @return the same {@link DataBinder} instance to support call chaining.
   * @throws NonExistingPropertyException
   *           If the {@code model} does not have a property with the given
   *           name.
   * @throws InvalidPropertyExpressionException
   *           If the provided property chain expression is invalid.
   * @throws ComponentAlreadyBoundException
   *           If the provided {@code component} is already bound to a property of
   *           the model.
   */
  public DataBinder<T> bind(final Object component, final String property,
          @SuppressWarnings("rawtypes") final Converter converter, final StateSync initialState) {

    return bind(component, property, converter, initialState, false);
  }

  /**
   * Binds the provided component to the specified property of the model instance
   * associated with this {@link DataBinder}. If the provided component already
   * participates in another binding managed by this {@link DataBinder}, a
   * {@link ComponentAlreadyBoundException} will be thrown.
   *
   * @param component
   *          The UI component the model instance should be bound to, must not be
   *          null.
   * @param property
   *          The name of the model property that should be used for the
   *          binding, following Java bean conventions. Chained (nested)
   *          properties are supported and must be dot (.) delimited (e.g.
   *          customer.address.street). Must not be null.
   * @param converter
   *          The converter to use for the binding, null if default conversion
   *          should be used (see {@link Convert#getConverter(Class, Class)} or
   *          {@link Convert#identityConverter(Class)} for possible arguments).
   * @param initialState
   *          Specifies the origin of the initial state of both model and UI
   *          component. Null if no initial state synchronization should be carried
   *          out.
   * @param bindOnKeyUp
   *          A boolean value that allows models bound to text-based components to
   *          be updated on a {@link com.google.gwt.event.dom.client.KeyUpEvent}
   *          as well as the default
   *          {@link com.google.gwt.event.logical.shared.ValueChangeEvent}
   * @return the same {@link DataBinder} instance to support call chaining.
   * @throws NonExistingPropertyException
   *           If the {@code model} does not have a property with the given
   *           name.
   * @throws InvalidPropertyExpressionException
   *           If the provided property chain expression is invalid.
   * @throws ComponentAlreadyBoundException
   *           If the provided {@code component} is already bound to a property of
   *           the model.
   */
  public DataBinder<T> bind(final Object component, final String property,
          @SuppressWarnings("rawtypes") final Converter converter, final StateSync initialState, final boolean bindOnKeyUp) {

    Assert.notNull(component);
    Assert.notNull(property);

    if (!(proxy instanceof BindableProxy<?>)) {
      proxy = BindableProxyFactory.getBindableProxy(Assert.notNull(proxy));
    }

    Binding binding = getAgent().bind(component, property, converter, bindOnKeyUp, initialState);
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
    clearModelHandlers();

    // Proxies without bindings will be removed from the cache to make sure the
    // garbage collector can do its job (see
    // BindableProxyFactory#removeCachedProxyForModel). We throw away the
    // reference to the proxy to force a new lookup in case this data binder
    // will be reused.
    unwrapProxy();
    return this;
  }

  private void clearModelHandlers() {
    getAgent().clearModelHandlers();
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
   *          widget.
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
   *          widget.
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

    final BindableProxy<T> newProxy;
    final StateSync newInitState = Optional.ofNullable(initialState).orElse(StateSync.FROM_MODEL);
    if (model instanceof BindableProxy) {
      newProxy = (BindableProxy<T>) model;
    }
    else {
      newProxy = (BindableProxy<T>) BindableProxyFactory.getBindableProxy(model);
    }

    newProxy.getBindableProxyAgent().mergePropertyChangeHandlers(propertyChangeHandlerSupport);
    if (fireChangeEvents) {
      newProxy.getBindableProxyAgent().fireChangeEvents(getAgent(), initialState);
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
                                      .bind(b.getComponent(), b.getProperty(), b.getConverter(), bindOnKeyUp, newInitState));
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
  public List<Object> getComponents(String property) {
    Assert.notNull(property);
    List<Object> widgets = new ArrayList<>();
    for (Binding binding : bindings.get(property)) {
      widgets.add(binding.getComponent());
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

  /**
   * @return true iff {@link #pause()} was called and there have been no calls since to {@link #resume(StateSync)} or
   *         {@link #setModel(Object, StateSync, boolean)}.
   */
  public boolean isPaused() {
    return (paused != null);
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
      proxy = BindableProxyFactory.getBindableProxy(Assert.notNull(proxy));
    }
  }

  /**
   * For adding nested bindings. This method must be public, but should not be called by end users.
   */
  public void addBinding(final String property, final Binding binding) {
    bindings.put(property, binding);
  }
}
