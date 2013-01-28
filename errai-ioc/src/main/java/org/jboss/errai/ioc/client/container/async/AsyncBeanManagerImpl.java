package org.jboss.errai.ioc.client.container.async;

import org.jboss.errai.ioc.client.container.CreationalContext;
import org.jboss.errai.ioc.client.container.DestructionCallback;
import org.jboss.errai.ioc.client.container.IOCResolutionException;
import org.jboss.errai.ioc.client.container.IOCSingletonBean;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Mike Brock
 */
public class AsyncBeanManagerImpl implements AsyncBeanManager {
  /**
   * A map of all named beans.
   */
  private final Map<String, List<AsyncBeanDef>> namedBeans
      = new HashMap<String, List<AsyncBeanDef>>();

  /**
   * A map of all beans managed by the bean mean manager, keyed by type.
   */
  private final Map<Class<?>, List<AsyncBeanDef>> beanMap
      = new HashMap<Class<?>, List<AsyncBeanDef>>();

  /**
   * A map which contains bean instances as keys, and their associated {@link org.jboss.errai.ioc.client.container.CreationalContext}s as values.
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

  public AsyncBeanManagerImpl() {
    // java.lang.Object is "special" in that it is treated like a concrete bean type for the purpose of
    // lookups. This modifies the lookup behavior to exclude other non-concrete types from qualified matching.
    concreteBeans.add("java.lang.Object");
  }

  private AsyncBeanDef<Object> _registerSingletonBean(final Class<Object> type,
                                                      final Class<?> beanType,
                                                      final AsyncBeanProvider<Object> callback,
                                                      final Object instance,
                                                      final Annotation[] qualifiers,
                                                      final String name,
                                                      final boolean concrete) {

    return registerBean(AsyncSingletonBean.newBean(this, type, beanType, qualifiers, name, concrete, callback, instance));
  }

  private AsyncBeanDef<Object> _registerDependentBean(final Class<Object> type,
                                                      final Class<?> beanType,
                                                      final AsyncBeanProvider<Object> callback,
                                                      final Annotation[] qualifiers,
                                                      final String name,
                                                      final boolean concrete) {

    return registerBean(AsyncDependentBean.newBean(this, type, beanType, qualifiers, name, concrete, callback));
  }

  private void registerSingletonBean(final Class<Object> type,
                                     final Class<?> beanType,
                                     final AsyncBeanProvider<Object> callback,
                                     final Object instance,
                                     final Annotation[] qualifiers,
                                     final String beanName,
                                     final boolean concrete) {


    _registerNamedBean(beanName, _registerSingletonBean(type, beanType, callback, instance, qualifiers, beanName, concrete));
  }

  private void registerDependentBean(final Class<Object> type,
                                     final Class<?> beanType,
                                     final AsyncBeanProvider<Object> callback,
                                     final Annotation[] qualifiers,
                                     final String beanName,
                                     final boolean concrete) {

    _registerNamedBean(beanName, _registerDependentBean(type, beanType, callback, qualifiers, beanName, concrete));
  }

  private void _registerNamedBean(final String name,
                                  final AsyncBeanDef beanDef) {
    if (name == null) return;

    if (!namedBeans.containsKey(name)) {
      namedBeans.put(name, new ArrayList<AsyncBeanDef>());
    }
    namedBeans.get(name).add(beanDef);
  }

  @Override
  public void addBean(final Class<Object> type,
                      final Class<?> beanType,
                      final AsyncBeanProvider<Object> callback,
                      final Object instance,
                      final Annotation[] qualifiers) {

    addBean(type, beanType, callback, instance, qualifiers, null, beanType.equals(type));
  }

  @Override
  public void addBean(final Class<Object> type,
                      final Class<?> beanType,
                      final AsyncBeanProvider<Object> callback,
                      final Object instance,
                      final Annotation[] qualifiers,
                      final String name) {

    addBean(type, beanType, callback, instance, qualifiers, name, true);
  }


  @Override
  public void addBean(final Class<Object> type,
                      final Class<?> beanType,
                      final AsyncBeanProvider<Object> callback,
                      final Object instance,
                      final Annotation[] qualifiers,
                      final String name,
                      final boolean concreteType) {

    if (concreteType) {
      concreteBeans.add(type.getName());
    }

    if (instance != null) {
      registerSingletonBean(type, beanType, callback, instance, qualifiers, name, concreteType);
    }
    else {
      registerDependentBean(type, beanType, callback, qualifiers, name, concreteType);
    }
  }


  @SuppressWarnings("unchecked")
  public void destroyBean(final Object ref) {
    final AsyncCreationalContext creationalContext =
        (AsyncCreationalContext) creationalContextMap.get(getActualBeanReference(ref));

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


  public boolean isManaged(final Object ref) {
    return creationalContextMap.containsKey(getActualBeanReference(ref));
  }

  public Object getActualBeanReference(final Object ref) {
    if (isProxyReference(ref)) {
      return proxyLookupForManagedBeans.get(ref);
    }
    else {
      return ref;
    }
  }

  public boolean isProxyReference(final Object ref) {
    return proxyLookupForManagedBeans.containsKey(ref);
  }


  public void addProxyReference(final Object proxyRef, final Object realRef) {
    proxyLookupForManagedBeans.put(proxyRef, realRef);
  }

  /**
   * Associates a bean instance with a creational context.
   *
   * @param ref
   *     the reference to the bean
   * @param creationalContext
   *     the {@link CreationalContext} instance to associate the bean instance with.
   */
  public void addBeanToContext(final Object ref, final CreationalContext creationalContext) {
    creationalContextMap.put(ref, creationalContext);
  }


