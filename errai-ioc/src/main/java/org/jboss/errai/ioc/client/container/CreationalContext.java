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

package org.jboss.errai.ioc.client.container;


import org.jboss.errai.ioc.client.BootstrapperInjectionContext;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A CreationalContext is used for representing context associated with the creation of a bean and its dependencies.
 * A CreationalContext captures {@link InitializationCallback}s and {@link DestructionCallback}s associated with
 * the graph being constructed.
 * <p/>
 * This class is relied upon by the {@link IOCBeanManager} itself and should not generally be used directly.
 *
 * @author Mike Brock
 */
public class CreationalContext {
  private final boolean immutableContext;
  private final String scopeName;

  private final IOCBeanManager beanManager;

  private final List<Tuple<Object, InitializationCallback>> initializationCallbacks =
          new ArrayList<Tuple<Object, InitializationCallback>>();

  private final List<Tuple<Object, DestructionCallback>> destructionCallbacks
          = new ArrayList<Tuple<Object, DestructionCallback>>();

  private final Map<BeanRef, List<ProxyResolver>> unresolvedProxies
          = new LinkedHashMap<BeanRef, List<ProxyResolver>>();

  private final Map<BeanRef, Object> wired = new LinkedHashMap<BeanRef, Object>();

  public CreationalContext(final IOCBeanManager beanManager, final String scopeName) {
    this.beanManager = beanManager;
    this.immutableContext = false;
    this.scopeName = scopeName;
  }

  public CreationalContext(boolean immutableContext, final IOCBeanManager beanManager, final String scopeName) {
    this.immutableContext = immutableContext;
    this.beanManager = beanManager;
    this.scopeName = scopeName;
  }

  /**
   * Records a {@link InitializationCallback} to the creational context. All initialization callbacks are executed
   * when the {@link #finish()} method is called.
   *
   * @param beanInstance the instance of the bean associated witht he {@link InitializationCallback}
   * @param callback     the instance of the {@link InitializationCallback}
   */
  public void addInitializationCallback(Object beanInstance, InitializationCallback callback) {
    initializationCallbacks.add(Tuple.of(beanInstance, callback));
  }

  /**
   * Records a {@link DestructionCallback} to the creational context. All destruction callbacks are executed
   * by the bean manager for a creational context when any of the beans within the creational context are
   * destroyed.
   *
   * @param beanInstance the instance of the bean associated with the {@link DestructionCallback}.
   * @param callback     the instance of the {@link DestructionCallback}
   */
  public void addDestructionCallback(Object beanInstance, DestructionCallback callback) {
    destructionCallbacks.add(Tuple.of(beanInstance, callback));
  }

  /**
   * Adds a lookup from a proxy to the actual bean instance that it is proxying. This is called directly by the
   * bootstrapping code.
   *
   * @param proxyRef the reference to the proxy instance
   * @param realRef  the reference to the actual bean instance which the proxy wraps
   */
  @SuppressWarnings("UnusedDeclaration") // used by generated code
  public void addProxyReference(Object proxyRef, Object realRef) {
    beanManager.addProxyReference(proxyRef, realRef);
  }

  /**
   * Returns a {@link BeanRef} which matches the specified type and qualifiers whether or not the bean is within
   * the creational context or not.
   *
   * @param beanType   the type of the bean
   * @param qualifiers the qualifiers for the bean
   * @return a {@link BeanRef} matching the specified type and qualifiers.
   */
  public BeanRef getBeanReference(final Class<?> beanType, final Annotation[] qualifiers) {
    return new BeanRef(beanType, qualifiers);
  }

  /**
   * Adds a bean to the creational context based on the specified bean type and qualifiers with a reference to an
   * actual instantiated instance of the bean.
   *
   * @param beanType   the type of the bean
   * @param qualifiers the qualifiers for the bean
   * @param instance   the instance to the bean
   */
  public void addBean(final Class<?> beanType, final Annotation[] qualifiers, final Object instance) {
    addBean(getBeanReference(beanType, qualifiers), instance);
  }

