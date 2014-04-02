package org.jboss.errai.ioc.client.container.async;

import java.lang.annotation.Annotation;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.ioc.client.BootstrapInjectionContext;
import org.jboss.errai.ioc.client.container.IOC;

/**
 * @author Mike Brock
 */
@SuppressWarnings("unchecked")
public class AsyncInjectionContext implements BootstrapInjectionContext {
  private final AsyncBeanManager manager;
  private final AsyncCreationalContext context;

  public AsyncInjectionContext() {
    this.manager = IOC.getAsyncBeanManager();
    this.context = new AsyncCreationalContext(manager, true, ApplicationScoped.class);
  }

  public void addBean(final Class type,
                      final Class beanType,
                      final AsyncBeanProvider provider,
                      final Object instance,
                      final Annotation[] qualifiers,
                      final String name) {

    ((AsyncBeanManagerSetup) manager).addBean(type, beanType, provider, instance, qualifiers, name);
  }

  public void addBean(final Class type,
                      final Class beanType,
                      final AsyncBeanProvider callback,
                      final boolean singleton,
                      final Annotation[] qualifiers) {
    if (singleton) {
      final CreationalCallback creationalCallback = new CreationalCallback() {
        @Override
        public void callback(final Object beanInstance) {
          ((AsyncBeanManagerSetup) manager).addBean(type, beanType, callback, beanInstance, qualifiers);
          context.getBeanContext().finish(this);
        }
      };
      context.getBeanContext().wait(creationalCallback);
      callback.getInstance(creationalCallback, context);
    }
    else {
      ((AsyncBeanManagerSetup) manager).addBean(type, beanType, callback, null, qualifiers);
    }
  }

  public void addBean(final Class type,
                      final Class beanType,
                      final AsyncBeanProvider provider,
                      final boolean singleton,
                      final Annotation[] qualifiers,
                      final String name,
                      final boolean concrete) {

    addBean(type, beanType, provider, singleton, qualifiers, name, concrete, null);   
  }

  public void addBean(final Class type,
      final Class beanType,
      final AsyncBeanProvider provider,
      final boolean singleton,
      final Annotation[] qualifiers,
      final String name,
      final boolean concrete,
      final Class beanActivatorType) {

    if (singleton) {
      final CreationalCallback creationalCallback = new CreationalCallback() {
        @Override
        public void callback(final Object beanInstance) {
          }

        @Override
        public String toString() {
          return type.getName();
        }
      };
      context.getSingletonInstanceOrNew(this, provider, creationalCallback, type, beanType, qualifiers, name);
    }
    else {
      ((AsyncBeanManagerSetup) manager).addBean(type, beanType, provider, null, qualifiers, name, concrete,
          beanActivatorType);
    }

  }

  @Override
  public AsyncCreationalContext getRootContext() {
    return context;
  }
}