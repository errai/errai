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

import java.util.HashMap;
import java.util.Map;

import org.jboss.errai.common.client.framework.Assert;
import org.jboss.errai.ioc.client.container.IOC;

import com.google.gwt.user.client.ui.HasValue;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class BindableProxyFactory {
  private static Map<Class<?>, BindableProxyProvider> bindableProxyProviders =
      new HashMap<Class<?>, BindableProxyProvider>();

  public static <T> T getBindableProxy(HasValue<?> hasValue, T model) {
    if (bindableProxyProviders.isEmpty()) {
      throw new RuntimeException("There are no proxy providers for bindable types registered yet.");
    }

    BindableProxyProvider proxyProvider;
    Class<?> type = model.getClass();
    do  {
      proxyProvider = bindableProxyProviders.get(type);
      // try the class's supertype (it could have been proxied by IOC)
    } while (proxyProvider == null && (type = type.getSuperclass()) != null);

    if (proxyProvider == null) {
      throw new RuntimeException("No proxy provider found for bindable type:" + model.getClass().getName());
    }
    
    Object proxy = proxyProvider.getBindableProxy(hasValue, model);
    if (proxy == null) {
      throw new RuntimeException("No proxy instance provided for bindable type: " + model.getClass().getName());
    }

    return (T) proxy;
  }

  public static void addBindableProxy(Class<?> proxyType, BindableProxyProvider proxyProvider) {
    Assert.notNull(proxyType);
    Assert.notNull(proxyProvider);

    bindableProxyProviders.put(proxyType, proxyProvider);
  }
}