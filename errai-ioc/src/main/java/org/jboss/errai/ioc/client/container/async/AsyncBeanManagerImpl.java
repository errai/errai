package org.jboss.errai.ioc.client.container.async;

import java.lang.annotation.Annotation;
import java.util.*;

import org.jboss.errai.ioc.client.QualifierUtil;
import org.jboss.errai.ioc.client.api.EnabledByProperty;
import org.jboss.errai.ioc.client.container.CreationalContext;
import org.jboss.errai.ioc.client.container.DestructionCallback;
import org.jboss.errai.ioc.client.container.IOCResolutionException;

import com.google.gwt.user.client.Timer;

/**
 * @author Mike Brock
 */
@EnabledByProperty(value = "errai.ioc.async_bean_manager")
public class AsyncBeanManagerImpl implements AsyncBeanManager, AsyncBeanManagerSetup {
  /**
   * A map of all named beans.
   */
  private final Map<String, List<AsyncBeanDef>> namedBeans = new HashMap<String, List<AsyncBeanDef>>();

  /**
   * A map of all beans managed by the bean mean manager, keyed by type.
   */
  private final Map<Class<?>, List<AsyncBeanDef>> beanMap = new HashMap<Class<?>, List<AsyncBeanDef>>();

  /**
   * A map which contains bean instances as keys, and their associated
   * {@link org.jboss.errai.ioc.client.container.CreationalContext}s as values.
   */
  private final Map<Object, CreationalContext> creationalContextMap = new IdentityHashMap<Object, CreationalContext>();

  /**
   * A map which contains proxied instances as keys, and the underlying proxied bean instances as
   * values.
   */
  private final Map<Object, Object> proxyLookupForManagedBeans = new IdentityHashMap<Object, Object>();

  /**
   * A collection which contains a list of all known concrete bean types being managed. eg. no
   * interface or abstract types will be present in this collection.
   */
  private final Set<String> concreteBeans = new HashSet<String>();

  public AsyncBeanManagerImpl() {

    // java.lang.Object is "special" in that it is treated like a concrete bean type for the purpose
    // of
    // lookups. This modifies the lookup behavior to exclude other non-concrete types from qualified
    // matching.
    concreteBeans.add("java.lang.Object");
  }

  private AsyncBeanDef<Object> _registerSingletonBean(final Class<Object> type,
                                                      final Class<?> beanType,
                                                      final AsyncBeanProvider<Object> callback,
                                                      final Object instance,
                                                      final Annotation[] qualifiers,
                                                      final String name,
                                                      final boolean concrete,
                                                      final Class<Object> beanActivatorType) {

    return registerBean(AsyncSingletonBean.newBean(this, type, beanType, qualifiers, name, concrete, callback,
        instance, beanActivatorType));
  }

  private AsyncBeanDef<Object> _registerDependentBean(final Class<Object> type,
                                                      final Class<?> beanType,
                                                      final AsyncBeanProvider<Object> callback,
                                                      final Annotation[] qualifiers,
                                                      final String name,
                                                      final boolean concrete,
                                                      final Class<Object> beanActivatorType) {

    return registerBean(
        AsyncDependentBean.newBean(this, type, beanType, qualifiers, name, concrete, callback, beanActivatorType));
  }

  private void registerSingletonBean(final Class<Object> type,
                                     final Class<?> beanType,
                                     final AsyncBeanProvider<Object> callback,
                                     final Object instance,
                                     final Annotation[] qualifiers,
                                     final String beanName,
                                     final boolean concrete,
                                     final Class<Object> beanActivatorType) {


    _registerNamedBean(beanName, 
        _registerSingletonBean(type, beanType, callback, instance, qualifiers, beanName, concrete, beanActivatorType));
  }

  private void registerDependentBean(final Class<Object> type,
                                     final Class<?> beanType,
                                     final AsyncBeanProvider<Object> callback,
                                     final Annotation[] qualifiers,
                                     final String beanName,
                                     final boolean concrete,
                                     final Class<Object> beanActivatorType) {

    _registerNamedBean(beanName, 
        _registerDependentBean(type, beanType, callback, qualifiers, beanName, concrete, beanActivatorType));
  }

  private void _registerNamedBean(final String name,
                                  final AsyncBeanDef beanDef) {
    if (name == null)
      return;

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

    addBean(type, beanType, callback, instance, qualifiers, null, beanType.equals(type), null);
  }

  @Override
  public void addBean(final Class<Object> type,
                      final Class<?> beanType,
                      final AsyncBeanProvider<Object> callback,
                      final Object instance,
                      final Annotation[] qualifiers,
                      final String name) {

    addBean(type, beanType, callback, instance, qualifiers, name, beanType.equals(type), null);
  }

  @Override
  public void addBean(final Class<Object> type,
                      final Class<?> beanType,
                      final AsyncBeanProvider<Object> callback,
                      final Object instance,
                      final Annotation[] qualifiers,
                      final String name,
                      final boolean concreteType,
                      final Class<Object> beanActivatorType) {

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

  @Override
  public void destroyBean(final Object ref, final Runnable runnable) {
    destroyBean(ref);

    /**
     * Just yield. In truth, all code will have been downloaded at this time, which means the only
     * problem is that we need to make callback-based lookup. A minimum yield is sufficient to
     * guarantee everything has happened.
     */
    new Timer() {
      @Override
      public void run() {
        runnable.run();
      }
    }.schedule(1);
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

  /**
   * Associates a bean instance with a creational context.
   * 
   * @param ref
   *          the reference to the bean
   * @param creationalContext
   *          the {@link CreationalContext} instance to associate the bean instance with.
   */
  @Override
  public void addBeanToContext(final Object ref, final CreationalContext creationalContext) {
    creationalContextMap.put(ref, creationalContext);
  }

  private <T> AsyncBeanDef<T> registerBean(final AsyncBeanDef<T> bean) {
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
  public <T> Collection<AsyncBeanDef<T>> lookupBeans(final Class<T> type, final Annotation... qualifierInstances) {
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

    final List<AsyncBeanDef<T>> matching = new ArrayList<AsyncBeanDef<T>>();

    final Annotation[] qualifiers = (qualifierInstances == null) ? new Annotation[0] : qualifierInstances;
    final Set<Annotation> qualifierSet = new HashSet<Annotation>(qualifiers.length * 2);
    Collections.addAll(qualifierSet, qualifiers);

    final boolean defaultMatch = QualifierUtil.isDefaultAnnotations(qualifierInstances);

    for (final AsyncBeanDef iocBean : beanList) {
      if (defaultMatch || iocBean.matches(qualifierSet)) {
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
  public <T> AsyncBeanDef<T> lookupBean(final Class<T> type, final Annotation... qualifiers) {
    final Collection<AsyncBeanDef<T>> matching = lookupBeans(type, qualifiers);

    if (matching.size() == 1) {
      return matching.iterator().next();
    }
    else if (matching.isEmpty()) {
      throw new IOCResolutionException("no matching bean instances for: " + type.getName());
    }
    else {
      throw new IOCResolutionException("multiple matching bean instances for: "
          + type.getName() + " matches: " + matching);
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
