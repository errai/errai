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

import com.google.gwt.user.client.ui.HasValue;

/**
 * Provides instances of {@link BindableProxy}s.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public interface BindableProxyProvider {

  /**
   * Returns a proxy for the provided model bound to the provided widget.
   * 
   * @param hasValue  widget that proxy should be bound to.
   * @param model     model to proxy.
   * @return proxy instance
   */
  public BindableProxy getBindableProxy(HasValue<?> hasValue, Object model);
}