  @Override
  public <T> AsyncBeanDef<T> registerBean(final AsyncBeanDef<T> bean) {
    if (!beanMap.containsKey(bean.getType())) {
      beanMap.put(bean.getType(), new ArrayList<AsyncBeanDef>());
    }

    beanMap.get(bean.getType()).add(bean);
    return bean;
  }


  @Override
  public Collection<AsyncBeanDef> lookupBeans(final String name) {
    if (!namedBeans.containsKey(name)) {
      return Collections.emptyList();
    }
    else {
      return namedBeans.get(name);
    }
  }


  @Override
  @SuppressWarnings("unchecked")
  public <T> Collection<AsyncBeanDef<T>> lookupBeans(final Class<T> type) {
    final List<AsyncBeanDef> beanList;

    if (type.getName().equals("java.lang.Object")) {
      beanList = new ArrayList<AsyncBeanDef>();
      for (final List<AsyncBeanDef> list : beanMap.values()) {
        beanList.addAll(list);
      }
    }
    else {
      beanList = beanMap.get(type);
    }

    final List<AsyncBeanDef<T>> matching = new ArrayList<AsyncBeanDef<T>>();

    if (beanList != null) {
      for (final AsyncBeanDef<T> beanDef : beanList) {
        matching.add(beanDef);
      }
    }

    return Collections.unmodifiableList(matching);
  }


  @Override
  @SuppressWarnings("unchecked")
  public <T> Collection<AsyncBeanDef<T>> lookupBeans(final Class<T> type, final Annotation... qualifiers) {
    final List<AsyncBeanDef> beanList;

    if (type.getName().equals("java.lang.Object")) {
      beanList = new ArrayList<AsyncBeanDef>();
      for (final List<AsyncBeanDef> list : beanMap.values()) {
        beanList.addAll(list);
      }
    }
    else {
      beanList = beanMap.get(type);
    }

    if (beanList == null) {
      return Collections.emptyList();
    }
    else if (beanList.size() == 1) {
      return Collections.singletonList((AsyncBeanDef<T>) beanList.iterator().next());
    }

    final List<AsyncBeanDef<T>> matching = new ArrayList<AsyncBeanDef<T>>();

    final Set<Annotation> qualifierSet = new HashSet<Annotation>(qualifiers.length * 2);
    Collections.addAll(qualifierSet, qualifiers);

    for (final AsyncBeanDef iocBean : beanList) {
      if (iocBean.matches(qualifierSet)) {
        matching.add(iocBean);
      }
    }

    if (matching.size() == 1) {
      return Collections.unmodifiableList(matching);
    }

    if (matching.size() > 1) {
      // perform second pass
      final Iterator<AsyncBeanDef<T>> secondIterator = matching.iterator();

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
  @SuppressWarnings("unchecked")
  public <T> AsyncBeanDef<T> lookupBean(final Class<T> type, final Annotation... qualifiers) {
    final Collection<AsyncBeanDef<T>> matching = lookupBeans(type, qualifiers);

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

  public boolean addDestructionCallback(final Object beanInstance, final DestructionCallback<?> destructionCallback) {
    final CreationalContext creationalContext = creationalContextMap.get(beanInstance);
    if (creationalContext == null) {
      return false;
    }

    creationalContext.addDestructionCallback(beanInstance, destructionCallback);
    return true;
  }

  public void destroyAllBeans() {
    namedBeans.clear();
    beanMap.clear();
  }
}
