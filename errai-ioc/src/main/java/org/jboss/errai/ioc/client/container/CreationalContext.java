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
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * @author Mike Brock
 */
public class CreationalContext {
  private Map<Object, InitializationCallback> initializationCallbacks =
          new IdentityHashMap<Object, InitializationCallback>();

  private Map<BeanRef, ProxyResolver> unresolvedProxies = new HashMap<BeanRef, ProxyResolver>();
  private Map<BeanRef, Object> wired = new HashMap<BeanRef, Object>();

  public CreationalContext() {
  }

  public void addInitializationCallback(Object beanInstance, InitializationCallback callback) {
    initializationCallbacks.put(beanInstance, callback);
  }

  public void addBean(Object beanInstance, Class<?> beanType, Annotation[] qualifiers) {
    wired.put(new BeanRef(beanType, qualifiers), beanInstance);
  }
  
  public void addUnresolvedProxy(ProxyResolver proxyResolver, Class<?> beanType, Annotation[] qualifiers) {
    unresolvedProxies.put(new BeanRef(beanType, qualifiers), proxyResolver);
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
    for (Map.Entry<BeanRef, ProxyResolver> entry : unresolvedProxies.entrySet()) {
      entry.getValue().resolve(wired.get(entry.getKey()));
    }
  }
}
