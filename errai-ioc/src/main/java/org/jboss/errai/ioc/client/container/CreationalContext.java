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


import com.google.gwt.dev.util.collect.IdentityHashSet;

import javax.enterprise.inject.spi.Bean;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Mike Brock
 */
public class CreationalContext {
  private final IOCBeanManager beanManager;
  private Map<Object, InitializationCallback> initializationCallbacks =
          new IdentityHashMap<Object, InitializationCallback>();

  private Map<Object, DestructionCallback> destructionCallbacks =
          new IdentityHashMap<Object, DestructionCallback>();

  private Map<BeanRef, List<ProxyResolver>> unresolvedProxies = new HashMap<BeanRef, List<ProxyResolver>>();
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

  public <T> T getInstanceOrNew(CreationalCallback<T> context, Class<?> beanType, Annotation[] qualifiers) {
    BeanRef ref = getBeanReference(beanType, qualifiers);

    if (wired.containsKey(ref)) {
      return (T) wired.get(ref);
    }
    else {
      return context.getInstance(this);
    }
  }

  public void addUnresolvedProxy(ProxyResolver proxyResolver, Class<?> beanType, Annotation[] qualifiers) {
    BeanRef ref = getBeanReference(beanType, qualifiers);
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

  private void fireAllInitCallbacks() {
    for (Map.Entry<Object, InitializationCallback> entry : initializationCallbacks.entrySet()) {
      entry.getValue().init(entry.getKey());
    }
  }

  private void resolveAllProxies() {
    boolean beansResolved = false;

    Iterator<Map.Entry<BeanRef, List<ProxyResolver>>> unresolvedIterator
            = new HashMap<BeanRef, List<ProxyResolver>>(unresolvedProxies).entrySet().iterator();

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
        Object beanInstance = IOC.getBeanManager().lookupBean(entry.getKey().getClazz(), entry.getKey().getAnnotations())
                .getInstance(this);

        if (beanInstance != null) {
          if (!wired.containsKey(entry.getKey())) {
            addBean(getBeanReference(entry.getKey().getClazz(), entry.getKey().getAnnotations()), beanInstance);
          }

          beansResolved = true;
        }
      }
    }


    if (beansResolved) {
      resolveAllProxies();
    }
    else if (!unresolvedProxies.isEmpty() && initialSize != unresolvedProxies.size()) {
      for (Map.Entry<BeanRef, List<ProxyResolver>> entry : unresolvedProxies.entrySet()) {
        throw new RuntimeException("unresolved proxy: " + entry.getKey());
      }
    }
  }
}
