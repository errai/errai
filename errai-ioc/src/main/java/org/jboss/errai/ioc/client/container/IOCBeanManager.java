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


  private void registerSingletonBean(final Class<Object> type, final CreationalCallback<Object> callback,
                                     final Object instance, final Annotation[] qualifiers) {
    registerBean(IOCSingletonBean.newBean(this, type, qualifiers, callback, instance));
  }

  private void registerDependentBean(final Class<Object> type, final CreationalCallback<Object> callback,
                                     final Annotation[] qualifiers) {
    registerBean(IOCDependentBean.newBean(this, type, qualifiers, callback));
  }

  /**
   * Register a bean with the manager. This is usually called by the generated code to advertise the bean. Adding
   * beans at runtime will make beans available for lookup through the BeanManager, but will not in any way alter
   * the wiring scenario of auto-discovered beans at runtime.
   *
   * @param type       the bean type
   * @param callback   the creational callback used to construct the bean
   * @param instance   the instance reference
   * @param qualifiers any qualifiers
   */
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
  @SuppressWarnings("unchecked")
  public void destroyBean(final Object ref) {
    final Object _target = getActualBeanReference(ref);

    final Map<Object, DestructionCallback> destructionCallbackList = activeManagedBeans.get(_target);
    if (destructionCallbackList != null) {
      for (Map.Entry<Object, DestructionCallback> entry : destructionCallbackList.entrySet()) {
        entry.getValue().destroy(entry.getKey());
      }
    }

    if (ref != _target) {
      proxyLookupForManagedBeans.remove(ref);
    }
    else {
      proxyLookupForManagedBeans.values().remove(_target);
    }

    proxyLookupForManagedBeans.remove(_target);
    activeManagedBeans.remove(_target);
  }

  /**
   * Indicates whether the referenced object is currently a managed bean.
   *
   * @param ref the reference to the bean
   * @return returns true if under management
   */
  public boolean isManaged(Object ref) {
    return activeManagedBeans.containsKey(getActualBeanReference(ref));
  }

  /**
   * Obtains an instance to the <em>actual</em> bean. If the specified reference is a proxy, this method will
   * return an unproxied reference to the object.
   *
   * @param ref the proxied or unproxied reference
   * @return returns the absolute reference to bean if the specified reference is a proxy. If the specified reference
   *         is not a proxy, the same instance passed to the method is returned.
   * @see #isProxyReference(Object)
   */
  public Object getActualBeanReference(Object ref) {
    if (isProxyReference(ref)) {
      return proxyLookupForManagedBeans.get(ref);
    }
    else {
      return ref;
    }
  }

  /**
   * Determines whether the referenced object is itself a proxy to a managed bean.
   *
   * @param ref the reference to check
   * @return returns true if the specified reference is itself a proxy.
   * @see #getActualBeanReference(Object)
   */
  public boolean isProxyReference(Object ref) {
    return proxyLookupForManagedBeans.containsKey(ref);
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
  @SuppressWarnings("unchecked")
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
