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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * Base class used by {@link ApplicationScopedContext} and
 * {@link DependentScopeContext}. Maps created proxies and instances to
 * factories.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public abstract class AbstractContext implements Context {

  private final Map<String, Factory<?>> factories = new HashMap<String, Factory<?>>();
  private final Map<String, Proxy<?>> proxies = new HashMap<String, Proxy<?>>();
  private final Map<Object, Factory<?>> factoriesByCreatedInstances = new IdentityHashMap<Object, Factory<?>>();
  private final Set<String> factoriesCurrentlyCreatingInstances = new HashSet<String>();
  private final ListMultimap<Object, DestructionCallback<?>> destructionCallbacksByInstance = ArrayListMultimap.create();

  private ContextManager contextManager;

  protected void beforeCreateInstance(final String factoryName) {
    factoriesCurrentlyCreatingInstances.add(factoryName);
  }

  protected void afterCreateInstance(final String factoryName) {
    factoriesCurrentlyCreatingInstances.remove(factoryName);
  }

  protected boolean isCurrentlyCreatingInstance(final String factoryName) {
    return factoriesCurrentlyCreatingInstances.contains(factoryName);
  }

  @Override
  public ContextManager getContextManager() {
    if (contextManager == null) {
      throw new RuntimeException("ContextManager has not been set.");
    }

    return contextManager;
  }

  @Override
  public void setContextManager(final ContextManager contextManager) {
    this.contextManager = contextManager;
  }

  @Override
  public <T> void registerFactory(final Factory<T> factory) {
    factories.put(factory.getHandle().getFactoryName(), factory);
  }

  /**
   * @return Returns null if a proxy cannot be created.
   */
  protected <T> Proxy<T> getOrCreateProxy(final String factoryName) {
    @SuppressWarnings("unchecked")
    Proxy<T> proxy = (Proxy<T>) proxies.get(factoryName);
    if (proxy == null && !proxies.containsKey(factoryName)) {
      final Factory<T> factory = getFactory(factoryName);
      proxy = factory.createProxy(this);
      proxies.put(factoryName, proxy);
    }

    return proxy;
  }

  protected <T> Factory<T> getFactory(final String factoryName) {
    @SuppressWarnings("unchecked")
    final Factory<T> factory = (Factory<T>) factories.get(factoryName);
    if (factory == null) {
      throw new RuntimeException("Could not find registered factory " + factoryName);
    }

    return factory;
  }

  @Override
  public Collection<Factory<?>> getAllFactories() {
    return Collections.unmodifiableCollection(factories.values());
  }

  @Override
  public <T> T getNewInstance(final String factoryName) {
    final Factory<T> factory = getFactory(factoryName);
    final Proxy<T> proxy = factory.createProxy(this);
    final T instance = factory.createInstance(getContextManager());
    if (proxy != null) {
      proxy.setInstance(instance);
    }
    factory.invokePostConstructs(instance);
    registerInstance(instance, factory);

    return (proxy != null) ? proxy.asBeanType() : instance;
  }

  protected void registerInstance(Object unwrappedInstance, Factory<?> factory) {
    factoriesByCreatedInstances.put(unwrappedInstance, factory);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public void destroyInstance(final Object instance) {
    // TODO destroy proxy if dependent scope
    final Object unwrapped = maybeUnwrap(instance);
    final Factory<?> factory = factoriesByCreatedInstances.get(unwrapped);
    if (factory != null) {
      for (final DestructionCallback callback : destructionCallbacksByInstance.get(unwrapped)) {
        callback.destroy(unwrapped);
      }
      factory.destroyInstance(unwrapped, contextManager);
      factoriesByCreatedInstances.remove(unwrapped);
      destructionCallbacksByInstance.removeAll(unwrapped);
    }
  }

  @Override
  public boolean addDestructionCallback(Object instance, DestructionCallback<?> callback) {
    final Object unwrapped = maybeUnwrap(instance);
    if (factoriesByCreatedInstances.containsKey(unwrapped)) {
      destructionCallbacksByInstance.put(unwrapped, callback);
      return true;
    } else {
      return false;
    }
  }

  private Object maybeUnwrap(Object instance) {
    return Factory.maybeUnwrapProxy(instance);
  }

  @Override
  public boolean isManaged(final Object ref) {
    return factoriesByCreatedInstances.containsKey(maybeUnwrap(ref));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <P> P getInstanceProperty(final Object instance, final String propertyName, final Class<P> type) {
    final Object unwrapped = maybeUnwrap(instance);
    return ((Factory<Object>) factoriesByCreatedInstances.get(unwrapped)).getReferenceAs(unwrapped, propertyName, type);
  }

}
