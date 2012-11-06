package org.jboss.errai.ioc.client.container.async;

import org.jboss.errai.ioc.client.BootstrapInjectionContext;
import org.jboss.errai.ioc.client.container.IOC;

import javax.enterprise.context.ApplicationScoped;
import java.lang.annotation.Annotation;

/**
 * @author Mike Brock
 */
@SuppressWarnings("unchecked")
public class AsyncInjectionContext implements BootstrapInjectionContext<AsyncBeanProvider> {
  private final AsyncBeanManager manager;
  private final AsyncCreationalContext context;

  public AsyncInjectionContext() {
    this.manager = IOC.getAsyncBeanManager();
    this.context = new AsyncCreationalContext(manager, true, ApplicationScoped.class);
  }


  public void addBean(final Class type,
                      final Class beanType,
                      final AsyncBeanProvider callback,
                      final boolean singleton,
                      final Annotation[] qualifiers) {
    if (singleton) {
      callback.getInstance(new CreationalCallback() {
        @Override
        public void callback(final Object beanInstance) {
           manager.addBean(type, beanType, callback, beanInstance, qualifiers);
        }
      }, context);
    }
    else {
      manager.addBean(type, beanType, callback, null, qualifiers);
    }
  }

  public void addBean(final Class type,
                      final Class beanType,
                      final AsyncBeanProvider callback,
                      final boolean singleton,
                      final Annotation[] qualifiers,
                      final String name) {
    if (singleton) {
      callback.getInstance(new CreationalCallback() {
        @Override
        public void callback(final Object beanInstance) {
           manager.addBean(type, beanType, callback, beanInstance, qualifiers, name);
        }
      }, context);
    }
    else {
      manager.addBean(type, beanType, callback, null, qualifiers, name);
    }
  }

  public void addBean(final Class type,
                      final Class beanType,
                      final AsyncBeanProvider callback,
                      final boolean singleton,
                      final Annotation[] qualifiers,
                      final String name,
                      final boolean concrete) {

    if (singleton) {
      callback.getInstance(new CreationalCallback() {
        @Override
        public void callback(final Object beanInstance) {
           manager.addBean(type, beanType, callback, beanInstance, qualifiers, name, concrete);
        }
      }, context);
    }
    else {
      manager.addBean(type, beanType, callback, null, qualifiers, name, concrete);
    }

  }

  @Override
  public AsyncCreationalContext getRootContext() {
    return context;
  }
}
