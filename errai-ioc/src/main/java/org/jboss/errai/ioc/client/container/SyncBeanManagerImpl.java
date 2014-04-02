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
import java.util.*;

import org.jboss.errai.ioc.client.QualifierUtil;
import org.jboss.errai.ioc.client.SimpleInjectionContext;
import org.jboss.errai.ioc.client.api.EnabledByProperty;

/**
 * A simple bean manager provided by the Errai IOC framework. The manager provides access to all of the wired beans
 * and their instances. Since the actual wiring code is generated, the bean manager is populated by the generated
 * code at bootstrap time.
 *
 * @author Mike Brock
 */
@EnabledByProperty(value = "errai.ioc.async_bean_manager", negated = true)
public class SyncBeanManagerImpl implements SyncBeanManager, SyncBeanManagerSetup {
  /**
   * A map of all named beans.
   */
  private final Map<String, List<IOCBeanDef>> namedBeans
      = new HashMap<String, List<IOCBeanDef>>();

  /**
   * A map of all beans managed by the bean mean manager, keyed by type.
   */
  private final Map<Class<?>, List<IOCBeanDef>> beanMap
      = new HashMap<Class<?>, List<IOCBeanDef>>();

  /**
   * A map which contains bean instances as keys, and their associated {@link CreationalContext}s as values.
   */
  private final Map<Object, CreationalContext> creationalContextMap
      = new IdentityHashMap<Object, CreationalContext>();

  /**
   * A map which contains proxied instances as keys, and the underlying proxied bean instances as values.
   */
  private final Map<Object, Object> proxyLookupForManagedBeans
      = new IdentityHashMap<Object, Object>();

  /**
   * A collection which contains a list of all known concrete bean types being managed. eg. no interface or
   * abstract types will be present in this collection.
   */
  private final Set<String> concreteBeans
      = new HashSet<String>();

  public SyncBeanManagerImpl() {
    // java.lang.Object is "special" in that it is treated like a concrete bean type for the purpose of
    // lookups. This modifies the lookup behavior to exclude other non-concrete types from qualified matching.
    concreteBeans.add("java.lang.Object");
  }

  private IOCBeanDef<Object> _registerSingletonBean(final Class<Object> type,
                                                    final Class<?> beanType,
                                                    final BeanProvider<Object> callback,
                                                    final Object instance,
                                                    final Annotation[] qualifiers,
                                                    final String name,
                                                    final boolean concrete,
                                                    final Class<Object> beanActivatorType) {

    return registerBean(IOCSingletonBean.newBean(
        this, type, beanType, qualifiers, name, concrete, callback, instance, beanActivatorType));
  }

  private IOCBeanDef<Object> _registerDependentBean(final Class<Object> type,
                                                    final Class<?> beanType,
                                                    final BeanProvider<Object> callback,
                                                    final Annotation[] qualifiers,
                                                    final String name,
                                                    final boolean concrete,
                                                    final Class<Object> beanActivatorType) {

    return registerBean(IOCDependentBean.newBean(
        this, type, beanType, qualifiers, name, concrete, callback, beanActivatorType));
  }

  private void registerSingletonBean(final Class<Object> type,
                                     final Class<?> beanType,
                                     final BeanProvider<Object> callback,
                                     final Object instance,
                                     final Annotation[] qualifiers,
                                     final String beanName,
                                     final boolean concrete,
                                     final Class<Object> activator) {


    _registerNamedBean(beanName, 
        _registerSingletonBean(type, beanType, callback, instance, qualifiers, beanName, concrete, activator));
  }

  private void registerDependentBean(final Class<Object> type,
                                     final Class<?> beanType,
                                     final BeanProvider<Object> callback,
                                     final Annotation[] qualifiers,
                                     final String beanName,
                                     final boolean concrete,
                                     final Class<Object> activator) {

    _registerNamedBean(beanName, 
        _registerDependentBean(type, beanType, callback, qualifiers, beanName, concrete, activator));
  }

  private void _registerNamedBean(final String name,
                                  final IOCBeanDef beanDef) {
    if (beanDef.isConcrete()) {
      if (!namedBeans.containsKey(name)) {
        namedBeans.put(name, new ArrayList<IOCBeanDef>());
      }
      namedBeans.get(name).add(beanDef);
    }
  }


  @Override
  public void addBean(final Class<Object> type,
                      final Class<?> beanType,
                      final BeanProvider<Object> callback,
                      final Object instance,
                      final Annotation[] qualifiers) {

    addBean(type, beanType, callback, instance, qualifiers, null, true, null);
  }

  @Override
  public void addBean(final Class<Object> type,
                      final Class<?> beanType,
                      final BeanProvider<Object> callback,
                      final Object instance,
                      final Annotation[] qualifiers,
                      final String name) {

    addBean(type, beanType, callback, instance, qualifiers, name, true, null);
  }

