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

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasValue;

/**
 * This class can be used to programmatically bind an instance of a data model (or any POJO) to an instance of a UI
 * field/component.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class DataBinder<T> {
  private Class<T> forType;
  private T model;
  private HandlerRegistration handlerRegistration;

  public DataBinder() {}

  public DataBinder(Class<T> forType) {
    this.forType = forType;
  }

  /**
   * Bind the provided widget to a newly created model instance.
   * 
   * @param <T>
   *          the model type
   * @param widget
   *          widget the model instance should be bound to, must not be null
   * @param property
   *          the property that should be used for the binding, following Java bean conventions, not null.
   * @return the newly created model instance.
   */
  public T bind(final HasValue<?> widget, final String property) {
    Assert.notNull(widget);
    Assert.notNull(property);

    this.model = BindableProxyFactory.getBindableProxy(forType);
    ((BindableProxy) model).bindTo(widget);
    _bind(widget, property);
    
    return this.model;
  }

  /**
   * Bind the provided widget to the provided model instance.
   * 
   * @param <T>
   *          the model type
   * @param widget
   *          widget the model instance should be bound to, must not be null
   * @param model
   *          the model instance, must not be null
   * @param property
   *          the property that should be used for the binding, following Java bean conventions, not null.
   * @return the proxied model which has to be used in place of the model instance provided.
   */
  public T bind(final HasValue<?> widget, final T model, final String property) {
    Assert.notNull(widget);
    Assert.notNull(model);
    Assert.notNull(property);

    this.model = BindableProxyFactory.getBindableProxy(widget, model);
    _bind(widget, property);
    
    return this.model;
  }

  @SuppressWarnings("all")
  private void _bind(final HasValue<?> widget, final String property) {
    handlerRegistration = widget.addValueChangeHandler(new ValueChangeHandler() {
      @Override
      public void onValueChange(ValueChangeEvent event) {
        ((BindableProxy) model).set(property, event.getValue());
      }
    });
  }

  /**
   * Unbinds the widget and model bound by a previous call to {@link DataBinder#bind(HasValue, Object, String)}
   *  
   * @return the unwrapped model
   */
  public T unbind() {
    if (handlerRegistration != null) {
      handlerRegistration.removeHandler();
    }
    
    return unwrap();
  }

  /**
   * Unwraps the proxied model and returns the actual target model instance.
   * 
   * @return target model instance
   */
  public T unwrap() {
    if (model != null) {
      return (T) ((BindableProxy) model).getTarget();
    }
    return null;
  }

  /**
   * Returns the proxied model instance.
   * 
   * @return the bound model instance.
   */
  public T getModel() {
    return (T) model;
  }
}