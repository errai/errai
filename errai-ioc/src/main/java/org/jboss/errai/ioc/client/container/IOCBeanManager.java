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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A simple bean manager provided by the Errai IOC framework. The manager provides access to all of the wired beans
 * and their instances. Since the actual wiring code is generated, the bean manager is populated by the generated
 * code at bootstrap time.
 *
 * @author Mike Brock
 */
public class IOCBeanManager {
  private Map<Class<?>, List<IOCBeanDef>> beanMap = new HashMap<Class<?>, List<IOCBeanDef>>();

  private Map<Object, Map<Object, DestructionCallback>> activeManagedBeans
          = new IdentityHashMap<Object, Map<Object, DestructionCallback>>();
  private Map<Object, Object> proxyLookupForManagedBeans = new IdentityHashMap<Object, Object>();


  /**
   * Register a bean with the manager. This is called by the generated code to advertise the bean.
   *
   * @param type       the bean type
   * @param instance   the instance reference
   * @param qualifiers any qualifiers
   */
  private void registerSingletonBean(final Class<Object> type, final CreationalCallback<Object> callback,
                                     final Object instance, final Annotation[] qualifiers) {
    registerBean(IOCSingletonBean.newBean(this, type, qualifiers, callback, instance));
  }

  private void registerDependentBean(final Class<Object> type, final CreationalCallback<Object> callback,
                                     final Annotation[] qualifiers) {
    registerBean(IOCDependentBean.newBean(this, type, qualifiers, callback));
  }

  public void addBean(final Class<Object> type, final CreationalCallback<Object> callback,
                      final Object instance, final Annotation[] qualifiers) {
    if (instance != null) {
      registerSingletonBean(type, callback, instance, qualifiers);
    }
    else {
      registerDependentBean(type, callback, qualifiers);
    }
  }


  /**
   * Destroy a bean and all other beans associated with its creational context in the bean manager.
   *
   * @param ref the instance reference of the bean
   */
  public void destroyBean(Object ref) {
    final Object _target;

    if (proxyLookupForManagedBeans.containsKey(ref)) {
      _target = proxyLookupForManagedBeans.get(ref);
    }
    else {
      _target = ref;
    }

    Map<Object, DestructionCallback> destructionCallbackList = activeManagedBeans.get(_target);
    if (destructionCallbackList != null) {
      for (Map.Entry<Object, DestructionCallback> entry : destructionCallbackList.entrySet()) {
        entry.getValue().destroy(entry.getKey());
      }
    }

    if (ref != _target) {
      activeManagedBeans.get(ref);
      proxyLookupForManagedBeans.get(_target);
    }
    else {
      activeManagedBeans.remove(_target);
    }
  }

  void addProxyReference(Object proxyRef, Object realRef) {
    proxyLookupForManagedBeans.put(proxyRef, realRef);
  }

  void addDestructionCallbacks(Object ref, Map<Object, DestructionCallback> callbacks) {
    activeManagedBeans.put(ref, callbacks);
  }

  /**
   * Register a bean with the manager.
   *
   * @param bean an {@link IOCSingletonBean} reference
   */
  public void registerBean(final IOCBeanDef bean) {
    List<IOCBeanDef> beans = beanMap.get(bean.getType());
    if (beans == null) {
      beanMap.put(bean.getType(), beans = new ArrayList<IOCBeanDef>());
    }

    beans.add(bean);
  }

  /**
   * Looks up all beans of the specified type.
   *
   * @param type The type of the bean
   * @return A list of all the beans that match the specified type. Returns an empty list if there is
   *         no matching type.
   */
  public Collection<IOCBeanDef> lookupBeans(Class<?> type) {
    List<IOCBeanDef> beanList = beanMap.get(type);
    if (beanList == null) {
      return Collections.emptyList();
    }
    else {
      return Collections.unmodifiableList(beanList);
    }
  }

  /**
   * Looks up a bean reference based on type and qualifiers. Returns <tt>null</tt> if there is no type associated
   * with the specified
   *
   * @param type       The type of the bean
   * @param qualifiers qualifiers to match
   * @param <T>        The type of the bean
   * @return An instance of the {@link IOCSingletonBean} for the matching type and qualifiers. Returns null if there is
   *         no matching type. Throws an {@link IOCResolutionException} if there is a matching type but none of the
   *         qualifiers match or if more than one bean  matches.
   */
  public <T> IOCBeanDef<T> lookupBean(Class<T> type, Annotation... qualifiers) {
    List<IOCBeanDef> beanList = beanMap.get(type);
    if (beanList == null) {
      return null;
    }

    if (beanList.size() == 1) {
      return beanList.get(0);
    }

    Set<Annotation> qualSet = new HashSet<Annotation>();
    Collections.addAll(qualSet, qualifiers);

    List<IOCBeanDef> matching = new ArrayList<IOCBeanDef>();

    for (IOCBeanDef iocBean : beanList) {
      if (iocBean.matches(qualSet)) {
        matching.add(iocBean);
      }
    }

    if (matching.isEmpty()) {
      throw new IOCResolutionException("no matching bean instances for: " + type.getName());
    }
    else if (matching.size() > 1) {
      throw new IOCResolutionException("multiple matching bean instances for: " + type.getName());
    }
    else {
      return matching.get(0);
    }
  }

  void destroyAllBeans() {
    beanMap = new HashMap<Class<?>, List<IOCBeanDef>>();
  }
}
