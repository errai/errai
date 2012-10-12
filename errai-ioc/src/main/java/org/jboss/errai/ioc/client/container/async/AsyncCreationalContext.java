package org.jboss.errai.ioc.client.container.async;

import org.jboss.errai.ioc.client.InjectionContext;
import org.jboss.errai.ioc.client.SimpleInjectionContext;
import org.jboss.errai.ioc.client.container.AbstractCreationalContext;
import org.jboss.errai.ioc.client.container.BeanRef;

import java.lang.annotation.Annotation;

/**
 * @author Mike Brock
 */
public class AsyncCreationalContext extends AbstractCreationalContext {
  private final AsyncBeanManager beanManager;

  public AsyncCreationalContext(final AsyncBeanManager beanManager,
                                final Class<? extends Annotation> scope) {
    super(scope);
    this.beanManager = beanManager;
  }

  public AsyncCreationalContext(final AsyncBeanManager beanManager, final boolean immutableContext,
                                final Class<? extends Annotation> scope) {
    super(immutableContext, scope);
    this.beanManager = beanManager;
  }

  @Override
  public void addProxyReference(final Object proxyRef, final Object realRef) {
  }

  public <T> void getInstanceOrNew(final AsyncBeanProvider<T> beanProvider,
                                   final CreationalCallback<T> creationalCallback,
                                   final Class<?> beanType,
                                   final Annotation[] qualifiers) {
    final BeanRef ref = getBeanReference(beanType, qualifiers);

    if (wired.containsKey(ref)) {
      creationalCallback.callback((T) wired.get(ref));
    }
    else {
      beanProvider.getInstance(creationalCallback, this);
    }
  }

  public <T> T getSingletonInstanceOrNew(final InjectionContext<AsyncBeanProvider> injectionContext,
                                         final AsyncBeanProvider<T> beanProvider,
                                         final CreationalCallback<T> creationalCallback,
                                         final Class<?> beanType,
                                         final Annotation[] qualifiers) {

    @SuppressWarnings("unchecked") T inst = (T) getBeanInstance(beanType, qualifiers);

    if (inst != null) {
      return inst;
    }
    else {
      beanProvider.getInstance(new CreationalCallback<T>() {
        @Override
        public void callback(final T beanInstance) {
          creationalCallback.callback(beanInstance);
          injectionContext.addBean(beanType, beanType, beanProvider, beanInstance, qualifiers);

        }
      }, this);


      return inst;
    }
  }


  @Override
  public void finish() {
  }

  public void destroyContext() {

  }
}
