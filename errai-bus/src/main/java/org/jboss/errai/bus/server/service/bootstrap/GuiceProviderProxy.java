/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.server.service.bootstrap;

import com.google.inject.Provider;
import org.jboss.errai.common.client.api.ResourceProvider;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: May 3, 2010
 */
public class GuiceProviderProxy implements Provider {
  private final ResourceProvider provider;

  public GuiceProviderProxy(ResourceProvider provider) {
    this.provider = provider;
  }

  public Object get() {
    return provider.get();
  }
}
