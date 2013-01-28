package org.jboss.errai.ioc.client.container.async;

import javax.enterprise.context.ApplicationScoped;
import java.lang.annotation.Annotation;

/**
 * @author Mike Brock
 */
public class AsyncSingletonBean<T> extends AsyncDependentBean<T> {
  private final T instance;

  private AsyncSingletonBean(final AsyncBeanManagerImpl beanManager,
                           final Class<T> type,
                           final Class<?> beanType,
                           final Annotation[] qualifiers,
                           final String name,
                           final boolean concrete,
                           final AsyncBeanProvider<T> callback,
                           final T instance) {

    super(beanManager, type, beanType, qualifiers, name, concrete, callback);
    this.instance = instance;
  }

  /**
   * Creates a new IOC Bean reference
   *
   * @param type
   *     The type of a bean
   * @param qualifiers
   *     The qualifiers of the bean.
   * @param name
   *     The name of the bean
   * @param instance
   *     The instance of the bean.
   * @param <T>
   *     The type of the bean
   *
   * @return A new instance of <tt>IOCSingletonBean</tt>
   */
  public static <T> AsyncBeanDef<T> newBean(final AsyncBeanManagerImpl beanManager,
                                          final Class<T> type,
                                          final Class<?> beanType,
                                          final Annotation[] qualifiers,
                                          final String name,
                                          final boolean concrete,
                                          final AsyncBeanProvider<T> callback,
                                          final T instance) {

    return new AsyncSingletonBean<T>(beanManager, type, beanType, qualifiers, name, concrete, callback, instance);
  }

  @Override
  public void getInstance(final CreationalCallback<T> callback, final AsyncCreationalContext context) {
    callback.callback(instance);
  }

  @Override
  public Class<? extends Annotation> getScope() {
    return ApplicationScoped.class;
  }

  @Override
  public String toString() {
    return "AsyncSingletonBean [instance=" + instance + "]";
  }
}
