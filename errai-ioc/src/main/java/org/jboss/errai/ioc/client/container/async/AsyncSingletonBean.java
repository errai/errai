package org.jboss.errai.ioc.client.container.async;

import java.lang.annotation.Annotation;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;

import org.jboss.errai.common.client.util.CreationalCallback;

/**
 * @author Mike Brock
 */
public class AsyncSingletonBean<T> extends AsyncDependentBean<T> {
  private final T instance;
  private final AsyncInjectionContext injectionContext;
  
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
    // normal singleton
    this.instance = instance;
    this.injectionContext = null;
    getOrCreateSingletonBeanProvider();
  }
  
  private AsyncSingletonBean(final AsyncBeanManagerImpl beanManager,
  							 final Class<T> type,
        				 	 final Class<?> beanType,
        				 	 final Annotation[] qualifiers,
        				 	 final String name,
          					 final boolean concrete,
          					 final AsyncBeanProvider<T> callback,
          					 final AsyncCreationalContext context,
         					 final AsyncInjectionContext injectionContext,
         					 final Class<Object> beanActivatorType) {
    super(beanManager, type, beanType, qualifiers, name, concrete, callback, beanActivatorType);
    // lazy singleton
    this.instance = null;
    this.injectionContext = injectionContext;
    getOrCreateSingletonBeanProvider();
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
  public static <T> AsyncBeanDef<T> newBean(final AsyncBeanManagerImpl beanManager,
                                          final Class<T> type,
                                          final Class<?> beanType,
                                          final Annotation[] qualifiers,
                                          final String name,
                                          final boolean concrete,
                                          final AsyncBeanProvider<T> callback,
                                          final T instance,
                                          final Class<Object> beanActivatorType) {

    return new AsyncSingletonBean<T>(
        beanManager, type, beanType, qualifiers, name, concrete, callback, instance, beanActivatorType);
  }
  
   public static <T> AsyncBeanDef<T> newBean(final AsyncBeanManagerImpl beanManager,
         								    final Class<T> type,
         								    final Class<?> beanType,
         								    final Annotation[] qualifiers,
          									final String name,
          									final boolean concrete,
          									final AsyncBeanProvider<T> callback,
          									final AsyncCreationalContext context,
          									final AsyncInjectionContext injectionContext,
          									final Class<Object> beanActivatorType) {
    return new AsyncSingletonBean<T>(
    	beanManager, type, beanType, qualifiers, name, concrete, callback, context, injectionContext, beanActivatorType);
  }

  
  @Override
  public void getInstance(final CreationalCallback<T> callback, final AsyncCreationalContext context) {
    SingletonBeanProvider<T> singletonBeanProvider = getOrCreateSingletonBeanProvider();
    singletonBeanProvider.getInstance(callback);
  }

  private SingletonBeanProvider<T> getOrCreateSingletonBeanProvider() {
    SingletonBeanProvider<T> singletonBeanProvider = beanManager.getLazySingleBeanProvider(beanType);
    if (singletonBeanProvider == null) {
      singletonBeanProvider = new SingletonBeanProvider<T>(instance) {
        
        AsyncCreationalContext currentContext;
        
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected void getNewInstance(
                final CreationalCallback<T> newInstanceCallback) {
          //create with dependend context, that ensures the correct postContruct calling behaviour
          currentContext = new AsyncCreationalContext(beanManager, Dependent.class);
          currentContext.getSingletonInstanceOrNew(injectionContext, beanProvider, newInstanceCallback, type, (Class) beanType,getQualifiersAsArray(), name);
        }

        @Override
        protected void onNewInstanceCreated(final T newInstance,
                final CreationalCallback<T> callback) {
          currentContext.finish(new Runnable() {
            @Override
            public void run() {
              callback.callback(newInstance);
            }
          });
        }

        private Annotation[] getQualifiersAsArray() {
          return qualifiers.toArray(new Annotation[qualifiers.size()]);
        };
      };
      beanManager.putLazySingleBeanProvider(beanType, singletonBeanProvider);
    }
    return singletonBeanProvider;
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

  /**
   * indicates wheter an instance is present or not
   */
  public boolean isReady() {
    return getOrCreateSingletonBeanProvider().isReady();
  }

  public boolean isLazySingleton() {
    return instance == null;
  }

  public void setInstance(T instance) {
    if (isReady())
      return;
    getOrCreateSingletonBeanProvider().setInstance(instance);
  }
}
