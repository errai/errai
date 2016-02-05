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

import org.jboss.errai.databinding.client.api.Bindable;
import org.jboss.errai.databinding.client.api.StateSync;

/**
 * Provides instances of {@link BindableProxy}s (proxy objects for types annotated with {@link Bindable}).
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public interface BindableProxyProvider {

  /**
   * Returns a proxy for a newly created model instance, bound to the provided widget.
   * 
   * @param state
   *          Specifies the origin of the initial state of both model and UI widget.
   * @return proxy instance
   */
  public BindableProxy<?> getBindableProxy(StateSync state);

  /**
   * Returns a proxy for the provided model instance, bound to the provided widget.
   * 
   * @param model
   *          The model to proxy.
   * @param state
   *          Specifies the origin of the initial state of both model and UI widget.
   * @return proxy instance
   */
  public BindableProxy<?> getBindableProxy(Object model, StateSync state);
}
