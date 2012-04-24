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

import com.google.gwt.user.client.ui.HasValue;

/**
 * This interface is implemented by the generated proxies for {@link Bindable} types.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public interface BindableProxy {

  /**
   * Sets a property value on the model instance. This method is invoked to change the state of the model in response to
   * UI changes.
   * 
   * @param property
   *          name of the property following Java bean conventions
   * @param value
   *          new value of the property
   */
  public void set(String property, Object value);

  /**
   * Returns the target object (the actual model instance)
   * 
   * @return target object
   */
  public Object getTarget();

  /**
   * Binds the proxy/model to the provided UI field/component.
   * 
   * @param hasValue
   *          the widget instance to bind to
   */
  public void bindTo(HasValue<?> hasValue);
}
