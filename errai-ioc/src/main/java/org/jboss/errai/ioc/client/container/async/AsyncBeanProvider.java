package org.jboss.errai.ioc.client.container.async;

/**
 * @author Mike Brock
 */
public interface AsyncBeanProvider<T> {
  public void getInstance(CreationalCallback<T> callback, AsyncCreationalContext creationalContext);
}