  /**
   * Adds a bean to the creational context based on the {@link BeanRef} with a reference to the an actual instantiated
   * instance of the bean.
   *
   * @param ref      the {@link BeanRef} representing the bean
   * @param instance the instance of the bean
   */
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
  public Set<BeanRef> getAllCreatedBeans() {
    return Collections.unmodifiableSet(wired.keySet());
  }

  public Collection<Object> getAllCreatedBeanInstances() {
    return Collections.unmodifiableCollection(wired.values());
  }

  /**
   * Obtains an instance of the bean within the creational context based on the specified bean type and qualifiers.
   *
   * @param beanType   the type of the bean
   * @param qualifiers the qualifiers fo the bean
   * @param <T>        the type of the bean
   * @return the actual instance of the bean
   */
  @SuppressWarnings("unchecked")
  public <T> T getBeanInstance(final Class<T> beanType, final Annotation[] qualifiers) {
    final T t = (T) wired.get(getBeanReference(beanType, qualifiers));
    if (t == null) {
      // see if the instance is available in the bean manager
      IOCBeanDef<T> beanDef = IOC.getBeanManager().lookupBean(beanType, qualifiers);

      if (beanDef != null && beanDef instanceof IOCSingletonBean) {
        return beanDef.getInstance();
      }
    }
    return t;
  }

  /**
   * Returns the instance of the specified bean of matching type and qualifiers, or if there is no matching bean within
   * the context, the specified {@link CreationalCallback} is called to instantiate and add the bean to the context.
   *
   * @param callback   the {@link CreationalCallback} to be called in order to instantiate the bean if it is not already
   *                   available withint he current creational context.
   * @param beanType   the type of the bean
   * @param qualifiers the qualifiers for the bean
   * @param <T>        the type of the bean
   * @return the instance of the bean
   * @see #getSingletonInstanceOrNew(org.jboss.errai.ioc.client.BootstrapperInjectionContext, CreationalCallback, Class, java.lang.annotation.Annotation[])
   */
  @SuppressWarnings({"unchecked", "UnusedDeclaration"})
  public <T> T getInstanceOrNew(final CreationalCallback<T> callback, final Class<?> beanType, final Annotation[] qualifiers) {
    final BeanRef ref = getBeanReference(beanType, qualifiers);

    if (wired.containsKey(ref)) {
      return (T) wired.get(ref);
    }
    else {
      return callback.getInstance(this);
    }
  }

  /**
   * Returns the instance of the specified bean of matching type and qualifiers, or if there is no matching bean within
   * the context, an instance of the bean will be obtained from the {@link BootstrapperInjectionContext}. This method
   * assumes that the caller <em>knows</em> that the bean is a singleton bean. It is called directly by the
   * IOC bootstrapping code.
   * <p/>
   * In the event that bean is not available within the {@link BootstrapperInjectionContext}, the specified
   * {@link CreationalCallback} is invoked at the bean is added to the {@link BootstrapperInjectionContext}. This
   * functionality primarily enables proper behavior for singleton producers.
   *
   * @param injectionContext the {@link BootstrapperInjectionContext} of the container
   * @param callback         the {@link CreationalCallback} to be called in order to instantiate the bean if it is not already
   *                         available withint he current creational context.   * @param beanType
   * @param qualifiers       the qualifiers for the bean
   * @param <T>              the type of the bean
   * @return the instance of the bean
   * @see #getInstanceOrNew(CreationalCallback, Class, java.lang.annotation.Annotation[])
   */
  public <T> T getSingletonInstanceOrNew(BootstrapperInjectionContext injectionContext,
                                         CreationalCallback<T> callback, Class<?> beanType, Annotation[] qualifiers) {

    @SuppressWarnings("unchecked") T inst = (T) getBeanInstance(beanType, qualifiers);

    if (inst != null) {
      return inst;
    }
    else {
      inst = callback.getInstance(this);
      injectionContext.addBean(beanType, callback, inst, qualifiers);
      return inst;
    }
  }

