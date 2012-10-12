package org.jboss.errai.ioc.client.container.async;

import com.apple.eawt.Application;
import org.jboss.errai.ioc.client.InjectionContext;
import org.jboss.errai.ioc.client.container.BeanProvider;
import org.jboss.errai.ioc.client.container.IOC;

import javax.enterprise.context.ApplicationScoped;
import java.lang.annotation.Annotation;

/**
 * @author Mike Brock
 */
public class AsyncInjectionContext implements InjectionContext<AsyncBeanProvider> {
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
