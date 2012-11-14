package org.jboss.errai.ioc.client;

import org.jboss.errai.ioc.client.container.BeanProvider;
import org.jboss.errai.ioc.client.container.CreationalContext;

import java.lang.annotation.Annotation;

/**
 * @author Mike Brock
 */
public interface BootstrapInjectionContext {
  public CreationalContext getRootContext();
}