  /**
   * Adds an unresolved proxy into the creational context. This is called to indicate a proxy was required while
   * building a bean, due to a forward reference in a cycle situation. The caller is responsible, through the providing
   * of the {@link ProxyResolver} callback, for implementing its own proxy closing strategy.
   * <p/>
   * After a creational context has added all beans to the context, calling {@link #finish()} will result in all of
   * the provided {@link ProxyResolver}s being executed.
   * <p/>
   * This method is typically called directly by the generated bootstapper.
   *
   * @param proxyResolver the {@link ProxyResolver} used for handling closure of the cycle.
   * @param beanType      the type of the bean
   * @param qualifiers    the qualifiers for the bean
   */
  @SuppressWarnings("UnusedDeclaration") // used by generated code
  public void addUnresolvedProxy(final ProxyResolver proxyResolver, final Class<?> beanType,
                                 final Annotation[] qualifiers) {

    final BeanRef ref = getBeanReference(beanType, qualifiers);

    List<ProxyResolver> resolverList = unresolvedProxies.get(ref);
    if (resolverList == null) {
      unresolvedProxies.put(ref, resolverList = new ArrayList<ProxyResolver>());
    }

    resolverList.add(proxyResolver);
  }

  /**
   * Called to indicate all beans have been added to the context. Calling this method results in all post-initialization
   * tasks (such as @PostConstruct) and proxy closures to occur.
   */
  public void finish() {
    resolveAllProxies();
    fireAllInitCallbacks();
    registerAllBeans();
  }

  @SuppressWarnings("unchecked")
  private void fireAllInitCallbacks() {
    for (Tuple<Object, InitializationCallback> entry : initializationCallbacks) {
      entry.getValue().init(entry.getKey());
    }
  }

  @SuppressWarnings("unchecked")
  private void resolveAllProxies() {
    boolean beansResolved = false;

    final Iterator<Map.Entry<BeanRef, List<ProxyResolver>>> unresolvedIterator
            = new LinkedHashMap<BeanRef, List<ProxyResolver>>(unresolvedProxies).entrySet().iterator();

    final int initialSize = unresolvedProxies.size();

    while (unresolvedIterator.hasNext()) {
      Map.Entry<BeanRef, List<ProxyResolver>> entry = unresolvedIterator.next();
      if (wired.containsKey(entry.getKey())) {
        for (ProxyResolver pr : entry.getValue()) {
          pr.resolve(wired.get(entry.getKey()));
        }

        unresolvedIterator.remove();
      }
      else {
        IOCBeanDef<?> iocBeanDef = IOC.getBeanManager().lookupBean(entry.getKey().getClazz(), entry.getKey().getAnnotations());

        if (iocBeanDef != null) {
          Object beanInstance = iocBeanDef.getInstance(this);

          if (beanInstance != null) {
            if (!wired.containsKey(entry.getKey())) {
              addBean(getBeanReference(entry.getKey().getClazz(), entry.getKey().getAnnotations()), beanInstance);
            }

            beansResolved = true;
          }
        }
      }
    }

    if (beansResolved) {
      resolveAllProxies();
    }
    else if (!unresolvedProxies.isEmpty() && initialSize != unresolvedProxies.size()) {
      throw new RuntimeException("unresolved proxy: " + unresolvedProxies.entrySet().iterator().next().getKey());
    }
  }

  private void registerAllBeans() {
    for (Object ref : getAllCreatedBeanInstances()) {
      beanManager.addBeantoContext(ref, this);
    }
  }

  void destroyContext() {
    if (immutableContext) {
      throw new IllegalStateException("scope [" + scopeName + "] is an immutable scope and cannot be destroyed");
    }

    for (Tuple<Object, DestructionCallback> tuple : destructionCallbacks) {
      tuple.getValue().destroy(tuple.getKey());
    }
  }
}
