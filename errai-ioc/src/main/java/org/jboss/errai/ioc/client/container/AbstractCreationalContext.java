/*
 * Copyright 2013 JBoss, by Red Hat, Inc
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

package org.jboss.errai.ioc.client.container;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mike Brock
 */
public abstract class AbstractCreationalContext implements CreationalContext {
  protected final boolean immutableContext;
  protected final Class<? extends Annotation> scope;

  protected final List<Tuple<Object, InitializationCallback>> initializationCallbacks =
      new ArrayList<Tuple<Object, InitializationCallback>>();

  protected final List<Tuple<Object, DestructionCallback>> destructionCallbacks
      = new ArrayList<Tuple<Object, DestructionCallback>>();

  protected final Map<BeanRef, List<ProxyResolver>> unresolvedProxies
      = new LinkedHashMap<BeanRef, List<ProxyResolver>>();

  protected final Map<BeanRef, Object> wired = new LinkedHashMap<BeanRef, Object>();
  
  private static final Logger logger = LoggerFactory.getLogger(AbstractCreationalContext.class);

  protected AbstractCreationalContext(final Class<? extends Annotation> scope) {
    this.immutableContext = false;
    this.scope = scope;
  }

  protected AbstractCreationalContext(final boolean immutableContext, final Class<? extends Annotation> scope) {
    this.immutableContext = immutableContext;
    this.scope = scope;
  }

  /**
   * Records a {@link InitializationCallback} to the creational context. All initialization callbacks are executed
   * when the finish() method is called.
   *
   * @param beanInstance
   *     the instance of the bean associated with the {@link InitializationCallback}
   * @param callback
   *     the instance of the {@link InitializationCallback}
   */
  @Override
  public void addInitializationCallback(final Object beanInstance, final InitializationCallback callback) {
    initializationCallbacks.add(Tuple.of(beanInstance, callback));
  }

  /**
   * Records a {@link DestructionCallback} to the creational context. All destruction callbacks are executed
   * by the bean manager for a creational context when any of the beans within the creational context are
   * destroyed.
   *
   * @param beanInstance
   *     the instance of the bean associated with the {@link DestructionCallback}.
   * @param callback
   *     the instance of the {@link DestructionCallback}
   */
  @Override
  public void addDestructionCallback(final Object beanInstance, final DestructionCallback callback) {
    destructionCallbacks.add(Tuple.of(beanInstance, callback));
  }


  /**
   * Returns a {@link BeanRef} which matches the specified type and qualifiers whether or not the bean is within
   * the creational context or not.
   *
   * @param beanType
   *     the type of the bean
   * @param qualifiers
   *     the qualifiers for the bean
   *
   * @return a {@link BeanRef} matching the specified type and qualifiers.
   */
  @Override
  public BeanRef getBeanReference(final Class<?> beanType, final Annotation[] qualifiers) {
    return new BeanRef(beanType, qualifiers);
  }

  /**
   * Adds a bean to the creational context based on the specified bean type and qualifiers with a reference to an
   * actual instantiated instance of the bean.
   *
   * @param beanType
   *     the type of the bean
   * @param qualifiers
   *     the qualifiers for the bean
   * @param instance
   *     the instance to the bean
   */
  @Override
  public void addBean(final Class<?> beanType, final Annotation[] qualifiers, final Object instance) {
    addBean(getBeanReference(beanType, qualifiers), instance);
  }

  /**
   * Adds a bean to the creational context based on the {@link BeanRef} with a reference to the an actual instantiated
   * instance of the bean.
   *
   * @param ref
   *     the {@link BeanRef} representing the bean
   * @param instance
   *     the instance of the bean
   */
  @Override
  public void addBean(final BeanRef ref, final Object instance) {
    if (!wired.containsKey(ref)) {
      wired.put(ref, instance);
    }
  }

  /**
   * Returns a list of all created beans within this creational context.
   *
   * @return An unmodifiable set of all the created beans within this creational context.
   */
  @Override
  public Set<BeanRef> getAllCreatedBeans() {
    return Collections.unmodifiableSet(wired.keySet());
  }

  /**
   * Returns a list of the instances of every created bean within this creational context.
   *
   * @return An unmodifiable collection of every bean instance within the creational context.
   */
  @Override
  public Collection<Object> getAllCreatedBeanInstances() {
    return Collections.unmodifiableCollection(wired.values());
  }

  /**
   * Adds an unresolved proxy into the creational context. This is called to indicate a proxy was required while
   * building a bean, due to a forward reference in a cycle situation. The caller is responsible, through the providing
   * of the {@link ProxyResolver} callback, for implementing its own proxy closing strategy.
   * <p/>
   * After a creational context has added all beans to the context, calling finish() will result in all of
   * the provided {@link ProxyResolver}s being executed.
   * <p/>
   * This method is typically called directly by the generated bootstrapper.
   *
   * @param proxyResolver
   *     the {@link ProxyResolver} used for handling closure of the cycle.
   * @param beanType
   *     the type of the bean
   * @param qualifiers
   *     the qualifiers for the bean
   */
  @Override
  @SuppressWarnings("UnusedDeclaration") // used by generated code
  public void addUnresolvedProxy(final ProxyResolver proxyResolver,
                                 final Class<?> beanType,
                                 final Annotation[] qualifiers) {

    final BeanRef ref = getBeanReference(beanType, qualifiers);

    if (!unresolvedProxies.containsKey(ref)) {
      unresolvedProxies.put(ref, new ArrayList<ProxyResolver>());
    }

    unresolvedProxies.get(ref).add(proxyResolver);
  }

  /**
   * Fires all {@link InitializationCallback}s which were declared during creation of the beans.
   */
  @SuppressWarnings("unchecked")
  protected void fireAllInitCallbacks() {
    for (final Tuple<Object, InitializationCallback> entry : initializationCallbacks) {
      try {
        entry.getValue().init(entry.getKey());
      }
      catch (Throwable t) {
        logger.error("error initializing bean: " + entry.getKey().getClass().getName(), t);
        throw new RuntimeException("error in bean initialization", t);
      }
    }
  }

  /**
   * Return a wired instance within the current creational context, or in the absence of an existing context,
   * invoke the specified provider to create the instance. This is should only called by generated code. A
   * wired instance is an instance which exists within this creational context specifically. For instance, a
   * dependent scoped bean.
   *
   * @param ref
   *      the bean reference
   * @param provider
   *      the provider to create the instance if it does not already exist.
   * @param <T>
   *      the bean type
   * @return
   *      the instance
   */
  public <T> T getWiredOrNew(final BeanRef ref, final Provider<T> provider) {
    final T t = (T) wired.get(ref);
    if (t == null) {
      return provider.get();
    }
    else {
      return t;
    }
  }

  /**
   * Fires all {@link DestructionCallback}s within the context.
   */
  @SuppressWarnings("unchecked")
  public void destroyContext() {
    if (immutableContext) {
      throw new IllegalStateException("scope [" + scope.getName() + "] is an immutable scope and cannot be destroyed");
    }

    for (final Tuple<Object, DestructionCallback> tuple : destructionCallbacks) {
      tuple.getValue().destroy(tuple.getKey());
    }
  }
}
