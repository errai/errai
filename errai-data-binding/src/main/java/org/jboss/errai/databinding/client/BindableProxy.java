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

import org.jboss.errai.databinding.client.api.Bindable;
import org.jboss.errai.databinding.client.api.InitialState;

import com.google.gwt.user.client.ui.Widget;

/**
 * This interface is implemented by the generated proxies for {@link Bindable} types.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public interface BindableProxy<T> {

  /**
   * Binds the property with the given name to the provided widget.
   */
  public void bind(Widget widget, String property);

  /**
   * Unbinds the property with the given name.
   */
  public void unbind(String property);

  /**
   * Unbinds all properties.
   */
  public void unbind();

  /**
   * Changes the target model instance of this proxy. The bindings stay intact.
   * 
   * @param model
   *          the instance of a {@link Bindable} type, must not be null
   * @param state
   *          specifies the origin of the initial state of both model and UI widget, null if no initial state
   *          synchronization should be carried out.
   */
  public void setTarget(T model, InitialState initialState);
}
