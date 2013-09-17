package org.jboss.errai.ioc.client.container;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Set;

/**
 * The <tt>CreationalContext</tt> is as its name implies, the context used in the creation of beans. There is a
 * one-to-one relationship between a <tt>CreationalContext</tt> and a scope. The creation of new dependent scope beans
 * and forced new-instance creation of normal-scoped beans are contained in their own <tt>CreationalContext</tt>.
 * <p>
 * The context is used for regulating lifecycle activities as well as managing proxies generated in the creation
 * of the beans.
 *
 * @author Mike Brock
 */
public interface CreationalContext {
  /**
    * Records a {@link InitializationCallback} to the creational context.
    *
    * @param beanInstance
    *     the instance of the bean associated with the {@link InitializationCallback}
    * @param callback
    *     the instance of the {@link InitializationCallback}
    */
  void addInitializationCallback(Object beanInstance, InitializationCallback callback);


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
  void addDestructionCallback(Object beanInstance, DestructionCallback callback);

  /**
   * Adds a lookup from a proxy to the actual bean instance that it is proxying. This is called directly by the
   * bootstrapping code.
   *
   * @param proxyRef
   *     the reference to the proxy instance
   * @param realRef
   *     the reference to the actual bean instance which the proxy wraps
   */
  @SuppressWarnings("UnusedDeclaration") // used by generated code
  void addProxyReference(Object proxyRef, Object realRef);

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
  BeanRef getBeanReference(Class<?> beanType, Annotation[] qualifiers);

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
  void addBean(Class<?> beanType, Annotation[] qualifiers, Object instance);

  /**
   * Adds a bean to the creational context based on the {@link BeanRef} with a reference to the an actual instantiated
   * instance of the bean.
   *
   * @param ref
   *     the {@link BeanRef} representing the bean
   * @param instance
   *     the instance of the bean
   */
  void addBean(BeanRef ref, Object instance);


  /**
   * Returns a list of all created beans within this creational context.
   *
   * @return An unmodifiable set of all the created beans within this creational context.
   */
  Set<BeanRef> getAllCreatedBeans();

  /**
   * Returns a list of the instances of every created bean within this creational context.
   *
   * @return An unmodifiable collection of every bean instance within the creational context.
   */

  Collection<Object> getAllCreatedBeanInstances();


  /**
   * Adds an unresolved proxy into the creational context. This is called to indicate a proxy was required while
   * building a bean, due to a forward reference in a cycle situation. The caller is responsible, through the providing
   * of the {@link ProxyResolver} callback, for implementing its own proxy closing strategy.
   * <p/>
   * After a creational context has added all beans to the context, calling {@link #finish()} will result in all of
   * the provided {@link ProxyResolver}s being executed.
   * <p/>
   * This method is typically called directly by the generated bootstrapper.
   *
   * @param proxyResolver
   *     the {@link ProxyResolver} used for handling closure of the cycle.
   * @param beanType
   *     the type of the bean
   * @param qualifiers
   *     the qualifiers for the bean
   */
  @SuppressWarnings("UnusedDeclaration") // used by generated code
  void addUnresolvedProxy(ProxyResolver proxyResolver,
                          Class<?> beanType,
                          Annotation[] qualifiers);


}
