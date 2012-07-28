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

import org.jboss.errai.common.client.framework.Assert;
import org.jboss.errai.databinding.client.BindableProxy;
import org.jboss.errai.databinding.client.BindableProxyFactory;

import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides the API to programmatically bind properties of a data model instance (any POJO annotated with
 * {@link Bindable}) to UI fields/widgets. The properties of the model and the UI components will automatically be kept
 * in sync for as long as they are bound.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class DataBinder<T> {
  private T model;

  /**
   * Creates a {@link DataBinder} for a newly created model instance of the provided type (see {@link #forType(Class)}).
   * 
   * @param modelType
   *          The bindable type, must not be null.
   */
  private DataBinder(Class<T> modelType) {
    this.model = BindableProxyFactory.getBindableProxy(Assert.notNull(modelType), null);
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
   * @param intialState
   *          Specifies the origin of the initial state of both model and UI widget. Null if no initial state
   *          synchronization should be carried out.
   */
  private DataBinder(T model, InitialState intialState) {
    this.model = BindableProxyFactory.getBindableProxy(Assert.notNull(model), intialState);
  }

  /**
   * Creates a {@link DataBinder} for a newly created model instance of the provided type.
   * 
   * @param modelType
   *          The bindable type, must not be null.
   */
  public static <T> DataBinder<T> forType(Class<T> modelType) {
    return new DataBinder<T>(modelType);
  }

  /**
   * Creates a {@link DataBinder} for the provided model instance.
   * 
   * @param model
   *          The instance of a {@link Bindable} type, must not be null.
   */
  public static <T> DataBinder<T> forModel(T model) {
    return new DataBinder<T>(model, null);
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
  public static <T> DataBinder<T> forModel(T model, InitialState intialState) {
    return new DataBinder<T>(model, intialState);
  }

  /**
   * Bind the provided widget to the specified property of the model instance associated with this {@link DataBinder}.
   * If an existing binding for the specified property exists it will be replaced. If the provided widget already
   * participates in another binding managed by this {@link DataBinder}, a {@link RuntimeException} will be thrown.
   * 
   * @param <T>
   *          The model type
   * @param widget
   *          The widget the model instance should be bound to, must not be null.
   * @param property
   *          The name of the model property that should be used for the binding, following Java bean conventions. Must
   *          not be null.
   * @return the same {@link DataBinder} instance to support call chaining.
   */
  public DataBinder<T> bind(final Widget widget, final String property) {
    bind(widget, property, null);
    return this;
  }

  /**
   * Bind the provided widget to the specified property of the model instance associated with this {@link DataBinder}.
   * If an existing binding for the specified property exists it will be replaced. If the provided widget already
   * participates in another binding managed by this {@link DataBinder}, a {@link RuntimeException} will be thrown.
   * 
   * @param <T>
   *          The model type
   * @param widget
   *          The widget the model instance should be bound to, must not be null.
   * @param property
   *          The name of the model property that should be used for the binding, following Java bean conventions. Must
   *          not be null.
   * @param converter
   *          The converter to use for the binding, null if default conversion should be used (see {@link Convert}).
   * @return the same {@link DataBinder} instance to support call chaining.
   */
  @SuppressWarnings("unchecked")
  public DataBinder<T> bind(final Widget widget, final String property,
      @SuppressWarnings("rawtypes") final Converter converter) {

    Assert.notNull(widget);
    Assert.notNull(property);
    ((BindableProxy<T>) this.model).bind(widget, property, converter);
    return this;
  }

  /**
   * Unbinds the widget from the specified model property, bound by a previous call to
   * {@link #bind(HasValue, Object, String)}.
   * 
   * @param property
   *          The name of the property to unbind, must not be null.
   * 
   * @return the same {@link DataBinder} instance to support call chaining.
   */
  @SuppressWarnings("unchecked")
  public DataBinder<T> unbind(String property) {
    ((BindableProxy<T>) this.model).unbind(property);
    return this;
  }

  /**
   * Unbinds the widget and model bound by previous calls to {@link #bind(HasValue, Object, String)}.
   * 
   * @return the same {@link DataBinder} instance to support call chaining.
   */
  @SuppressWarnings("unchecked")
  public DataBinder<T> unbind() {
    ((BindableProxy<T>) this.model).unbind();
    return this;
  }

  /**
   * Returns the proxied model instance.
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
    // Ensure that we do not proxy the model twice
    if (model instanceof BindableProxy) {
      model = (T) ((BindableProxy<?>) model).unwrap();
    }

    ((BindableProxy<T>) this.model).setModel(model, initialState);
    return this.model;
  }
  /**
   * @param property the name of the model property.
   * @return the value of the model property for the current target.
   * @throws  NonExistingPropertyException
   */
  @SuppressWarnings("unchecked")
  public Object get(String property){
	return  ((BindableProxy<T>) this.model).get(property);
  }

  /**
   * Sets the property on the model to the provided value.
   * @param property the name of the model property.
   * @param value the value to set.
   */
  @SuppressWarnings("unchecked")
  public void set(String property, Object value){
	((BindableProxy<T>) this.model).set(property,value);
  }

  /**
   * Get the Widget currently bound to the provided property.
   * @param property the name of the model property;
   * @return the Widget currently bound to the provided property.
   * 
   */
  @SuppressWarnings("unchecked")
  public Widget getWidget(String property){
	return ((BindableProxy<T>) this.model).getWidget(property);
  }
  


}