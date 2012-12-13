package org.jboss.errai.ioc.client.container.async;

/**
 * @author Mike Brock
 */
public interface CreationalCallback<T> {
  public void callback(T beanInstance);
}
