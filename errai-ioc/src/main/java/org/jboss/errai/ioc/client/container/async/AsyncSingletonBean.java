package org.jboss.errai.ioc.client.container.async;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.common.client.util.CreationalCallback;

import com.google.common.collect.Maps;
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
                           final T instance,
                           final Class<Object> beanActivatorType) {

    super(beanManager, type, beanType, qualifiers, name, concrete, callback, beanActivatorType);
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
   * @param activator
   *    The bean activator to use, may be null.    
   *
   * @return A new instance of <tt>IOCSingletonBean</tt>
   */
    public static <T> AsyncBeanDef<T> newBean(final AsyncBeanManagerImpl beanManager, final Class<T> type, final Class<?> beanType, final Annotation[] qualifiers, final String name,
            final boolean concrete, final AsyncBeanProvider<T> callback, final T instance,
                                          final Class<Object> beanActivatorType) {
    return new AsyncSingletonBean<T>(
        beanManager, type, beanType, qualifiers, name, concrete, callback, instance, beanActivatorType);
    }
    
    //ensures that even when having multiple async beans only one instance is created global
    private static final Map<Class<?>, SingletonBeanProvider<?>> lazySingletons = new HashMap<Class<?>, SingletonBeanProvider<?>>();
    
    @Override
    public synchronized void getInstance(CreationalCallback<T> callback, final AsyncCreationalContext context) {
        SingletonBeanProvider<T> singletonBeanProvider = (SingletonBeanProvider<T>)lazySingletons.get(beanType);
        if (singletonBeanProvider == null) {
            singletonBeanProvider = new SingletonBeanProvider<T>(){
                @Override
                protected void getNewInstance(CreationalCallback<T> callback) {
                    newInstance(callback);
                };
            };
            lazySingletons.put(beanType, singletonBeanProvider);
        }
        singletonBeanProvider.getInstance(callback);
    }
    
    @Override
    public void getInstance(CreationalCallback<T> callback) {
        getInstance(callback, null);
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
