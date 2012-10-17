package org.jboss.errai.ioc.client.container.async;

import org.jboss.errai.ioc.client.BootstrapInjectionContext;
import org.jboss.errai.ioc.client.container.IOC;

import javax.enterprise.context.ApplicationScoped;
import java.lang.annotation.Annotation;

/**
 * @author Mike Brock
 */
public class AsyncInjectionContext implements BootstrapInjectionContext<AsyncBeanProvider> {
  private final AsyncBeanManager manager;
  private final AsyncCreationalContext context;

  public AsyncInjectionContext() {
    this.manager = IOC.getAsyncBeanManager();
    this.context = new AsyncCreationalContext(manager, true, ApplicationScoped.class);
  }

  @Override
  public void addBean(Class type, Class beanType, AsyncBeanProvider callback, Object instance, Annotation[] qualifiers) {
  }

  @Override
  public void addBean(Class type, Class beanType, AsyncBeanProvider callback, Object instance, Annotation[] qualifiers, String name) {
  }

  @Override
  public void addBean(Class type, Class beanType, AsyncBeanProvider callback, Object instance, Annotation[] qualifiers, String name, boolean concrete) {
  }
}
