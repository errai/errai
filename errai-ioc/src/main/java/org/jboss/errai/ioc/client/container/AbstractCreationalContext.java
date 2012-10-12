package org.jboss.errai.ioc.client.container;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Mike Brock
 */
public abstract class AbstractCreationalContext implements CreationalContext {
  protected final boolean immutableContext;
  protected final Class<? extends Annotation> scope;

  protected final List<Tuple<Object, InitializationCallback>> initializationCallbacks =
      new ArrayList<Tuple<Object, InitializationCallback>>();

  protected final List<Tuple<Object, DestructionCallback>> destructionCallbacks
      = new ArrayList<Tuple<Object, DestructionCallback>>();

  protected final Map<BeanRef, List<ProxyResolver>> unresolvedProxies
      = new LinkedHashMap<BeanRef, List<ProxyResolver>>();

  protected final Map<BeanRef, Object> wired = new LinkedHashMap<BeanRef, Object>();

  protected AbstractCreationalContext(final Class<? extends Annotation> scope) {
    this.immutableContext = false;
    this.scope = scope;
  }

  protected AbstractCreationalContext(final boolean immutableContext, final Class<? extends Annotation> scope) {
    this.immutableContext = immutableContext;
    this.scope = scope;
  }

  /**
   * Records a {@link InitializationCallback} to the creational context. All initialization callbacks are executed
   * when the {@link #finish()} method is called.
   *
   * @param beanInstance
   *     the instance of the bean associated witht he {@link InitializationCallback}
   * @param callback
   *     the instance of the {@link InitializationCallback}
   */
  @Override
  public void addInitializationCallback(final Object beanInstance, final InitializationCallback callback) {
    initializationCallbacks.add(Tuple.of(beanInstance, callback));
  }

  /**
   * Records a {@link DestructionCallback} to the creational context. All destruction callbacks are executed
   * by the bean manager for a creational context when any of the beans within the creational context are
   * destroyed.
   *
   * @param beanInstance
   *     the instance of the bean associated with the {@link DestructionCallback}.
   * @param callback
   *     the instance of the {@link DestructionCallback}
   */
  @Override
  public void addDestructionCallback(final Object beanInstance, final DestructionCallback callback) {
    destructionCallbacks.add(Tuple.of(beanInstance, callback));
  }


  /**
   * Returns a {@link BeanRef} which matches the specified type and qualifiers whether or not the bean is within
   * the creational context or not.
   *
   * @param beanType
   *     the type of the bean
   * @param qualifiers
   *     the qualifiers for the bean
   *
   * @return a {@link BeanRef} matching the specified type and qualifiers.
   */
  @Override
  public BeanRef getBeanReference(final Class<?> beanType, final Annotation[] qualifiers) {
    return new BeanRef(beanType, qualifiers);
  }

  /**
   * Adds a bean to the creational context based on the specified bean type and qualifiers with a reference to an
   * actual instantiated instance of the bean.
   *
   * @param beanType
   *     the type of the bean
   * @param qualifiers
   *     the qualifiers for the bean
   * @param instance
   *     the instance to the bean
   */
  @Override
  public void addBean(final Class<?> beanType, final Annotation[] qualifiers, final Object instance) {
    addBean(getBeanReference(beanType, qualifiers), instance);
  }

  /**
   * Adds a bean to the creational context based on the {@link BeanRef} with a reference to the an actual instantiated
   * instance of the bean.
   *
   * @param ref
   *     the {@link BeanRef} representing the bean
   * @param instance
   *     the instance of the bean
   */
  @Override
  public void addBean(final BeanRef ref, final Object instance) {
    if (!wired.containsKey(ref)) {
      wired.put(ref, instance);
    }
  }

  /**
   * Returns a list of all created beans within this creational context.
   *
   * @return An unmodifiable set of all the created beans within this creational context.
   */
  @Override
  public Set<BeanRef> getAllCreatedBeans() {
    return Collections.unmodifiableSet(wired.keySet());
  }

  /**
   * Returns a list of the instances of every created bean within this creational context.
   *
   * @return An unmodifiable collection of every bean instance within the creational context.
   */
  @Override
  public Collection<Object> getAllCreatedBeanInstances() {
    return Collections.unmodifiableCollection(wired.values());
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
  @Override
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
   * Adds an unresolved proxy into the creational context. This is called to indicate a proxy was required while
   * building a bean, due to a forward reference in a cycle situation. The caller is responsible, through the providing
   * of the {@link ProxyResolver} callback, for implementing its own proxy closing strategy.
   * <p/>
   * After a creational context has added all beans to the context, calling {@link #finish()} will result in all of
   * the provided {@link ProxyResolver}s being executed.
   * <p/>
   * This method is typically called directly by the generated bootstapper.
   *
   * @param proxyResolver
   *     the {@link ProxyResolver} used for handling closure of the cycle.
   * @param beanType
   *     the type of the bean
   * @param qualifiers
   *     the qualifiers for the bean
   */
  @Override
  @SuppressWarnings("UnusedDeclaration") // used by generated code
  public void addUnresolvedProxy(final ProxyResolver proxyResolver,
                                 final Class<?> beanType,
                                 final Annotation[] qualifiers) {

    final BeanRef ref = getBeanReference(beanType, qualifiers);

    if (!unresolvedProxies.containsKey(ref)) {
      unresolvedProxies.put(ref, new ArrayList<ProxyResolver>());
    }

    unresolvedProxies.get(ref).add(proxyResolver);
  }


  /**
   * Fires all {@link InitializationCallback}s which were declared during creation of the beans.
   */
  @SuppressWarnings("unchecked")
  protected void fireAllInitCallbacks() {
    for (final Tuple<Object, InitializationCallback> entry : initializationCallbacks) {
      entry.getValue().init(entry.getKey());
    }
  }

  /**
   * Fires all {@link DestructionCallback}s within the context.
   */
  @SuppressWarnings("unchecked")
  void destroyContext() {
    if (immutableContext) {
      throw new IllegalStateException("scope [" + scope.getName() + "] is an immutable scope and cannot be destroyed");
    }

    for (final Tuple<Object, DestructionCallback> tuple : destructionCallbacks) {
      tuple.getValue().destroy(tuple.getKey());
    }
  }
}