  @Override
  public void addBean(final Class<Object> type,
                      final Class<?> beanType,
                      final BeanProvider<Object> callback,
                      Object instance,
                      final Annotation[] qualifiers,
                      final String name,
                      final boolean concreteType,
                      final Class<Object> beanActivatorType) {

    if (instance == SimpleInjectionContext.LAZY_INIT_REF) {
      throw new RuntimeException("you cannot record a lazy initialization reference!");
    }

    if (concreteType) {
      concreteBeans.add(type.getName());
    }

    if (instance != null) {
      registerSingletonBean(type, beanType, callback, instance, qualifiers, name, concreteType, beanActivatorType);
    }
    else {
      registerDependentBean(type, beanType, callback, qualifiers, name, concreteType, beanActivatorType);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void destroyBean(final Object ref) {
    final SimpleCreationalContext creationalContext =
        (SimpleCreationalContext) creationalContextMap.get(getActualBeanReference(ref));

    if (creationalContext == null) {
      return;
    }

    creationalContext.destroyContext();

    for (final Object inst : creationalContext.getAllCreatedBeanInstances()) {
      creationalContextMap.remove(inst);
      proxyLookupForManagedBeans.remove(inst);
      proxyLookupForManagedBeans.values().remove(inst);
    }
  }

  @Override
  public boolean isManaged(final Object ref) {
    return creationalContextMap.containsKey(getActualBeanReference(ref));
  }

  @Override
  public Object getActualBeanReference(final Object ref) {
    if (isProxyReference(ref)) {
      return proxyLookupForManagedBeans.get(ref);
    }
    else {
      return ref;
    }
  }

  @Override
  public boolean isProxyReference(final Object ref) {
    return proxyLookupForManagedBeans.containsKey(ref);
  }

  @Override
  public void addProxyReference(final Object proxyRef, final Object realRef) {
    proxyLookupForManagedBeans.put(proxyRef, realRef);
  }

  @Override
  public void addBeanToContext(final Object ref, final CreationalContext creationalContext) {
    creationalContextMap.put(ref, creationalContext);
  }

  @Override
  public <T> IOCBeanDef<T> registerBean(final IOCBeanDef<T> bean) {
    if (!beanMap.containsKey(bean.getType())) {
      beanMap.put(bean.getType(), new ArrayList<IOCBeanDef>());
    }
    beanMap.get(bean.getType()).add(bean);
    return bean;
  }

  @Override
  public Collection<IOCBeanDef> lookupBeans(final String name) {
    if (!namedBeans.containsKey(name)) {
      return Collections.emptyList();
    }
    else {
      return namedBeans.get(name);
    }
  }

  @Override
  public <T> Collection<IOCBeanDef<T>> lookupBeans(final Class<T> type) {
    final List<IOCBeanDef> beanList;

    if (type.getName().equals("java.lang.Object")) {
      beanList = new ArrayList<IOCBeanDef>();
      for (final List<IOCBeanDef> list : beanMap.values()) {
        beanList.addAll(list);
      }
    }
    else {
      beanList = beanMap.get(type);
    }

    final List<IOCBeanDef<T>> matching = new ArrayList<IOCBeanDef<T>>();

    if (beanList != null) {
      for (final IOCBeanDef<T> beanDef : beanList) {
        matching.add(beanDef);
      }
    }

    return Collections.unmodifiableList(matching);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> Collection<IOCBeanDef<T>> lookupBeans(final Class<T> type, final Annotation... qualifierInstances) {
    final List<IOCBeanDef> beanList;

    if (type.getName().equals("java.lang.Object")) {
      beanList = new ArrayList<IOCBeanDef>();
      for (final List<IOCBeanDef> list : beanMap.values()) {
        beanList.addAll(list);
      }
    }
    else {
      beanList = beanMap.get(type);
    }

    if (beanList == null) {
      return Collections.emptyList();
    }

    final List<IOCBeanDef<T>> matching = new ArrayList<IOCBeanDef<T>>();

    final Annotation[] qualifiers = (qualifierInstances == null) ? new Annotation[0] : qualifierInstances;
      final Set<Annotation> qualifierSet = new HashSet<Annotation>(qualifiers.length * 2);
      Collections.addAll(qualifierSet, qualifiers);

      final boolean defaultMatch = QualifierUtil.isDefaultAnnotations(qualifierInstances);

      for (final IOCBeanDef iocBean : beanList) {
        if (defaultMatch || iocBean.matches(qualifierSet)) {
          matching.add(iocBean);
        }
      }
    if (matching.size() == 1) {
      return Collections.unmodifiableList(matching);
    }

    if (matching.size() > 1) {
      // perform second pass
      final Iterator<IOCBeanDef<T>> secondIterator = matching.iterator();

      if (concreteBeans.contains(type.getName())) {
        while (secondIterator.hasNext()) {
          if (!secondIterator.next().isConcrete())
            secondIterator.remove();
        }
      }
      else {
        while (secondIterator.hasNext()) {
          if (!concreteBeans.contains(secondIterator.next().getBeanClass().getName()))
            secondIterator.remove();
        }
      }
    }

    return Collections.unmodifiableList(matching);
  }

  @Override
  public <T> IOCBeanDef<T> lookupBean(final Class<T> type, final Annotation... qualifiers) {
    final Collection<IOCBeanDef<T>> matching = lookupBeans(type, qualifiers);

    if (matching.size() == 1) {
      return matching.iterator().next();
    }
    else if (matching.isEmpty()) {
      throw new IOCResolutionException("no matching bean instances for: " + type.getName());
    }
    else {
      throw new IOCResolutionException("multiple matching bean instances for: " + type.getName() + " matches: " + matching);
    }
  }

  @Override
  public boolean addDestructionCallback(final Object beanInstance, final DestructionCallback<?> destructionCallback) {
    final CreationalContext creationalContext = creationalContextMap.get(beanInstance);
    if (creationalContext == null) {
      return false;
    }

    creationalContext.addDestructionCallback(beanInstance, destructionCallback);
    return true;
  }

  @Override
  public void destroyAllBeans() {
    namedBeans.clear();
    beanMap.clear();
  }
}
