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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mike Brock
 */
public class CreationalContext {
  private Map<Object, InitializationCallback> initializationCallbacks =
          new IdentityHashMap<Object, InitializationCallback>();

  private Map<BeanRef, List<ProxyResolver>> unresolvedProxies = new HashMap<BeanRef, List<ProxyResolver>>();
  private Map<BeanRef, Object> wired = new LinkedHashMap<BeanRef, Object>();

  public CreationalContext() {
  }

  public void addInitializationCallback(Object beanInstance, InitializationCallback callback) {
    initializationCallbacks.put(beanInstance, callback);
  }

  public void addBean(Object beanInstance, Class<?> beanType, Annotation[] qualifiers) {
    wired.put(new BeanRef(beanType, qualifiers), beanInstance);
  }

  public void addUnresolvedProxy(ProxyResolver proxyResolver, Class<?> beanType, Annotation[] qualifiers) {
    BeanRef ref = new BeanRef(beanType, qualifiers);
    List<ProxyResolver> resolverList = unresolvedProxies.get(ref);
    if (resolverList == null) {
      unresolvedProxies.put(ref, resolverList = new ArrayList<ProxyResolver>());
    }
    resolverList.add(proxyResolver);
  }

  public Object getUnresolvedProxy(Class<?> beanType, Annotation[] qualifiers) {
    return unresolvedProxies.get(new BeanRef(beanType, qualifiers));
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

    Iterator<Map.Entry<BeanRef, List<ProxyResolver>>> unresolvedIterator = unresolvedProxies.entrySet().iterator();
    while (unresolvedIterator.hasNext()) {
      Map.Entry<BeanRef, List<ProxyResolver>> entry = unresolvedIterator.next();
      if (wired.containsKey(entry.getKey())) {
        for (ProxyResolver pr : entry.getValue()) {
          pr.resolve(wired.get(entry.getKey()));
        }

        unresolvedIterator.remove();
      }
      else {
        Object bean = IOC.getBeanManager().lookupBean(entry.getKey().getClazz(), entry.getKey().getAnnotations())
                .getInstance(this);

        if (bean != null) {
          for (ProxyResolver pr : entry.getValue()) {
            pr.resolve(bean);
          }
          
          unresolvedIterator.remove();
        }
      }
    }

    if (!unresolvedProxies.isEmpty()) {
      for (Map.Entry<BeanRef, List<ProxyResolver>> entry : unresolvedProxies.entrySet()) {
        System.out.println("unresolved proxy: " + entry.getKey());
      }
    }
  }
}
