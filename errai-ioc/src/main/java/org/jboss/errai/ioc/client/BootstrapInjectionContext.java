package org.jboss.errai.ioc.client;

import org.jboss.errai.ioc.client.container.BeanProvider;

import java.lang.annotation.Annotation;

/**
 * @author Mike Brock
 */
public interface BootstrapInjectionContext<C> {
  public void addBean(final Class type,
                      final Class beanType,
                      final C callback,
                      final Object instance,
                      final Annotation[] qualifiers);

  public void addBean(final Class type,
                        final Class beanType,
                        final C callback,
                        final Object instance,
                        final Annotation[] qualifiers,
                        final String name);

  public void addBean(final Class type,
                        final Class beanType,
                        final C callback,
                        final Object instance,
                        final Annotation[] qualifiers,
                        final String name,
                        final boolean concrete);
}
