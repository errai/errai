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

import org.jboss.errai.common.client.api.WrappedPortable;
import org.jboss.errai.databinding.client.api.Bindable;
import org.jboss.errai.databinding.client.api.Converter;
import org.jboss.errai.databinding.client.api.InitialState;

import com.google.gwt.user.client.ui.Widget;

/**
 * This interface is implemented by the generated proxies for {@link Bindable} types.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public interface BindableProxy<T> extends WrappedPortable {

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
  public void bind(Widget widget, String property, @SuppressWarnings("rawtypes") Converter converter);

  /**
   * Unbinds the property with the given name.
   * 
   * @param property
   *          the name of the model property to unbind, must not be null.
   */
  public void unbind(String property);

  /**
   * Unbinds all properties.
   */
  public void unbind();

  /**
   * Changes the target model instance of this proxy. The existing bindings stay intact but only affect the new model
   * instance. The previously associated model instance will no longer be kept in sync with the UI.
   * 
   * @param model
   *          The instance of a {@link Bindable} type, must not be null.
   * @param state
   *          Specifies the origin of the initial state of both model and UI widget, null if no initial state
   *          synchronization should be carried out.
   */
  public void setModel(T model, InitialState initialState);

  /**
   * Updates all widgets bound to the model instance associated with this proxy (see {@link #bind(Widget, String)}).
   * This method is only useful if the model instance has undergone changes that were not caused by calls to setter
   * methods on this proxy (and were therefore not visible to this proxy).
   */
  public void updateWidgets();
}
