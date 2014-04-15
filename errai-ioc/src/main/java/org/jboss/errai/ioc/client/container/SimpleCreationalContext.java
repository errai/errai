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


import org.jboss.errai.ioc.client.SimpleInjectionContext;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A SimpleCreationalContext is used for representing context associated with the creation of a bean and its dependencies.
 * A SimpleCreationalContext captures {@link InitializationCallback}s and {@link DestructionCallback}s associated with
 * the graph being constructed.
 * <p/>
 * This class is relied upon by the {@link SyncBeanManagerImpl} itself and should not generally be used directly.
 *
 * @author Mike Brock
 */
public class SimpleCreationalContext extends AbstractCreationalContext {

  private final SyncBeanManager beanManager;

  public SimpleCreationalContext(final SyncBeanManagerImpl beanManager, final Class<? extends Annotation> scopeName) {
    super(scopeName);
    this.beanManager = beanManager;
  }

  public SimpleCreationalContext(final boolean immutableContext, final SyncBeanManager beanManager,
                                 final Class<? extends Annotation> scope) {
    super(immutableContext, scope);
    this.beanManager = beanManager;
  }

  /**
   * Adds a lookup from a proxy to the actual bean instance that it is proxying. This is called directly by the
   * bootstrapping code.
   *
   * @param proxyRef
   *     the reference to the proxy instance
   * @param realRef
   *     the reference to the actual bean instance which the proxy wraps
   */
  @Override
  @SuppressWarnings("UnusedDeclaration") // used by generated code
  public void addProxyReference(final Object proxyRef, final Object realRef) {
    beanManager.addProxyReference(proxyRef, realRef);
  }

  /**
   * Returns the instance of the specified bean of matching type and qualifiers, or if there is no matching bean within
   * the context, the specified {@link BeanProvider} is called to instantiate and add the bean to the context.
   *
   * @param callback
   *     the {@link BeanProvider} to be called in order to instantiate the bean if it is not already
   *     available without he current creational context.
   * @param beanType
   *     the type of the bean
   * @param qualifiers
   *     the qualifiers for the bean
   * @param <T>
   *     the type of the bean
   *
   * @return the instance of the bean
   *
   * @see #getSingletonInstanceOrNew(org.jboss.errai.ioc.client.SimpleInjectionContext, BeanProvider, Class, java.lang.annotation.Annotation[])
   */
  @SuppressWarnings({"unchecked", "UnusedDeclaration"})
  public <T> T getInstanceOrNew(final BeanProvider<T> callback,
                                final Class<?> beanType,
                                final Annotation[] qualifiers) {
    final BeanRef ref = getBeanReference(beanType, qualifiers);

    if (wired.containsKey(ref)) {
      return (T) wired.get(ref);
    }
    else {
      return callback.getInstance(this);
    }
  }

  /**
   * Obtains an instance of the bean within the creational context based on the specified bean type and qualifiers.
   *
   * @param beanType
   *     the type of the bean
   * @param qualifiers
   *     the qualifiers fo the bean
   * @param <T>
   *     the type of the bean
   *
   * @return the actual instance of the bean
   */
  @SuppressWarnings("unchecked")
  public <T> T getBeanInstance(final Class<T> beanType, final Annotation[] qualifiers) {
    final T t = (T) wired.get(getBeanReference(beanType, qualifiers));
    if (t == null) {
      // see if the instance is available in the bean manager
      final Collection<IOCBeanDef<T>> beanList
          = IOC.getBeanManager().lookupBeans(beanType, qualifiers);

      if (!beanList.isEmpty()) {
        final IOCBeanDef<T> bean = beanList.iterator().next();
        if (bean != null && bean instanceof IOCSingletonBean) {
          return bean.getInstance();
        }
      }
    }
    return t;
  }

  /**
   * Returns the instance of the specified bean of matching type and qualifiers, or if there is no matching bean within
   * the context, an instance of the bean will be obtained from the {@link org.jboss.errai.ioc.client.SimpleInjectionContext}. This method
   * assumes that the caller <em>knows</em> that the bean is a singleton bean. It is called directly by the
   * IOC bootstrapping code.
   * <p/>
   * In the event that bean is not available within the {@link org.jboss.errai.ioc.client.SimpleInjectionContext}, the specified
   * {@link BeanProvider} is invoked and the bean is added to the {@link org.jboss.errai.ioc.client.SimpleInjectionContext}. This
   * functionality primarily enables proper behavior for singleton producers.
   *
   * @param injectionContext
   *     the {@link org.jboss.errai.ioc.client.SimpleInjectionContext} of the container
   * @param callback
   *     the {@link BeanProvider} to be called in order to instantiate the bean if it is not already
   *     available without he current creational context.   * @param beanType
   * @param qualifiers
   *     the qualifiers for the bean
   * @param <T>
   *     the type of the bean
   *
   * @return the instance of the bean
   *
   * @see #getInstanceOrNew(BeanProvider, Class, java.lang.annotation.Annotation[])
   */
  public <T> T getSingletonInstanceOrNew(final SimpleInjectionContext injectionContext,
                                         final BeanProvider<T> callback,
                                         final Class<?> beanType,
                                         final Annotation[] qualifiers) {

    @SuppressWarnings("unchecked") T inst = (T) getBeanInstance(beanType, qualifiers);

    if (inst != null) {
      return inst;
    }
    else {
      inst = callback.getInstance(this);
      injectionContext.addBean(beanType, beanType, callback, inst, qualifiers);
      return inst;
    }
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

  /**
   * Resolves all proxies which were opened during creation of the beans.
   */
  @SuppressWarnings("unchecked")
  private void resolveAllProxies() {
    boolean beansResolved = false;

    final Iterator<Map.Entry<BeanRef, List<ProxyResolver>>> unresolvedIterator
        = new LinkedHashMap<BeanRef, List<ProxyResolver>>(unresolvedProxies).entrySet().iterator();

    final int initialSize = unresolvedProxies.size();

    while (unresolvedIterator.hasNext()) {
      final Map.Entry<BeanRef, List<ProxyResolver>> entry = unresolvedIterator.next();
      if (wired.containsKey(entry.getKey())) {
        final Object wiredInst = wired.get(entry.getKey());
        for (final ProxyResolver pr : entry.getValue()) {
          pr.resolve(wiredInst);
        }
        
        unresolvedIterator.remove();
      }
      else {
        final IOCBeanDef<?> iocBeanDef =
            IOC.getBeanManager().lookupBean(entry.getKey().getClazz(), entry.getKey().getAnnotations());

        if (iocBeanDef != null) {
          if (!wired.containsKey(entry.getKey())) {
            Object instance = iocBeanDef.getInstance(this);
            addBean(getBeanReference(entry.getKey().getClazz(), entry.getKey().getAnnotations()), instance);
            
            final Iterator<Tuple<Object, InitializationCallback>> initCallbacks = initializationCallbacks.iterator();
            while (initCallbacks.hasNext()) {
              final Tuple<Object, InitializationCallback> tuple = initCallbacks.next();
              if (tuple.getKey() == instance) {
                tuple.getValue().init(tuple.getKey());
                initCallbacks.remove();
              }
            }
          }

          beansResolved = true;
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

  /**
   * Registers all created beans with the bean manager.
   */
  private void registerAllBeans() {
    for (final Object ref : getAllCreatedBeanInstances()) {
      beanManager.addBeanToContext(ref, this);
    }
  }
}
