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
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A CreationalContext is used for representing context associated with the creation of a bean and its dependencies.
 * A CreationalContext captures {@link InitializationCallback}s and {@link DestructionCallback}s associated with
 * the graph being constructed.
 * <p>
 * This class is relied upon by the {@link IOCBeanManager} itself and should not generally be used directly.
 *
 * @author Mike Brock
 */
public class CreationalContext {
  private final IOCBeanManager beanManager;
  private Map<Object, InitializationCallback> initializationCallbacks =
          new IdentityHashMap<Object, InitializationCallback>();

  private Map<Object, DestructionCallback> destructionCallbacks =
          new IdentityHashMap<Object, DestructionCallback>();

  private Map<BeanRef, List<ProxyResolver>> unresolvedProxies = new LinkedHashMap<BeanRef, List<ProxyResolver>>();
  private Map<BeanRef, Object> wired = new LinkedHashMap<BeanRef, Object>();

  public CreationalContext(IOCBeanManager beanManager) {
    this.beanManager = beanManager;
  }

  public void addInitializationCallback(Object beanInstance, InitializationCallback callback) {
    initializationCallbacks.put(beanInstance, callback);
  }

  public void addDestructionCallback(Object beanInstance, DestructionCallback callback) {
    destructionCallbacks.put(beanInstance, callback);
    beanManager.addDestructionCallbacks(beanInstance, destructionCallbacks);
  }

  public void addProxyReference(Object proxyRef, Object realRef) {
    beanManager.addProxyReference(proxyRef, realRef);
  }

  public BeanRef getBeanReference(Class<?> beanType, Annotation[] qualifiers) {
    return new BeanRef(beanType, qualifiers);
  }

  public void addBean(BeanRef ref, Object instance) {
    if (!wired.containsKey(ref)) {
      wired.put(ref, instance);
    }
  }

  @SuppressWarnings("unchecked")
  public <T> T getBeanInstance(Class<T> beanType, Annotation[] qualifiers) {
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

  @SuppressWarnings({"unchecked", "UnusedDeclaration"})
  public <T> T getInstanceOrNew(CreationalCallback<T> context, Class<?> beanType, Annotation[] qualifiers) {
    final BeanRef ref = getBeanReference(beanType, qualifiers);

    if (wired.containsKey(ref)) {
      return (T) wired.get(ref);
    }
    else {
      return context.getInstance(this);
    }
  }

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


  public void addUnresolvedProxy(ProxyResolver proxyResolver, Class<?> beanType, Annotation[] qualifiers) {
    final BeanRef ref = getBeanReference(beanType, qualifiers);

    List<ProxyResolver> resolverList = unresolvedProxies.get(ref);
    if (resolverList == null) {
      unresolvedProxies.put(ref, resolverList = new ArrayList<ProxyResolver>());
    }

    resolverList.add(proxyResolver);
  }

  public void finish() {
    resolveAllProxies();
    fireAllInitCallbacks();
  }

  @SuppressWarnings("unchecked")
  private void fireAllInitCallbacks() {
    for (Map.Entry<Object, InitializationCallback> entry : initializationCallbacks.entrySet()) {
      entry.getValue().init(entry.getKey());
    }
  }

  @SuppressWarnings("unchecked")
  private void resolveAllProxies() {
    boolean beansResolved = false;

    Iterator<Map.Entry<BeanRef, List<ProxyResolver>>> unresolvedIterator
            = new LinkedHashMap<BeanRef, List<ProxyResolver>>(unresolvedProxies).entrySet().iterator();

    int initialSize = unresolvedProxies.size();

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
}
