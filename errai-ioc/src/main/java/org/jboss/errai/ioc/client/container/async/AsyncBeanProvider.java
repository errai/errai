package org.jboss.errai.ioc.client.container.async;

import org.jboss.errai.ioc.client.container.CreationalContext;

/**
 * @author Mike Brock
 */
public interface AsyncBeanProvider<T> {
  public void getInstance(CreationalCallback<T> callback, CreationalContext creationalContext);
}
