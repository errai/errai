package org.jboss.errai.ioc.client.lifecycle.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import javax.inject.Singleton;

import org.jboss.errai.ioc.client.lifecycle.api.LifecycleEvent;
import org.jboss.errai.ioc.client.lifecycle.api.LifecycleListener;
import org.jboss.errai.ioc.client.lifecycle.api.LifecycleListenerGenerator;
import org.jboss.errai.ioc.client.lifecycle.api.LifecycleListenerRegistrar;

@Singleton
public class LifecycleListenerRegistrarImpl implements LifecycleListenerRegistrar {

  private final Map<Object, Collection<? extends LifecycleListener<?>>> activeListenerMap = new IdentityHashMap<Object, Collection<? extends LifecycleListener<?>>>();
  private final Map<Class<?>, Collection<LifecycleListenerGenerator<?>>> generatorMap = new HashMap<Class<?>, Collection<LifecycleListenerGenerator<?>>>();

  @Override
  public <T> Iterable<LifecycleListener<T>> getListeners(final Class<? extends LifecycleEvent<T>> eventType,
          final T instance) {
    final Collection<LifecycleListener<T>> allInstanceListeners = getInstanceListeners(instance);
    final Collection<LifecycleListener<T>> eventListeners = new ArrayList<LifecycleListener<T>>();
    for (final LifecycleListener<T> listener : allInstanceListeners) {
      if (listener.isObserveableEventType(eventType)) {
        eventListeners.add(listener);
      }
    }

    return eventListeners;
  }

  private <T> Collection<LifecycleListener<T>> getInstanceListeners(final T instance) {
    Collection<LifecycleListener<T>> listeners = (Collection<LifecycleListener<T>>) activeListenerMap.get(instance);
    if (listeners == null) {
      listeners = generateNewLifecycleListeners(instance);
      activeListenerMap.put(instance, listeners);
    }

    return listeners;
  }

  protected <T> Collection<LifecycleListener<T>> generateNewLifecycleListeners(final T instance) {
    final Collection<LifecycleListener<T>> newListeners = new ArrayList<LifecycleListener<T>>();
    final Collection<LifecycleListenerGenerator<?>> generators = generatorMap.get(instance.getClass());
    if (generators != null) {
      for (final LifecycleListenerGenerator<?> generator : generators) {
        newListeners.add((LifecycleListener<T>) generator.newInstance());
      }
    }

    return newListeners;
  }

  @Override
  public <T> void registerListener(Class<T> lifecycleType, LifecycleListenerGenerator<T> generator) {
    Collection<LifecycleListenerGenerator<?>> generators = generatorMap.get(lifecycleType);
    if (generators == null) {
      generators = new ArrayList<LifecycleListenerGenerator<?>>();
      generatorMap.put(lifecycleType, generators);
    }
    generators.add(generator);
  }

  @Override
  public <T> boolean unregisterListener(final Class<T> lifecycleType, final LifecycleListenerGenerator<T> generator) {
    final Collection<LifecycleListenerGenerator<?>> listenerGenerators = generatorMap.get(lifecycleType);
    if (listenerGenerators == null)
      return false;
    
    return listenerGenerators.remove(generator);
  }

  @Override
  public <T> boolean endInstanceLifecycle(T instance) {
    return activeListenerMap.remove(instance) != null;
  }

}
