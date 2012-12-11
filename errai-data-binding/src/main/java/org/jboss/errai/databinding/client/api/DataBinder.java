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

package org.jboss.errai.databinding.client.api;

import java.util.Set;

import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.databinding.client.BindableProxy;
import org.jboss.errai.databinding.client.BindableProxyAgent;
import org.jboss.errai.databinding.client.BindableProxyFactory;
import org.jboss.errai.databinding.client.HasPropertyChangeHandlers;
import org.jboss.errai.databinding.client.NonExistingPropertyException;

import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides an API to programmatically bind properties of a data model instance (any POJO annotated with
 * {@link Bindable}) to UI fields/widgets. The properties of the model and the UI components will automatically be kept
 * in sync for as long as they are bound.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class DataBinder<T> implements HasPropertyChangeHandlers {

  private T model;

  /**
   * Creates a {@link DataBinder} for a new model instance of the provided type (see {@link #forType(Class)}).
   *
   * @param modelType
   *          The bindable type, must not be null.
   */
  private DataBinder(Class<T> modelType) {
    this.model = BindableProxyFactory.getBindableProxy(Assert.notNull(modelType), null);
  }

  /**
   * Creates a {@link DataBinder} for a new model instance of the provided type (see {@link #forType(Class)}),
   * initializing either model or UI widgets from the values defined by {@link InitialState} (see
   * {@link #forModel(Object, InitialState)}).
   *
   * @param modelType
   *          The bindable type, must not be null.
   * @param initialState
   *          Specifies the origin of the initial state of both model and UI widget. Null if no initial state
   *          synchronization should be carried out.
   */
  private DataBinder(Class<T> modelType, InitialState initialState) {
    this.model = BindableProxyFactory.getBindableProxy(Assert.notNull(modelType), initialState);
  }

  /**
   * Creates a {@link DataBinder} for the provided model instance (see {@link #forModel(Object)}).
   *
   * @param model
   *          The instance of a {@link Bindable} type, must not be null.
   */
  private DataBinder(T model) {
    this(Assert.notNull(model), null);
  }

  /**
   * Creates a {@link DataBinder} for the provided model instance, initializing either model or UI widgets from the
   * values defined by {@link InitialState} (see {@link #forModel(Object, InitialState)}).
   *
   * @param model
   *          The instance of a {@link Bindable} type, must not be null.
   * @param initialState
   *          Specifies the origin of the initial state of both model and UI widget. Null if no initial state
   *          synchronization should be carried out.
   */
  private DataBinder(T model, InitialState initialState) {
    this.model = BindableProxyFactory.getBindableProxy(Assert.notNull(model), initialState);
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
   * Creates a {@link DataBinder} for a new model instance of the provided type, initializing either model or
   * UI widgets from the values defined by {@link InitialState}.
   *
   * @param modelType
   *          The bindable type, must not be null.
   * @param intialState
   *          Specifies the origin of the initial state of both model and UI widget. Null if no initial state
   *          synchronization should be carried out.
   */
  public static <T> DataBinder<T> forType(Class<T> modelType, InitialState initialState) {
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
   * Creates a {@link DataBinder} for the provided model instance, initializing either model or UI widgets from the
   * values defined by {@link InitialState}.
   *
   * @param model
   *          The instance of a {@link Bindable} type, must not be null.
   * @param intialState
   *          Specifies the origin of the initial state of both model and UI widget. Null if no initial state
   *          synchronization should be carried out.
   */
  public static <T> DataBinder<T> forModel(T model, InitialState initialState) {
    return new DataBinder<T>(maybeUnwrapModel(model), initialState);
  }

  /**
   * Binds the provided widget to the specified property of the model instance associated with this {@link DataBinder}.
   * If an existing binding for the specified property exists it will be replaced. If the provided widget already
   * participates in another binding managed by this {@link DataBinder}, a {@link RuntimeException} will be thrown.
   *
   * @param widget
   *          The widget the model instance should be bound to, must not be null.
   * @param property
   *          The name of the model property that should be used for the binding, following Java bean conventions.
   *          Chained (nested) properties are supported and must be dot (.) delimited (e.g. customer.address.street).
   *          Must not be null.
   * @return the same {@link DataBinder} instance to support call chaining.
   * @throws NonExistingPropertyException
   *           If the {@code widget} does not have a property with the given name.
   */
  public DataBinder<T> bind(final Widget widget, final String property) {
    bind(widget, property, null);
    return this;
  }

  /**
   * Binds the provided widget to the specified property of the model instance associated with this {@link DataBinder}.
   * If an existing binding for the specified property exists it will be replaced. If the provided widget already
   * participates in another binding managed by this {@link DataBinder}, a {@link RuntimeException} will be thrown.
   *
   * @param widget
   *          The widget the model instance should be bound to, must not be null.
   * @param property
   *          The name of the model property that should be used for the binding, following Java bean conventions.
   *          Chained (nested) properties are supported and must be dot (.) delimited (e.g. customer.address.street).
   *          Must not be null.
   * @param converter
   *          The converter to use for the binding, null if default conversion should be used (see {@link Convert}).
   * @return the same {@link DataBinder} instance to support call chaining.
   * @throws NonExistingPropertyException
   *           If the {@code widget} does not have a property with the given name.
   */
  public DataBinder<T> bind(final Widget widget, final String property,
      @SuppressWarnings("rawtypes") final Converter converter) {

    Assert.notNull(widget);
    Assert.notNull(property);
    getAgent().bind(widget, property, converter);
    return this;
  }

  /**
   * Unbinds the widget from the specified model property, bound by a previous call to
   * {@link #bind(HasValue, Object, String)}. This method has no effect if the specified property was never bound.
   *
   * @param property
   *          The name of the property (or a property chain) to unbind, must not be null.
   *
   * @return the same {@link DataBinder} instance to support call chaining.
   */
  public DataBinder<T> unbind(String property) {
    getAgent().unbind(property);
    return this;
  }

  /**
   * Unbinds all widgets bound by previous calls to {@link #bind(HasValue, Object, String)}.
   *
   * @return the same {@link DataBinder} instance to support call chaining.
   */
  public DataBinder<T> unbind() {
    getAgent().unbind();
    return this;
  }

  /**
   * Returns the model instance associated with this {@link DataBinder}.
   *
   * @return The model instance which has to be used in place of the provided model (see {@link #forModel(Object)} and
   *         {@link #forType(Class)}) if changes should be automatically synchronized with the UI.
   */
  public T getModel() {
    return this.model;
  }

  /**
   * Changes the underlying model instance. The existing bindings stay intact but only affect the new model instance.
   * The previously associated model instance will no longer be kept in sync with the UI.
   *
   * @param model
   *          The instance of a {@link Bindable} type, must not be null.
   * @return The model instance which has to be used in place of the provided model (see {@link #forModel(Object)} and
   *         {@link #forType(Class)}) if changes should be automatically synchronized with the UI (also accessible using
   *         {@link #getModel()}).
   */
  public T setModel(T model) {
    return setModel(model, null);
  }

  /**
   * Changes the underlying model instance. The existing bindings stay intact but only affect the new model instance.
   * The previously associated model instance will no longer be kept in sync with the UI.
   *
   * @param model
   *          The instance of a {@link Bindable} type, must not be null.
   * @param initialState
   *          Specifies the origin of the initial state of both model and UI widget. Null if no initial state
   *          synchronization should be carried out.
   * @return The model instance which has to be used in place of the provided model (see {@link #forModel(Object)} and
   *         {@link #forType(Class)}) if changes should be automatically synchronized with the UI (also accessible using
   *         {@link #getModel()}).
   */
  @SuppressWarnings("unchecked")
  public T setModel(T model, InitialState initialState) {
    Assert.notNull(model);

    // ensure that we do not proxy the model twice
    model = maybeUnwrapModel(model);

    // create a new proxy and copy the bindings
    BindableProxy<T> newProxy = (BindableProxy<T>) BindableProxyFactory.getBindableProxy(
        model, (initialState != null) ? initialState : getAgent().getInitialState());

    newProxy.getProxyAgent().copyStateFrom(getAgent());

    // unbind the old proxied model
    unbind();

    this.model = (T) newProxy;
    return this.model;
  }

  /**
   * Returns the widget currently bound to the provided model property (see {@link #bind(Widget, String)}).
   *
   * @param property
   *          The name of the model property, must not be null.
   * @return The widget currently bound to the provided property or null if no widget was bound.
   */
  public Widget getWidget(String property) {
    return getAgent().getWidget(Assert.notNull(property));
  }

  /**
   * Returns a set of the currently bound property names.
   *
   * @return bound properties, an empty set if no properties have been bound.
   */
  public Set<String> getBoundProperties() {
    return getAgent().getBoundProperties();
  }

  @Override
  public void addPropertyChangeHandler(PropertyChangeHandler<?> handler) {
    getAgent().addPropertyChangeHandler(handler);
  }

  @Override
  public void removePropertyChangeHandler(PropertyChangeHandler<?> handler) {
    getAgent().removePropertyChangeHandler(handler);
  }

  @Override
  public <P> void addPropertyChangeHandler(String property, PropertyChangeHandler<P> handler) {
    getAgent().addPropertyChangeHandler(property, handler);
  }

  @Override
  public void removePropertyChangeHandler(String property, PropertyChangeHandler<?> handler) {
    getAgent().removePropertyChangeHandler(property, handler);
  }

  @SuppressWarnings("unchecked")
  private static <M> M maybeUnwrapModel(M model) {
    if (model instanceof BindableProxy) {
      model = (M) ((BindableProxy<M>) model).unwrap();
    }
    return model;
  }

  @SuppressWarnings("unchecked")
  private BindableProxyAgent<T> getAgent() {
    return ((BindableProxy<T>) this.model).getProxyAgent();
  }

}