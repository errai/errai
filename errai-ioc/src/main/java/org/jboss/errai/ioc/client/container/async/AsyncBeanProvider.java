package org.jboss.errai.ioc.client.container.async;

import org.jboss.errai.common.client.util.CreationalCallback;

/**
 * @author Mike Brock
 */
public interface AsyncBeanProvider<T> {
  public void getInstance(CreationalCallback<T> callback, AsyncCreationalContext creationalContext);
}
