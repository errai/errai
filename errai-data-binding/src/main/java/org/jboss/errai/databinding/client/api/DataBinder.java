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
import org.jboss.errai.databinding.client.Convert;

import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

/**
 * This class can be used to programmatically bind properties of a data model instance (any POJO annotated with
 * {@link Bindable}) to UI fields/widgets. The properties of the model and the UI components will automatically be kept
 * in sync for as long as they are bound.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class DataBinder<T> {
  private T model;

  /**
   * Creates a {@link DataBinder} for a newly created model instance of the provided type.
   * 
   * @param modelType
   *          The bindable type, must not be null.
   */
  public DataBinder(Class<T> modelType) {
    this.model = BindableProxyFactory.getBindableProxy(Assert.notNull(modelType), null);
  }

  /**
   * Creates a {@link DataBinder} for the provided model instance.
   * 
   * @param model
   *          The instance of a {@link Bindable} type, must not be null.
   */
  public DataBinder(T model) {
    this(Assert.notNull(model), null);
  }

  /**
   * Creates a {@link DataBinder} for the provided model instance, initializing either model or UI widgets from the
   * values defined by {@link InitialState}.
   * 
   * @param model
   *          The instance of a {@link Bindable} type, must not be null.
   * @param intialState
   *          Specifies the origin of the initial state of both model and UI widget.
   */
  public DataBinder(T model, InitialState intialState) {
    this.model = BindableProxyFactory.getBindableProxy(Assert.notNull(model), intialState);
  }

  /**
   * Bind the provided widget to the specified property of the model instance associated with this {@link DataBinder}
   * instance. If an existing binding for the specified property exists it will be replaced.
   * 
   * @param <T>
   *          The model type
   * @param widget
   *          The widget the model instance should be bound to, must not be null.
   * @param property
   *          The name of the property that should be used for the binding, following Java bean conventions. Must not be
   *          null.
   * @return The model instance which has to be used in place of the provided model (see {@link #DataBinder(Object)}) if
   *         changes should be automatically synchronized with the UI (also accessible using {@link #getModel()}).
   */
  public T bind(final Widget widget, final String property) {
    return bind(widget, property, null);
  }

  /**
   * Bind the provided widget to the specified property of the model instance associated with this {@link DataBinder}
   * instance. If an existing binding for the specified property exists it will be replaced.
   * 
   * @param <T>
   *          The model type
   * @param widget
   *          The widget the model instance should be bound to, must not be null.
   * @param property
   *          The name of the property that should be used for the binding, following Java bean conventions. Must not be
   *          null.
   * @param converter
   *          The converter to use for the binding, null if default conversion should be used (see {@link Convert}).
   * @return The model instance which has to be used in place of the provided model (see {@link #DataBinder(Object)}) if
   *         changes should be automatically synchronized with the UI (also accessible using {@link #getModel()}).
   */
  @SuppressWarnings("unchecked")
  public T bind(final Widget widget, final String property, @SuppressWarnings("rawtypes") final Converter converter) {
    Assert.notNull(widget);
    Assert.notNull(property);
    ((BindableProxy<T>) this.model).bind(widget, property, converter);
    return this.model;
  }

  /**
   * Unbinds the widget from the specified model property, bound by a previous call to
   * {@link DataBinder#bind(HasValue, Object, String)}.
   * 
   * @param property
   *          The name of the property to unbind, must not be null.
   * 
   * @return the model instance with the property unbound.
   */
  @SuppressWarnings("unchecked")
  public T unbind(String property) {
    ((BindableProxy<T>) this.model).unbind(property);
    return this.model;
  }

  /**
   * Unbinds the widget and model bound by previous calls to {@link DataBinder#bind(HasValue, Object, String)}.
   * 
   * @return the model instance without any bound property.
   */
  @SuppressWarnings("unchecked")
  public T unbind() {
    ((BindableProxy<T>) this.model).unbind();
    return this.model;
  }

  /**
   * Returns the proxied model instance.
   * 
   * @return the bound model instance
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
   * @return The model instance which has to be used in place of the provided model (see {@link #DataBinder(Object)}) if
   *         changes should be automatically synchronized with the UI (also accessible using {@link #getModel()}).
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
   *          Specifies the origin of the initial state of both model and UI widget, null if no initial state
   *          synchronization should be carried out.
   * @return The model instance which has to be used in place of the provided model (see {@link #DataBinder(Object)}) if
   *         changes should be automatically synchronized with the UI (also accessible using {@link #getModel()}).
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
}