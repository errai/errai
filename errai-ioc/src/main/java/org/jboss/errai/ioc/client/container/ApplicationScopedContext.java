/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.ioc.client.container;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Singleton;

import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ioc.client.api.ScopeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of {@link Context} for singleton beans. All calls to
 * {@link #getInstance(String)} will return the same bean instance.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@ScopeContext({ApplicationScoped.class, Singleton.class, EntryPoint.class})
public class ApplicationScopedContext extends AbstractContext {

  private static final Logger logger = LoggerFactory.getLogger(ApplicationScopedContext.class);
  private static final Set<Class<? extends Annotation>> handledScopes = new HashSet<Class<? extends Annotation>>();

  static {
    handledScopes.add(ApplicationScoped.class);
    handledScopes.add(Singleton.class);
    handledScopes.add(EntryPoint.class);
  }

  private final Map<String, Object> instances = new HashMap<String, Object>();

  @Override
  public <T> T getInstance(final String factoryName) {
    final Proxy<T> proxy = getOrCreateProxy(factoryName);
    if (proxy == null) {
      return getActiveNonProxiedInstance(factoryName);
    } else {
      return proxy.asBeanType();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getActiveNonProxiedInstance(final String factoryName) {
    if (instances.containsKey(factoryName)) {
      return (T) instances.get(factoryName);
    } else if (isCurrentlyCreatingInstance(factoryName)) {
      final Factory<T> factory = this.<T>getFactory(factoryName);
      final T incomplete = factory.getIncompleteInstance();
      if (incomplete == null) {
        throw new RuntimeException("Could not obtain an incomplete instance of " + factory.getHandle().getActualType().getName() + " to break a circular injection.");
      } else {
        logger.warn("An incomplete " + factory.getHandle().getActualType() + " was required to break a circular injection.");
        return incomplete;
      }
    } else {
      return createNewUnproxiedInstance(factoryName);
    }
  }

  private <T> T createNewUnproxiedInstance(final String factoryName) {
    final Factory<T> factory = this.<T>getFactory(factoryName);
    beforeCreateInstance(factoryName);
    final T instance = factory.createInstance(getContextManager());
    afterCreateInstance(factoryName);
    instances.put(factoryName, instance);
    registerInstance(instance, factory);
    factory.invokePostConstructs(instance);
    return instance;
  }

  @Override
  public Class<? extends Annotation> getScope() {
    return ApplicationScoped.class;
  }

  @Override
  public boolean isActive() {
    return true;
  }

  @Override
  public boolean handlesScope(final Class<? extends Annotation> scope) {
    return handledScopes.contains(scope);
  }

}
